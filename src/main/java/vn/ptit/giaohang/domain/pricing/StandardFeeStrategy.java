package vn.ptit.giaohang.domain.pricing;

import org.springframework.stereotype.Component;

/**
 * Dich vu tieu chuan: khong co phu phi.
 */
@Component
public class StandardFeeStrategy extends AbstractShippingFeeStrategy {

    @Override
    public ServiceType getServiceType() {
        return ServiceType.TIEU_CHUAN;
    }

    @Override
    protected long surcharge(FeeRequest request, PriceConfig config) {
        return 0;
    }
}
