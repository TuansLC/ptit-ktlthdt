package vn.ptit.giaohang.config;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import vn.ptit.giaohang.common.CodeGenerator;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Admin;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.repository.CodRemittanceRepository;
import vn.ptit.giaohang.repository.OrderRepository;
import vn.ptit.giaohang.repository.OrderStatusHistoryRepository;
import vn.ptit.giaohang.repository.SettlementRepository;
import vn.ptit.giaohang.service.OrderService;
import vn.ptit.giaohang.service.RemittanceService;
import vn.ptit.giaohang.service.SettlementService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tao lich su van hanh cho du lieu mau: cac don da ket thuc trong N ngay truoc, nop COD va doi soat moi tuan.
 * <p>
 * Moi don di qua dung cac service that (tao don, phan cong, lay hang, giao, nop tien, doi soat) nen tuan theo
 * cac quy tac cua State pattern va co nhat ky trang thai day du. Vi he thong luon ghi thoi diem hien tai,
 * buoc cuoi {@link #applyBackdating()} moi doi cac moc thoi gian ve qua khu bang cau lenh update.
 * Dung so ngau nhien co dinh (seed) de lan nao tao du lieu cung ra cung mot ket qua.
 */
@Component
public class DemoHistorySeeder {

    private static final String[] RECEIVERS = {
            "Nguyễn Thị Mai", "Trần Văn Hùng", "Lê Thu Hà", "Phạm Quốc Bảo", "Hoàng Thị Lan", "Vũ Minh Đức",
            "Đặng Thùy Linh", "Bùi Văn Nam", "Đỗ Hải Yến", "Ngô Gia Huy", "Dương Thị Hồng", "Lý Thành Công",
            "Phan Ngọc Anh", "Trịnh Văn Khoa", "Mai Phương Thảo", "Hồ Đức Thắng", "Cao Thị Ngân", "Tạ Quang Vinh"};
    private static final String[] STREETS = {
            "Nguyễn Trãi", "Láng Hạ", "Kim Mã", "Xã Đàn", "Giải Phóng", "Trường Chinh", "Lê Văn Lương",
            "Hoàng Quốc Việt", "Phạm Văn Đồng", "Nguyễn Văn Cừ", "Tam Trinh", "Quang Trung", "Tố Hữu", "Lạc Long Quân"};
    private static final String[] FASHION_ITEMS = {"Áo sơ mi", "Váy liền", "Quần âu", "Áo khoác gió", "Đầm công sở", "Chân váy"};
    private static final String[] TECH_ITEMS = {"Tai nghe", "Sạc dự phòng", "Ốp lưng", "Chuột không dây", "Bàn phím", "Cáp sạc"};
    private static final String[] FAIL_REASONS = {"Khách không nghe máy", "Khách hẹn giao lại", "Sai địa chỉ / số điện thoại"};

    private record OrderTimes(Long id, LocalDateTime created, LocalDateTime finished, boolean delivered) {
    }

    private record VoucherTime(Long id, LocalDateTime at) {
    }

    private final OrderService orderService;
    private final RemittanceService remittanceService;
    private final SettlementService settlementService;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CodRemittanceRepository remittanceRepository;
    private final SettlementRepository settlementRepository;
    private final EntityManager em;

    private final List<OrderTimes> orderTimes = new ArrayList<>();
    private final List<VoucherTime> remittanceTimes = new ArrayList<>();
    private final List<VoucherTime> settlementTimes = new ArrayList<>();
    private Random rnd;

    public DemoHistorySeeder(OrderService orderService, RemittanceService remittanceService,
                             SettlementService settlementService, OrderRepository orderRepository,
                             OrderStatusHistoryRepository historyRepository, CodRemittanceRepository remittanceRepository,
                             SettlementRepository settlementRepository, EntityManager em) {
        this.orderService = orderService;
        this.remittanceService = remittanceService;
        this.settlementService = settlementService;
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.remittanceRepository = remittanceRepository;
        this.settlementRepository = settlementRepository;
        this.em = em;
    }

    /**
     * Tao don cho cac ngay [hom nay - days, hom qua]. Cuoi moi tuan (Chu nhat) va ngay cuoi cung,
     * shipper nop tien, quan tri vien xac nhan va doi soat, thanh toan cho tung shop.
     */
    public void seed(Admin admin, List<Shop> shops, List<Shipper> shippers, List<Ward> servedWards, int days) {
        rnd = new Random(20251118L);
        LocalDate today = LocalDate.now();
        for (LocalDate day = today.minusDays(days); day.isBefore(today); day = day.plusDays(1)) {
            int count = 2 + rnd.nextInt(4);
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count += 2;
            }
            for (int i = 0; i < count; i++) {
                createFinishedOrder(admin, pick(shops), shippers, servedWards, day);
            }
            boolean lastDay = day.plusDays(1).equals(today);
            if (day.getDayOfWeek() == DayOfWeek.SUNDAY || lastDay) {
                closeWeek(admin, shops, shippers, day);
            }
        }
    }

    private void createFinishedOrder(Admin admin, Shop shop, List<Shipper> shippers, List<Ward> wards, LocalDate day) {
        Ward to = pick(wards);
        boolean sameZone = shop.getWard().getZone().sameAs(to.getZone());
        ServiceType service = sameZone && rnd.nextInt(4) == 0 ? ServiceType.HOA_TOC : ServiceType.TIEU_CHUAN;
        int weight = 200 + rnd.nextInt(service == ServiceType.HOA_TOC ? 2_800 : 3_800);
        long cod = rnd.nextInt(5) == 0 ? 0 : (100 + rnd.nextInt(1_400)) * 1_000L;
        boolean fashion = shop.getShopName().contains("Fashion");
        String[] items = fashion ? FASHION_ITEMS : TECH_ITEMS;

        OrderForm f = new OrderForm();
        f.setPickupAddress(shop.getAddress());
        f.setPickupWardId(shop.getWard().getId());
        f.setReceiverName(pick(RECEIVERS));
        f.setReceiverPhone("09" + (10_000_000 + rnd.nextInt(89_999_999)));
        f.setReceiverAddress((1 + rnd.nextInt(300)) + " " + pick(STREETS));
        f.setDeliveryWardId(to.getId());
        f.setItemDescription(pick(items));
        f.setWeightGram(weight);
        f.setCodAmount(cod);
        f.setServiceType(service);
        Order order = orderService.createOrder(shop.getId(), f);

        Shipper shipper = shippers.stream().filter(s -> s.covers(to)).findFirst().orElse(shippers.get(0));
        orderService.assign(admin.getId(), order.getId(), shipper.getId());
        orderService.pickUp(shipper.getId(), order.getId());

        int outcome = rnd.nextInt(100);
        boolean delivered;
        if (outcome < 9) {
            // That bai roi hoan ve shop
            orderService.deliverFail(shipper.getId(), order.getId(), "Khách từ chối nhận hàng");
            orderService.startReturn(admin.getId(), order.getId());
            orderService.confirmReturned(shipper.getId(), order.getId());
            delivered = false;
        } else {
            if (outcome < 20) {
                // That bai mot lan, giao lai thanh cong
                orderService.deliverFail(shipper.getId(), order.getId(), pick(FAIL_REASONS));
                orderService.retry(admin.getId(), order.getId(), shipper.getId());
            }
            orderService.deliverSuccess(shipper.getId(), order.getId(), cod);
            delivered = true;
        }

        LocalDateTime finished = day.atTime(LocalTime.of(9, 0)).plusMinutes(rnd.nextInt(11 * 60));
        LocalDateTime created = finished.minusMinutes(90 + rnd.nextInt(20 * 60));
        orderTimes.add(new OrderTimes(order.getId(), created, finished, delivered));
    }

    private void closeWeek(Admin admin, List<Shop> shops, List<Shipper> shippers, LocalDate day) {
        LocalDateTime evening = day.atTime(19, 0);
        for (Shipper shipper : shippers) {
            if (!remittanceService.pendingOrders(shipper.getId()).isEmpty()) {
                CodRemittance r = remittanceService.submit(shipper.getId(), "Nộp tiền cuối tuần");
                remittanceService.confirm(admin.getId(), r.getId());
                remittanceTimes.add(new VoucherTime(r.getId(), evening));
            }
        }
        for (Shop shop : shops) {
            if (!settlementService.readyOrders(shop.getId()).isEmpty()) {
                Settlement s = settlementService.create(shop.getId(), "Đối soát tuần");
                settlementService.markPaid(s.getId(), "FT" + day.toString().replace("-", "") + s.getId());
                settlementTimes.add(new VoucherTime(s.getId(), evening.plusHours(1)));
            }
        }
    }

    /**
     * Doi moc thoi gian cua du lieu da tao ve qua khu. Goi o buoc cuoi cung, sau moi thao tac khac,
     * vi cau lenh update hang loat bo qua cac doi tuong dang nam trong persistence context.
     */
    public void applyBackdating() {
        em.flush();
        for (OrderTimes t : orderTimes) {
            orderRepository.backdate(t.id(), CodeGenerator.generate("GH", t.created().toLocalDate(), t.id()),
                    t.created(), t.finished(), t.delivered() ? t.finished() : null);
            historyRepository.backdate(t.id(), t.created(), t.finished());
        }
        for (VoucherTime t : remittanceTimes) {
            remittanceRepository.backdate(t.id(), CodeGenerator.generate("NP", t.at().toLocalDate(), t.id()), t.at());
        }
        for (VoucherTime t : settlementTimes) {
            settlementRepository.backdate(t.id(), CodeGenerator.generate("DS", t.at().toLocalDate(), t.id()), t.at());
        }
        em.clear();
        orderTimes.clear();
        remittanceTimes.clear();
        settlementTimes.clear();
    }

    private <T> T pick(List<T> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    private String pick(String[] values) {
        return values[rnd.nextInt(values.length)];
    }
}
