package vn.ptit.giaohang.domain.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.user.Shipper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static vn.ptit.giaohang.TestData.*;

class OrderStateTest {

    private Order order;
    private Shipper shipper;

    @BeforeEach
    void setUp() {
        order = order(shop(), 300_000);
        shipper = shipper();
    }

    @Test
    void newOrderWaitsForAssignment() {
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CHO_PHAN_CONG);
        assertThat(order.allowedActions()).containsExactlyInAnyOrder(OrderAction.ASSIGN, OrderAction.CANCEL);
    }

    @Test
    void happyPathToDelivered() {
        order.assignShipper(shipper);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CHO_LAY_HANG);
        assertThat(order.isAssignedTo(shipper)).isTrue();

        order.pickUp();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DANG_GIAO);

        order.deliverSuccess(300_000);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.GIAO_THANH_CONG);
        assertThat(order.getCollectedAmount()).isEqualTo(300_000);
        assertThat(order.isCodPendingRemittance()).isTrue();
        assertThat(order.allowedActions()).isEmpty();
    }

    @Test
    void cannotPickUpBeforeAssignment() {
        assertThatThrownBy(order::pickUp)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chờ phân công");
    }

    @Test
    void cannotCancelAfterPickUp() {
        order.assignShipper(shipper);
        order.pickUp();
        assertThatThrownBy(order::cancel).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelBeforePickUpIsAllowed() {
        order.assignShipper(shipper);
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DA_HUY);
    }

    @Test
    void collectedAmountMustMatchCod() {
        order.assignShipper(shipper);
        order.pickUp();
        assertThatThrownBy(() -> order.deliverSuccess(250_000))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("300000");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DANG_GIAO);
    }

    @Test
    void failRequiresReason() {
        order.assignShipper(shipper);
        order.pickUp();
        assertThatThrownBy(() -> order.deliverFail(" ")).isInstanceOf(BusinessException.class);
    }

    @Test
    void retryAllowedUntilMaxAttemptsThenOnlyReturn() {
        order.assignShipper(shipper);
        order.pickUp();
        for (int i = 1; i < Order.MAX_DELIVERY_ATTEMPTS; i++) {
            order.deliverFail("Khách không nghe máy");
            assertThat(order.allows(OrderAction.RETRY)).isTrue();
            order.retryDelivery(shipper);
        }
        order.deliverFail("Khách từ chối");
        assertThat(order.getFailedAttempts()).isEqualTo(Order.MAX_DELIVERY_ATTEMPTS);
        assertThat(order.allowedActions()).containsExactly(OrderAction.START_RETURN);
        assertThatThrownBy(() -> order.retryDelivery(shipper)).isInstanceOf(BusinessException.class);

        order.startReturn();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DANG_HOAN);
        order.confirmReturned();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DA_HOAN);
        assertThat(order.isReadyForSettlement()).isTrue();
        assertThat(order.settlementCod()).isZero();
    }

    @Test
    void cannotAssignInactiveShipper() {
        shipper.deactivate();
        assertThatThrownBy(() -> order.assignShipper(shipper))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khóa");
    }

    @Test
    void allowedActionsMatchWhatEachStateAccepts() {
        // Moi thao tac duoc liet ke trong allowedActions phai thuc hien duoc o trang thai do
        order.assignShipper(shipper);
        assertThat(order.allowedActions()).contains(OrderAction.PICK_UP, OrderAction.CANCEL, OrderAction.ASSIGN);
        order.pickUp();
        assertThat(order.allowedActions()).containsExactlyInAnyOrder(OrderAction.DELIVER_SUCCESS, OrderAction.DELIVER_FAIL);
    }
}
