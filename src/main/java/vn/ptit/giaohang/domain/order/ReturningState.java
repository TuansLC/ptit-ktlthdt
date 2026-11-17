package vn.ptit.giaohang.domain.order;

import java.util.Set;

/** Shipper dang mang hang tra lai cho shop. */
class ReturningState extends AbstractOrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        return Set.of(OrderAction.CONFIRM_RETURNED);
    }

    @Override
    public void confirmReturned(Order order) {
        order.moveTo(OrderStatus.DA_HOAN);
    }
}
