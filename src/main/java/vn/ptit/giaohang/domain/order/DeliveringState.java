package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.common.BusinessException;

import java.util.Set;

/** Shipper dang giao hang cho nguoi nhan. */
class DeliveringState extends AbstractOrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        return Set.of(OrderAction.DELIVER_SUCCESS, OrderAction.DELIVER_FAIL);
    }

    @Override
    public void deliverSuccess(Order order, long collectedAmount) {
        if (collectedAmount != order.getCodAmount()) {
            throw new BusinessException("Số tiền thu hộ phải đúng " + order.getCodAmount()
                    + " đ (đang nhập " + collectedAmount + " đ)");
        }
        order.markDelivered(collectedAmount);
        order.moveTo(OrderStatus.GIAO_THANH_CONG);
    }

    @Override
    public void deliverFail(Order order, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do giao thất bại");
        }
        order.recordFailedAttempt(reason.trim());
        order.moveTo(OrderStatus.GIAO_THAT_BAI);
    }
}
