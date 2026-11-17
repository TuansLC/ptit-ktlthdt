package vn.ptit.giaohang.domain.order;

import vn.ptit.giaohang.domain.user.Shipper;

import java.util.Set;

/**
 * State pattern: moi trang thai don hang la mot lop cai dat interface nay.
 * Lop trang thai tu quyet dinh thao tac nao hop le va don chuyen sang trang thai nao,
 * nen lop Order khong can chuoi if/else hay switch theo trang thai.
 */
public interface OrderState {

    /** Cac thao tac hop le o trang thai hien tai (dung de an/hien nut tren giao dien). */
    Set<OrderAction> allowedActions(Order order);

    default boolean allows(Order order, OrderAction action) {
        return allowedActions(order).contains(action);
    }

    void assignShipper(Order order, Shipper shipper);

    void cancel(Order order);

    void pickUp(Order order);

    void deliverSuccess(Order order, long collectedAmount);

    void deliverFail(Order order, String reason);

    void retryDelivery(Order order, Shipper shipper);

    void startReturn(Order order);

    void confirmReturned(Order order);
}
