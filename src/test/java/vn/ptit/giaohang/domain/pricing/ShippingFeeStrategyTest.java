package vn.ptit.giaohang.domain.pricing;

import org.junit.jupiter.api.Test;
import vn.ptit.giaohang.common.BusinessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static vn.ptit.giaohang.TestData.*;

class ShippingFeeStrategyTest {

    private final ShippingFeeStrategy standard = new StandardFeeStrategy();
    private final ShippingFeeStrategy express = new ExpressFeeStrategy();

    private FeeQuote standardQuote(int weight, boolean sameZone) {
        return standard.calculate(new FeeRequest(CAU_GIAY, sameZone ? KIM_LIEN : HA_DONG, weight, ServiceType.TIEU_CHUAN),
                standardPrice());
    }

    @Test
    void standardSameZoneWithinIncludedWeight() {
        FeeQuote q = standardQuote(2_000, true);
        assertThat(q.sameZone()).isTrue();
        assertThat(q.total()).isEqualTo(20_000);
    }

    @Test
    void standardCrossZoneUsesCrossZoneFee() {
        FeeQuote q = standardQuote(1_000, false);
        assertThat(q.sameZone()).isFalse();
        assertThat(q.total()).isEqualTo(30_000);
    }

    @Test
    void overweightStepsAreRoundedUp() {
        // vuot 1 g -> 1 buoc; vuot 501 g -> 2 buoc
        assertThat(standardQuote(2_001, true).weightFee()).isEqualTo(2_500);
        assertThat(standardQuote(2_501, true).weightFee()).isEqualTo(5_000);
        assertThat(standardQuote(2_500, true).weightFee()).isEqualTo(2_500);
    }

    @Test
    void expressAddsSurchargeOnlyForSameZone() {
        FeeQuote q = express.calculate(new FeeRequest(CAU_GIAY, KIM_LIEN, 1_000, ServiceType.HOA_TOC), expressPrice());
        assertThat(q.surcharge()).isEqualTo(15_000);
        assertThat(q.total()).isEqualTo(50_000);
    }

    @Test
    void expressRejectsCrossZone() {
        assertThatThrownBy(() -> express.calculate(new FeeRequest(CAU_GIAY, HA_DONG, 1_000, ServiceType.HOA_TOC), expressPrice()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cùng một vùng");
    }

    @Test
    void rejectsWardOutsideServiceArea() {
        assertThatThrownBy(() -> standard.calculate(new FeeRequest(CAU_GIAY, SON_TAY, 1_000, ServiceType.TIEU_CHUAN), standardPrice()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ngoài phạm vi");
    }

    @Test
    void rejectsOverMaxWeight() {
        assertThatThrownBy(() -> express.calculate(new FeeRequest(CAU_GIAY, KIM_LIEN, 5_001, ServiceType.HOA_TOC), expressPrice()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tối đa");
    }

    @Test
    void factoryReturnsStrategyForEachServiceType() {
        ShippingFeeStrategyFactory factory = new ShippingFeeStrategyFactory(List.of(standard, express));
        assertThat(factory.get(ServiceType.TIEU_CHUAN)).isSameAs(standard);
        assertThat(factory.get(ServiceType.HOA_TOC)).isSameAs(express);
    }

    @Test
    void factoryFailsWhenAServiceTypeHasNoStrategy() {
        assertThatThrownBy(() -> new ShippingFeeStrategyFactory(List.of(standard)))
                .isInstanceOf(IllegalStateException.class);
    }
}
