package vn.ptit.giaohang.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Admin;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.dto.ShipperEditForm;
import vn.ptit.giaohang.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kiem thu tai khoan (doi / dat lai mat khau, sua shipper), phan trang va thong ke theo ngay ket thuc.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountAndStatsIntegrationTest {

    @Autowired AccountService accountService;
    @Autowired OrderService orderService;
    @Autowired RemittanceService remittanceService;
    @Autowired SettlementService settlementService;
    @Autowired StatisticsService statisticsService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserRepository userRepository;
    @Autowired DeliveryZoneRepository zoneRepository;
    @Autowired WardRepository wardRepository;
    @Autowired PriceConfigRepository priceConfigRepository;
    @Autowired EntityManager em;

    Admin admin;
    Shop shop;
    Shipper zone1Shipper;
    Shipper zone2Shipper;
    DeliveryZone z1;
    DeliveryZone z2;
    Ward ward1;
    Ward ward2;

    @BeforeEach
    void setUp() {
        z1 = zoneRepository.save(new DeliveryZone("V1", "Vùng 1", null));
        z2 = zoneRepository.save(new DeliveryZone("V2", "Vùng 2", null));
        ward1 = wardRepository.save(new Ward("Cầu Giấy", z1));
        ward2 = wardRepository.save(new Ward("Hà Đông", z2));
        priceConfigRepository.save(new PriceConfig(ServiceType.TIEU_CHUAN, 20_000, 30_000, 2_000, 500, 2_500, 30_000, 0));
        priceConfigRepository.save(new PriceConfig(ServiceType.HOA_TOC, 35_000, 35_000, 2_000, 500, 5_000, 5_000, 15_000));

        admin = userRepository.save(new Admin("admin_a", passwordEncoder.encode("admin123"), "Admin", "0900000000"));
        shop = new Shop("shop_a", passwordEncoder.encode("old-pass"), "Chủ shop", "0900000001", "Shop A", "1 Láng", ward1);
        shop.updateBankAccount("VCB", "0011", "CHU SHOP");
        userRepository.save(shop);
        // Ten bat dau bang "A" de dung truoc theo thu tu ten, kiem tra viec sap xep theo vung
        zone2Shipper = userRepository.save(new Shipper("shipper_v2", "x", "An Vùng Hai", "0900000002", null, z2));
        zone1Shipper = userRepository.save(new Shipper("shipper_v1", "x", "Bình Vùng Một", "0900000003", null, z1));
    }

    private Order createOrder(long cod) {
        OrderForm f = new OrderForm();
        f.setPickupAddress("1 Láng");
        f.setPickupWardId(ward1.getId());
        f.setReceiverName("Người nhận");
        f.setReceiverPhone("0911111111");
        f.setReceiverAddress("2 Xã Đàn");
        f.setDeliveryWardId(ward1.getId());
        f.setItemDescription("Áo");
        f.setWeightGram(500);
        f.setCodAmount(cod);
        f.setServiceType(ServiceType.TIEU_CHUAN);
        return orderService.createOrder(shop.getId(), f);
    }

    // ---------------- Mat khau ----------------

    @Test
    void changePasswordRequiresCorrectCurrentPassword() {
        assertThatThrownBy(() -> accountService.changePassword(shop.getId(), "wrong", "new-pass"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("hiện tại không đúng");

        accountService.changePassword(shop.getId(), "old-pass", "new-pass");
        assertThat(passwordEncoder.matches("new-pass", accountService.getUser(shop.getId()).getPasswordHash())).isTrue();
    }

    @Test
    void newPasswordMustDifferFromCurrent() {
        assertThatThrownBy(() -> accountService.changePassword(shop.getId(), "old-pass", "old-pass"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void adminResetsPasswordForShopButNotForAdmin() {
        accountService.resetPassword(shop.getId(), "reset-123");
        assertThat(passwordEncoder.matches("reset-123", accountService.getUser(shop.getId()).getPasswordHash())).isTrue();

        assertThatThrownBy(() -> accountService.resetPassword(admin.getId(), "reset-123"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> accountService.resetPassword(shop.getId(), "123"))
                .isInstanceOf(BusinessException.class);
    }

    // ---------------- Shipper ----------------

    @Test
    void adminUpdatesShipperInfoAndZone() {
        ShipperEditForm f = ShipperEditForm.from(zone2Shipper);
        f.setPhone("0988888888");
        f.setVehiclePlate("29H1-111.11");
        f.setZoneId(z1.getId());
        accountService.updateShipper(zone2Shipper.getId(), f);

        Shipper updated = accountService.getShipper(zone2Shipper.getId());
        assertThat(updated.getPhone()).isEqualTo("0988888888");
        assertThat(updated.getVehiclePlate()).isEqualTo("29H1-111.11");
        assertThat(updated.getZone().getId()).isEqualTo(z1.getId());
    }

    @Test
    void shippersCoveringDeliveryZoneComeFirst() {
        assertThat(accountService.shippersForDelivery(ward1)).first().isEqualTo(zone1Shipper);
        assertThat(accountService.shippersForDelivery(ward2)).first().isEqualTo(zone2Shipper);
    }

    // ---------------- Phan trang ----------------

    @Test
    void orderSearchIsPaginated() {
        for (int i = 0; i < OrderService.PAGE_SIZE + 2; i++) {
            createOrder(0);
        }
        Page<Order> first = orderService.searchForShop(shop.getId(), null, null, 0);
        Page<Order> second = orderService.searchForShop(shop.getId(), null, null, 1);
        assertThat(first.getContent()).hasSize(OrderService.PAGE_SIZE);
        assertThat(second.getContent()).hasSize(2);
        assertThat(first.getTotalElements()).isEqualTo(OrderService.PAGE_SIZE + 2);
        assertThat(orderService.searchForShop(shop.getId(), null, null, -5).getNumber()).isZero();
    }

    // ---------------- Thong ke ----------------

    @Test
    void settlingAnOrderDoesNotMoveItInStatistics() {
        Order order = createOrder(100_000);
        orderService.assign(admin.getId(), order.getId(), zone1Shipper.getId());
        orderService.pickUp(zone1Shipper.getId(), order.getId());
        orderService.deliverSuccess(zone1Shipper.getId(), order.getId(), 100_000);
        em.flush();
        LocalDateTime finishedAt = order.getFinishedAt();
        assertThat(finishedAt).isNotNull();

        CodRemittance r = remittanceService.submit(zone1Shipper.getId(), null);
        remittanceService.confirm(admin.getId(), r.getId());
        settlementService.create(shop.getId(), null);
        em.flush();
        assertThat(order.getFinishedAt()).isEqualTo(finishedAt);

        LocalDate today = LocalDate.now();
        StatisticsService.AdminStats stats = statisticsService.adminStats(today.minusDays(6), today);
        assertThat(stats.daily()).hasSize(7);
        assertThat(stats.deliveredInPeriod()).isEqualTo(1);
        assertThat(stats.revenueInPeriod()).isEqualTo(order.getShippingFee());
        assertThat(stats.chartDelivered().get(6)).isEqualTo(1L);
        assertThat(stats.successRate()).isEqualTo(100.0);

        // Ky khong chua ngay hom nay thi khong tinh don nay
        StatisticsService.AdminStats past = statisticsService.adminStats(today.minusDays(10), today.minusDays(1));
        assertThat(past.deliveredInPeriod()).isZero();
    }

    @Test
    void statisticsRejectsInvalidRange() {
        LocalDate today = LocalDate.now();
        assertThatThrownBy(() -> statisticsService.adminStats(today, today.minusDays(1)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> statisticsService.adminStats(today.minusDays(400), today))
                .isInstanceOf(BusinessException.class);
    }
}
