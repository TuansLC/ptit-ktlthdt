package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.domain.user.Shipper;

import java.util.Set;

/** Don moi tao, cho quan tri vien phan cong shipper. */
class WaitingAssignmentState extends AbstractOrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        return Set.of(OrderAction.ASSIGN, OrderAction.CANCEL);
    }

    @Override
    public void assignShipper(Order order, Shipper shipper) {
        requireActiveShipper(shipper);
        order.setShipper(shipper);
        order.moveTo(OrderStatus.CHO_LAY_HANG);
    }

    @Override
    public void cancel(Order order) {
        order.moveTo(OrderStatus.DA_HUY);
    }
}
