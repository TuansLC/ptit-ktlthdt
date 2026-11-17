package vn.ptit.giaohang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Admin;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.repository.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kiem thu tich hop toan bo quy trinh tren CSDL H2: tao don -> giao -> nop COD -> doi soat.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliveryFlowIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired RemittanceService remittanceService;
    @Autowired SettlementService settlementService;
    @Autowired UserRepository userRepository;
    @Autowired DeliveryZoneRepository zoneRepository;
    @Autowired WardRepository wardRepository;
    @Autowired PriceConfigRepository priceConfigRepository;

    Admin admin;
    Shop shop;
    Shop otherShop;
    Shipper shipper;
    Ward pickupWard;
    Ward deliveryWard;
    Ward crossZoneWard;

    @BeforeEach
    void setUp() {
        DeliveryZone z1 = zoneRepository.save(new DeliveryZone("V1", "Vùng 1", null));
        DeliveryZone z2 = zoneRepository.save(new DeliveryZone("V2", "Vùng 2", null));
        pickupWard = wardRepository.save(new Ward("Cầu Giấy", z1));
        deliveryWard = wardRepository.save(new Ward("Kim Liên", z1));
        crossZoneWard = wardRepository.save(new Ward("Hà Đông", z2));
        priceConfigRepository.save(new PriceConfig(ServiceType.TIEU_CHUAN, 20_000, 30_000, 2_000, 500, 2_500, 30_000, 0));
        priceConfigRepository.save(new PriceConfig(ServiceType.HOA_TOC, 35_000, 35_000, 2_000, 500, 5_000, 5_000, 15_000));

        admin = userRepository.save(new Admin("admin_t", "x", "Admin", "0900000000"));
        shop = new Shop("shop_t", "x", "Chủ shop", "0900000001", "Shop T", "1 Trần Duy Hưng", pickupWard);
        shop.updateBankAccount("VCB", "0011", "CHU SHOP");
        userRepository.save(shop);
        otherShop = userRepository.save(new Shop("shop_o", "x", "Shop khác", "0900000002", "Shop O", "2 Láng", pickupWard));
        shipper = userRepository.save(new Shipper("shipper_t", "x", "Shipper", "0900000003", "29B1", z1));
    }

    private Order createOrder(long cod, int weight, Ward to) {
        OrderForm f = new OrderForm();
        f.setPickupAddress("1 Trần Duy Hưng");
        f.setPickupWardId(pickupWard.getId());
        f.setReceiverName("Người nhận");
        f.setReceiverPhone("0911111111");
        f.setReceiverAddress("2 Xã Đàn");
        f.setDeliveryWardId(to.getId());
        f.setItemDescription("Áo");
        f.setWeightGram(weight);
        f.setCodAmount(cod);
        f.setServiceType(ServiceType.TIEU_CHUAN);
        return orderService.createOrder(shop.getId(), f);
    }

    @Test
    void fullFlowFromCreateToSettlement() {
        Order delivered = createOrder(500_000, 2_600, deliveryWard);   // 20.000 + 2 buoc x 2.500 = 25.000
        Order returned = createOrder(200_000, 1_000, crossZoneWard);   // 30.000
        assertThat(delivered.getCode()).startsWith("GH");
        assertThat(delivered.getShippingFee()).isEqualTo(25_000);
        assertThat(returned.getShippingFee()).isEqualTo(30_000);

        // Don 1: giao thanh cong
        orderService.assign(admin.getId(), delivered.getId(), shipper.getId());
        orderService.pickUp(shipper.getId(), delivered.getId());
        orderService.deliverSuccess(shipper.getId(), delivered.getId(), 500_000);

        // Don 2: that bai roi hoan
        orderService.assign(admin.getId(), returned.getId(), shipper.getId());
        orderService.pickUp(shipper.getId(), returned.getId());
        orderService.deliverFail(shipper.getId(), returned.getId(), "Khách từ chối");
        orderService.startReturn(admin.getId(), returned.getId());
        orderService.confirmReturned(shipper.getId(), returned.getId());

        assertThat(orderService.history(delivered)).hasSize(4);
        assertThat(orderService.history(returned)).hasSize(6);

        // Chua nop COD thi don giao thanh cong chua duoc doi soat
        assertThat(settlementService.readyOrders(shop.getId())).extracting(Order::getId).containsExactly(returned.getId());

        // Shipper nop tien, admin xac nhan
        assertThat(remittanceService.holdingAmount(shipper.getId())).isEqualTo(500_000);
        CodRemittance remittance = remittanceService.submit(shipper.getId(), null);
        assertThat(remittance.getTotalAmount()).isEqualTo(500_000);
        assertThat(remittanceService.holdingAmount(shipper.getId())).isZero();
        remittanceService.confirm(admin.getId(), remittance.getId());

        // Doi soat: 500.000 thu ho - (25.000 + 30.000) phi = 445.000
        Settlement settlement = settlementService.create(shop.getId(), null);
        assertThat(settlement.getOrders()).hasSize(2);
        assertThat(settlement.getTotalCod()).isEqualTo(500_000);
        assertThat(settlement.getTotalFee()).isEqualTo(55_000);
        assertThat(settlement.getPayoutAmount()).isEqualTo(445_000);
        assertThat(settlementService.readyOrders(shop.getId())).isEmpty();

        settlementService.markPaid(settlement.getId(), "FT123");
        assertThat(settlement.getPaidAt()).isNotNull();
        assertThatThrownBy(() -> settlementService.markPaid(settlement.getId(), "FT124"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectedRemittanceReleasesOrdersForResubmission() {
        Order order = createOrder(100_000, 500, deliveryWard);
        orderService.assign(admin.getId(), order.getId(), shipper.getId());
        orderService.pickUp(shipper.getId(), order.getId());
        orderService.deliverSuccess(shipper.getId(), order.getId(), 100_000);

        CodRemittance first = remittanceService.submit(shipper.getId(), null);
        remittanceService.reject(admin.getId(), first.getId(), "Thiếu tiền");
        assertThat(remittanceService.holdingAmount(shipper.getId())).isEqualTo(100_000);

        CodRemittance second = remittanceService.submit(shipper.getId(), null);
        assertThat(second.getTotalAmount()).isEqualTo(100_000);
    }

    @Test
    void shopCannotSeeAnotherShopsOrder() {
        Order order = createOrder(0, 500, deliveryWard);
        assertThatThrownBy(() -> orderService.findForShop(otherShop.getId(), order.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shipperCannotActOnOrderNotAssignedToThem() {
        Shipper other = userRepository.save(new Shipper("shipper_o", "x", "Shipper khác", "0900000004", null, null));
        Order order = createOrder(0, 500, deliveryWard);
        orderService.assign(admin.getId(), order.getId(), shipper.getId());
        assertThatThrownBy(() -> orderService.pickUp(other.getId(), order.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThat(orderService.findById(order.getId()).getStatus()).isEqualTo(OrderStatus.CHO_LAY_HANG);
    }

    @Test
    void settlementRequiresBankAccountWhenShopReceivesMoney() {
        Shop noBank = userRepository.save(new Shop("shop_nb", "x", "Không NH", "0900000005", "Shop NB", "3 Láng", pickupWard));
        OrderForm f = new OrderForm();
        f.setPickupAddress("3 Láng");
        f.setPickupWardId(pickupWard.getId());
        f.setReceiverName("A");
        f.setReceiverPhone("0911111111");
        f.setReceiverAddress("B");
        f.setDeliveryWardId(deliveryWard.getId());
        f.setItemDescription("Áo");
        f.setWeightGram(500);
        f.setCodAmount(100_000L);
        f.setServiceType(ServiceType.TIEU_CHUAN);
        Order order = orderService.createOrder(noBank.getId(), f);
        orderService.assign(admin.getId(), order.getId(), shipper.getId());
        orderService.pickUp(shipper.getId(), order.getId());
        orderService.deliverSuccess(shipper.getId(), order.getId(), 100_000);
        CodRemittance r = remittanceService.submit(shipper.getId(), null);
        remittanceService.confirm(admin.getId(), r.getId());

        assertThatThrownBy(() -> settlementService.create(noBank.getId(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tài khoản ngân hàng");
        assertThat(order.getSettlement()).isNull();
    }
}
