package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.domain.finance.RemittanceStatus;
import vn.ptit.giaohang.domain.finance.SettlementStatus;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.repository.CodRemittanceRepository;
import vn.ptit.giaohang.repository.OrderRepository;
import vn.ptit.giaohang.repository.SettlementRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private static final EnumSet<OrderStatus> REVENUE_STATUSES =
            EnumSet.of(OrderStatus.GIAO_THANH_CONG, OrderStatus.DA_HOAN);

    /** So lieu tong hop cho trang quan tri. */
    public record AdminStats(Map<OrderStatus, Long> countByStatus, long totalOrders,
                             long deliveredInPeriod, long returnedInPeriod, long revenueInPeriod,
                             long pendingRemittances, long unpaidSettlements,
                             LocalDate from, LocalDate to) {

        /** Ty le giao thanh cong (%) tren cac don da ket thuc trong ky. */
        public double successRate() {
            long finished = deliveredInPeriod + returnedInPeriod;
            return finished == 0 ? 0 : 100.0 * deliveredInPeriod / finished;
        }
    }

    private final OrderRepository orderRepository;
    private final CodRemittanceRepository remittanceRepository;
    private final SettlementRepository settlementRepository;

    public StatisticsService(OrderRepository orderRepository, CodRemittanceRepository remittanceRepository,
                             SettlementRepository settlementRepository) {
        this.orderRepository = orderRepository;
        this.remittanceRepository = remittanceRepository;
        this.settlementRepository = settlementRepository;
    }

    /** Thong ke trong khoang [from, to] (tinh ca ngay to). */
    public AdminStats adminStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        long total = 0;
        for (OrderStatus status : OrderStatus.values()) {
            long c = orderRepository.countByStatus(status);
            counts.put(status, c);
            total += c;
        }
        return new AdminStats(counts, total,
                orderRepository.countFinishedBetween(OrderStatus.GIAO_THANH_CONG, start, end),
                orderRepository.countFinishedBetween(OrderStatus.DA_HOAN, start, end),
                orderRepository.sumShippingFee(REVENUE_STATUSES, start, end),
                remittanceRepository.countByStatus(RemittanceStatus.CHO_XAC_NHAN),
                settlementRepository.countByStatus(SettlementStatus.CHO_THANH_TOAN),
                from, to);
    }

    public Map<OrderStatus, Long> shopCountByStatus(Long shopId) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByShopIdAndStatus(shopId, status));
        }
        return counts;
    }
}
