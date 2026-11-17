package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.domain.user.Shipper;

import java.util.Set;

/** Da phan cong, shipper dang den lay hang. Van cho phep doi shipper hoac huy don. */
class WaitingPickupState extends AbstractOrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        return Set.of(OrderAction.ASSIGN, OrderAction.CANCEL, OrderAction.PICK_UP);
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

    @Override
    public void pickUp(Order order) {
        order.moveTo(OrderStatus.DANG_GIAO);
    }
}
