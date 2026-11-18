package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.finance.RemittanceStatus;
import vn.ptit.giaohang.domain.finance.SettlementStatus;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.repository.CodRemittanceRepository;
import vn.ptit.giaohang.repository.OrderRepository;
import vn.ptit.giaohang.repository.SettlementRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Thong ke cho quan tri vien. So lieu theo ky tinh theo thoi diem don ket thuc (Order.finishedAt).
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    /** Khoang thong ke toi da, tranh bieu do qua nhieu cot. */
    public static final int MAX_RANGE_DAYS = 366;

    private static final EnumSet<OrderStatus> REVENUE_STATUSES =
            EnumSet.of(OrderStatus.GIAO_THANH_CONG, OrderStatus.DA_HOAN);

    /** So lieu cua mot ngay tren bieu do. Doanh thu = phi van chuyen cua don giao thanh cong va don hoan. */
    public record DailyStat(LocalDate date, long delivered, long returned, long revenue) {
    }

    /** So lieu tong hop cho trang quan tri. */
    public record AdminStats(Map<OrderStatus, Long> countByStatus, long totalOrders,
                             List<DailyStat> daily,
                             long pendingRemittances, long unpaidSettlements,
                             LocalDate from, LocalDate to) {

        public long deliveredInPeriod() {
            return daily.stream().mapToLong(DailyStat::delivered).sum();
        }

        public long returnedInPeriod() {
            return daily.stream().mapToLong(DailyStat::returned).sum();
        }

        public long revenueInPeriod() {
            return daily.stream().mapToLong(DailyStat::revenue).sum();
        }

        // ---- Du lieu cho bieu do (moi phan tu ung voi mot ngay) ----

        public List<String> chartLabels() {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
            return daily.stream().map(d -> d.date().format(f)).toList();
        }

        public List<Long> chartDelivered() {
            return daily.stream().map(DailyStat::delivered).toList();
        }

        public List<Long> chartReturned() {
            return daily.stream().map(DailyStat::returned).toList();
        }

        public List<Long> chartRevenue() {
            return daily.stream().map(DailyStat::revenue).toList();
        }

        /** Ty le giao thanh cong (%) tren cac don giao thanh cong hoac hoan trong ky. */
        public double successRate() {
            long delivered = deliveredInPeriod();
            long finished = delivered + returnedInPeriod();
            return finished == 0 ? 0 : 100.0 * delivered / finished;
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
        if (from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new BusinessException("Chỉ xem được tối đa " + MAX_RANGE_DAYS + " ngày mỗi lần");
        }

        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        long total = 0;
        for (OrderStatus status : OrderStatus.values()) {
            long c = orderRepository.countByStatus(status);
            counts.put(status, c);
            total += c;
        }
        return new AdminStats(counts, total, daily(from, to),
                remittanceRepository.countByStatus(RemittanceStatus.CHO_XAC_NHAN),
                settlementRepository.countByStatus(SettlementStatus.CHO_THANH_TOAN),
                from, to);
    }

    /** Gom cac don ket thuc trong ky theo ngay; ngay khong co don van co mot dong so 0. */
    private List<DailyStat> daily(LocalDate from, LocalDate to) {
        List<Order> finished = orderRepository.findFinishedBetween(REVENUE_STATUSES,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<LocalDate, long[]> byDay = new TreeMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            byDay.put(d, new long[3]);
        }
        for (Order o : finished) {
            long[] row = byDay.get(o.getFinishedAt().toLocalDate());
            if (o.getStatus() == OrderStatus.GIAO_THANH_CONG) {
                row[0]++;
            } else {
                row[1]++;
            }
            row[2] += o.getShippingFee();
        }

        List<DailyStat> result = new ArrayList<>();
        byDay.forEach((date, row) -> result.add(new DailyStat(date, row[0], row[1], row[2])));
        return result;
    }

    public Map<OrderStatus, Long> shopCountByStatus(Long shopId) {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByShopIdAndStatus(shopId, status));
        }
        return counts;
    }
}
