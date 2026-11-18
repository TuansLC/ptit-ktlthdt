package vn.ptit.giaohang.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.domain.finance.SettlementStatus;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.repository.*;
import vn.ptit.giaohang.service.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Chay toan bo qua trinh tao du lieu mau (profile test khong tu chay DataInitializer nen tao bang tay)
 * va kiem tra lich su van hanh hop ly cho trang thong ke.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DemoDataSeedTest {

    @Autowired UserRepository userRepository;
    @Autowired DeliveryZoneRepository zoneRepository;
    @Autowired WardRepository wardRepository;
    @Autowired PriceConfigRepository priceConfigRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired OrderService orderService;
    @Autowired RemittanceService remittanceService;
    @Autowired OrderRepository orderRepository;
    @Autowired SettlementRepository settlementRepository;
    @Autowired DemoHistorySeeder historySeeder;
    @Autowired StatisticsService statisticsService;
    @Autowired AccountService accountService;

    @Test
    void seedProducesSpreadOutHistoryAndConsistentMoney() {
        new DataInitializer(userRepository, zoneRepository, wardRepository, priceConfigRepository, passwordEncoder,
                orderService, remittanceService, orderRepository, historySeeder).run();

        LocalDate today = LocalDate.now();
        StatisticsService.AdminStats stats = statisticsService.adminStats(today.minusDays(29), today);

        // Co du lieu tren hau het cac ngay cua 4 tuan truoc, khong don vao 1-2 ngay
        long daysWithOrders = stats.daily().stream().filter(d -> d.delivered() + d.returned() > 0).count();
        assertThat(daysWithOrders).isGreaterThanOrEqualTo(25);
        assertThat(stats.deliveredInPeriod()).isGreaterThan(80);
        assertThat(stats.returnedInPeriod()).isPositive();
        assertThat(stats.successRate()).isBetween(80.0, 97.0);

        // Don lich su da duoc doi ngay, ma don mang ngay tao
        List<Order> all = orderRepository.search(null, null, null, null, Pageable.unpaged()).getContent();
        Order oldest = all.get(all.size() - 1);
        assertThat(oldest.getCreatedAt().toLocalDate()).isBefore(today.minusDays(20));
        assertThat(oldest.getCode()).contains(oldest.getCreatedAt().toLocalDate().toString().substring(2).replace("-", ""));

        // Lich su da nop COD va doi soat het; chi con cac don mau cua hom nay
        long historyPendingCod = accountService.allShippers().stream()
                .flatMap(s -> remittanceService.pendingOrders(s.getId()).stream())
                .filter(o -> o.getFinishedAt().toLocalDate().isBefore(today))
                .count();
        assertThat(historyPendingCod).isZero();
        assertThat(settlementRepository.countByStatus(SettlementStatus.DA_THANH_TOAN)).isGreaterThanOrEqualTo(8);
        assertThat(settlementRepository.countByStatus(SettlementStatus.CHO_THANH_TOAN)).isZero();

        // Cac don mau "dang xu ly" hom nay van con de demo
        assertThat(orderRepository.countByStatus(OrderStatus.CHO_PHAN_CONG)).isEqualTo(2);
        assertThat(orderRepository.countByStatus(OrderStatus.GIAO_THAT_BAI)).isEqualTo(1);
    }
}
