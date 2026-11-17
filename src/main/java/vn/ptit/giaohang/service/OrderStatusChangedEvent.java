package vn.ptit.giaohang.service;

import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.user.User;

/**
 * Su kien phat ra moi khi don hang doi trang thai (Observer pattern).
 * fromStatus = null khi don vua duoc tao.
 */
public record OrderStatusChangedEvent(Order order, OrderStatus fromStatus, OrderStatus toStatus,
                                      User actor, String note) {
}
