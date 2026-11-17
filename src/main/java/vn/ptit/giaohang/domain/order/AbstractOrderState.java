package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.user.Shipper;

import java.util.Set;

/**
 * Mac dinh moi thao tac deu bi tu choi. Lop trang thai cu the chi ghi de cac thao tac no cho phep.
 */
abstract class AbstractOrderState implements OrderState {

    @Override
    public Set<OrderAction> allowedActions(Order order) {
        return Set.of();
    }

    @Override
    public void assignShipper(Order order, Shipper shipper) {
        reject(order, OrderAction.ASSIGN);
    }

    @Override
    public void cancel(Order order) {
        reject(order, OrderAction.CANCEL);
    }

    @Override
    public void pickUp(Order order) {
        reject(order, OrderAction.PICK_UP);
    }

    @Override
    public void deliverSuccess(Order order, long collectedAmount) {
        reject(order, OrderAction.DELIVER_SUCCESS);
    }

    @Override
    public void deliverFail(Order order, String reason) {
        reject(order, OrderAction.DELIVER_FAIL);
    }

    @Override
    public void retryDelivery(Order order, Shipper shipper) {
        reject(order, OrderAction.RETRY);
    }

    @Override
    public void startReturn(Order order) {
        reject(order, OrderAction.START_RETURN);
    }

    @Override
    public void confirmReturned(Order order) {
        reject(order, OrderAction.CONFIRM_RETURNED);
    }

    protected void reject(Order order, OrderAction action) {
        throw new BusinessException("Không thể " + action.getLabel() + " khi đơn "
                + order.getCode() + " đang ở trạng thái \"" + order.getStatus().getLabel() + "\"");
    }

    protected void requireActiveShipper(Shipper shipper) {
        if (shipper == null) {
            throw new BusinessException("Vui lòng chọn shipper");
        }
        if (!shipper.isActive()) {
            throw new BusinessException("Shipper " + shipper.getFullName() + " đang bị khóa");
        }
    }
}
