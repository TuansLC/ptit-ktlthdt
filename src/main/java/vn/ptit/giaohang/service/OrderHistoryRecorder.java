package vn.ptit.giaohang.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import vn.ptit.giaohang.domain.order.OrderStatusHistory;
import vn.ptit.giaohang.repository.OrderStatusHistoryRepository;

/**
 * Observer: lang nghe su kien doi trang thai va ghi nhat ky.
 * OrderService khong can biet co nhat ky; muon them thong bao (email, SMS) chi can them mot listener moi.
 * Listener chay dong bo trong cung transaction voi thao tac doi trang thai.
 */
@Component
public class OrderHistoryRecorder {

    private final OrderStatusHistoryRepository historyRepository;

    public OrderHistoryRecorder(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        historyRepository.save(new OrderStatusHistory(
                event.order(),
                event.fromStatus(),
                event.toStatus(),
                event.actor().getFullName(),
                event.actor().getRole().getLabel(),
                event.note()));
    }
}
