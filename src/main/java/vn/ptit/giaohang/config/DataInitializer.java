package vn.ptit.giaohang.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Admin;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.repository.*;
import vn.ptit.giaohang.service.OrderService;
import vn.ptit.giaohang.service.RemittanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tao du lieu mau khi CSDL con trong: vung giao hang, 51 phuong Ha Noi, bang gia, tai khoan va mot so don.
 * Don mau duoc tao qua OrderService nen co day du nhat ky trang thai nhu don that.
 * Mat khau mau chi dung cho demo, can doi khi trien khai that.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Phuong thuoc khu vuc trung tam (cac quan cu Hoan Kiem, Ba Dinh, Dong Da, Hai Ba Trung, Cau Giay, Thanh Xuan, Tay Ho). */
    private static final List<String> ZONE_1 = List.of(
            "Hoàn Kiếm", "Cửa Nam", "Ba Đình", "Ngọc Hà", "Giảng Võ", "Hai Bà Trưng", "Vĩnh Tuy", "Bạch Mai",
            "Đống Đa", "Kim Liên", "Văn Miếu – Quốc Tử Giám", "Láng", "Ô Chợ Dừa", "Hồng Hà",
            "Thanh Xuân", "Khương Đình", "Phương Liệt", "Cầu Giấy", "Nghĩa Đô", "Yên Hòa", "Tây Hồ", "Phú Thượng");

    /** Phuong noi thanh mo rong (cac quan cu Hoang Mai, Long Bien, Nam/Bac Tu Liem, Ha Dong). */
    private static final List<String> ZONE_2 = List.of(
            "Lĩnh Nam", "Hoàng Mai", "Vĩnh Hưng", "Tương Mai", "Định Công", "Hoàng Liệt", "Yên Sở",
            "Tây Tựu", "Phú Diễn", "Xuân Đỉnh", "Đông Ngạc", "Thượng Cát", "Từ Liêm", "Xuân Phương", "Tây Mỗ",
            "Đại Mỗ", "Long Biên", "Bồ Đề", "Việt Hưng", "Phúc Lợi", "Hà Đông", "Dương Nội", "Yên Nghĩa",
            "Phú Lương", "Kiến Hưng", "Thanh Liệt");

    /** Phuong o xa trung tam, mac dinh chua phuc vu (quan tri vien co the gan vung sau). */
    private static final List<String> NOT_SERVED = List.of("Chương Mỹ", "Sơn Tây", "Tùng Thiện");

    private final UserRepository userRepository;
    private final DeliveryZoneRepository zoneRepository;
    private final WardRepository wardRepository;
    private final PriceConfigRepository priceConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;
    private final RemittanceService remittanceService;

    private final Map<String, Ward> wards = new HashMap<>();

    public DataInitializer(UserRepository userRepository, DeliveryZoneRepository zoneRepository,
                           WardRepository wardRepository, PriceConfigRepository priceConfigRepository,
                           PasswordEncoder passwordEncoder, OrderService orderService,
                           RemittanceService remittanceService) {
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.wardRepository = wardRepository;
        this.priceConfigRepository = priceConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
        this.remittanceService = remittanceService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("CSDL trống, đang tạo dữ liệu mẫu...");

        DeliveryZone z1 = zoneRepository.save(new DeliveryZone("V1", "Vùng 1 - Trung tâm",
                "Các phường thuộc khu vực Hoàn Kiếm, Ba Đình, Đống Đa, Hai Bà Trưng, Cầu Giấy, Thanh Xuân, Tây Hồ cũ"));
        DeliveryZone z2 = zoneRepository.save(new DeliveryZone("V2", "Vùng 2 - Nội thành mở rộng",
                "Các phường thuộc khu vực Hoàng Mai, Long Biên, Nam Từ Liêm, Bắc Từ Liêm, Hà Đông cũ"));
        ZONE_1.forEach(name -> saveWard(name, z1));
        ZONE_2.forEach(name -> saveWard(name, z2));
        NOT_SERVED.forEach(name -> saveWard(name, null));

        priceConfigRepository.save(new PriceConfig(ServiceType.TIEU_CHUAN,
                20_000, 30_000, 2_000, 500, 2_500, 30_000, 0));
        priceConfigRepository.save(new PriceConfig(ServiceType.HOA_TOC,
                35_000, 35_000, 2_000, 500, 5_000, 5_000, 15_000));

        String demoPassword = passwordEncoder.encode("123456");
        Admin admin = userRepository.save(new Admin("admin", demoPassword, "Nguyễn Quản Trị", "0901000001"));

        Shop hoa = new Shop("shop_hoa", demoPassword, "Trần Thị Hoa", "0912000001",
                "Hoa Linh Fashion", "25 Trần Duy Hưng", wards.get("Yên Hòa"));
        hoa.updateBankAccount("Vietcombank", "0011004455667", "TRAN THI HOA");
        userRepository.save(hoa);

        Shop minh = new Shop("shop_minh", demoPassword, "Lê Văn Minh", "0912000002",
                "Minh Tech Phụ Kiện", "102 Quang Trung", wards.get("Hà Đông"));
        minh.updateBankAccount("Techcombank", "1903555888999", "LE VAN MINH");
        userRepository.save(minh);

        Shipper an = userRepository.save(new Shipper("shipper_an", demoPassword, "Phạm Văn An", "0983000001",
                "29B1-123.45", z1));
        Shipper binh = userRepository.save(new Shipper("shipper_binh", demoPassword, "Đỗ Quang Bình", "0983000002",
                "29H1-678.90", z2));

        seedOrders(admin, hoa, minh, an, binh);
        log.info("Đã tạo dữ liệu mẫu. Tài khoản: admin / shop_hoa / shop_minh / shipper_an / shipper_binh, mật khẩu 123456");
    }

    private void saveWard(String name, DeliveryZone zone) {
        wards.put(name, wardRepository.save(new Ward(name, zone)));
    }

    private void seedOrders(Admin admin, Shop hoa, Shop minh, Shipper an, Shipper binh) {
        // 2 don cho phan cong
        create(hoa, "Nguyễn Thu Trang", "0977111222", "18 Nguyễn Chí Thanh", "Láng", "Váy công sở", 600, 450_000, ServiceType.TIEU_CHUAN);
        create(minh, "Vũ Đức Long", "0977333444", "5 Lê Trọng Tấn", "Dương Nội", "Tai nghe bluetooth", 300, 0, ServiceType.TIEU_CHUAN);

        // Cho lay hang
        Order o3 = create(hoa, "Hoàng Minh Châu", "0977555666", "200 Xã Đàn", "Kim Liên", "Áo khoác", 900, 690_000, ServiceType.HOA_TOC);
        orderService.assign(admin.getId(), o3.getId(), an.getId());

        // Dang giao
        Order o4 = create(minh, "Bùi Thị Lan", "0966111222", "12 Nguyễn Văn Cừ", "Bồ Đề", "Sạc dự phòng 20000mAh", 2_600, 520_000, ServiceType.TIEU_CHUAN);
        orderService.assign(admin.getId(), o4.getId(), binh.getId());
        orderService.pickUp(binh.getId(), o4.getId());

        // 2 don giao thanh cong, shipper da nop tien va duoc xac nhan -> san sang doi soat
        Order o5 = create(hoa, "Đặng Quốc Huy", "0966333444", "35 Chùa Bộc", "Kim Liên", "Quần jean", 700, 380_000, ServiceType.TIEU_CHUAN);
        Order o6 = create(hoa, "Ngô Thanh Hà", "0966555666", "88 Kim Mã", "Giảng Võ", "Đầm dạ hội", 1_200, 1_250_000, ServiceType.TIEU_CHUAN);
        for (Order o : List.of(o5, o6)) {
            orderService.assign(admin.getId(), o.getId(), an.getId());
            orderService.pickUp(an.getId(), o.getId());
            orderService.deliverSuccess(an.getId(), o.getId(), o.getCodAmount());
        }
        var remittance = remittanceService.submit(an.getId(), "Nộp tiền cuối ca sáng");
        remittanceService.confirm(admin.getId(), remittance.getId());

        // Giao thanh cong nhung shipper chua nop tien
        Order o7 = create(minh, "Trịnh Văn Nam", "0955111222", "45 Nguyễn Trãi", "Thanh Xuân", "Chuột không dây", 250, 299_000, ServiceType.TIEU_CHUAN);
        orderService.assign(admin.getId(), o7.getId(), an.getId());
        orderService.pickUp(an.getId(), o7.getId());
        orderService.deliverSuccess(an.getId(), o7.getId(), o7.getCodAmount());

        // Giao that bai 1 lan
        Order o8 = create(minh, "Lý Thị Mai", "0955333444", "7 Phố Nhổn", "Tây Tựu", "Bàn phím cơ", 1_100, 850_000, ServiceType.TIEU_CHUAN);
        orderService.assign(admin.getId(), o8.getId(), binh.getId());
        orderService.pickUp(binh.getId(), o8.getId());
        orderService.deliverFail(binh.getId(), o8.getId(), "Khách không nghe máy");

        // Da hoan ve shop sau 3 lan that bai
        Order o9 = create(hoa, "Cao Văn Tú", "0944111222", "60 Ngọc Lâm", "Bồ Đề", "Áo sơ mi", 400, 320_000, ServiceType.TIEU_CHUAN);
        orderService.assign(admin.getId(), o9.getId(), binh.getId());
        orderService.pickUp(binh.getId(), o9.getId());
        orderService.deliverFail(binh.getId(), o9.getId(), "Khách hẹn hôm sau");
        orderService.retry(admin.getId(), o9.getId(), binh.getId());
        orderService.deliverFail(binh.getId(), o9.getId(), "Khách không nghe máy");
        orderService.retry(admin.getId(), o9.getId(), binh.getId());
        orderService.deliverFail(binh.getId(), o9.getId(), "Khách từ chối nhận hàng");
        orderService.startReturn(admin.getId(), o9.getId());
        orderService.confirmReturned(binh.getId(), o9.getId());
    }

    private Order create(Shop shop, String receiverName, String receiverPhone, String receiverAddress,
                         String wardName, String item, int weightGram, long cod, ServiceType serviceType) {
        OrderForm f = new OrderForm();
        f.setPickupAddress(shop.getAddress());
        f.setPickupWardId(shop.getWard().getId());
        f.setReceiverName(receiverName);
        f.setReceiverPhone(receiverPhone);
        f.setReceiverAddress(receiverAddress);
        f.setDeliveryWardId(wards.get(wardName).getId());
        f.setItemDescription(item);
        f.setWeightGram(weightGram);
        f.setCodAmount(cod);
        f.setServiceType(serviceType);
        return orderService.createOrder(shop.getId(), f);
    }
}
