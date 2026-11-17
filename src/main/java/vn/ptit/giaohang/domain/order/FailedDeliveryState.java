package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.user.Shipper;

import java.util.Set;

/**
 * Giao that bai. Quan tri vien quyet dinh giao lai (toi da Order.MAX_DELIVERY_ATTEMPTS lan)
 * hoac chuyen hoan ve shop.
 */
class FailedDeliveryState extends AbstractOrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        if (order.canRetryDelivery()) {
            return Set.of(OrderAction.RETRY, OrderAction.START_RETURN);
        }
        return Set.of(OrderAction.START_RETURN);
    }

    @Override
    public void retryDelivery(Order order, Shipper shipper) {
        if (!order.canRetryDelivery()) {
            throw new BusinessException("Đơn " + order.getCode() + " đã giao thất bại "
                    + order.getFailedAttempts() + " lần, chỉ có thể chuyển hoàn");
        }
        requireActiveShipper(shipper);
        order.setShipper(shipper);
        order.moveTo(OrderStatus.DANG_GIAO);
    }

    @Override
    public void startReturn(Order order) {
        order.moveTo(OrderStatus.DANG_HOAN);
    }
}
