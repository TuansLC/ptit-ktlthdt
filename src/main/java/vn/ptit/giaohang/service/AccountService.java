package vn.ptit.giaohang.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.user.Role;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.user.User;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.RegisterShopForm;
import vn.ptit.giaohang.dto.ShipperForm;
import vn.ptit.giaohang.dto.ShopProfileForm;
import vn.ptit.giaohang.repository.ShipperRepository;
import vn.ptit.giaohang.repository.ShopRepository;
import vn.ptit.giaohang.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ShipperRepository shipperRepository;
    private final ZoneService zoneService;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, ShopRepository shopRepository,
                          ShipperRepository shipperRepository, ZoneService zoneService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.shipperRepository = shipperRepository;
        this.zoneService = zoneService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Shop registerShop(RegisterShopForm f) {
        requireUniqueUsername(f.getUsername());
        Ward ward = zoneService.getWard(f.getWardId());
        if (!ward.isServed()) {
            throw new BusinessException(ward + " nằm ngoài phạm vi phục vụ");
        }
        Shop shop = new Shop(f.getUsername(), passwordEncoder.encode(f.getPassword()), f.getFullName().trim(),
                f.getPhone(), f.getShopName().trim(), f.getAddress().trim(), ward);
        return shopRepository.save(shop);
    }

    @Transactional
    public Shipper createShipper(ShipperForm f) {
        requireUniqueUsername(f.getUsername());
        DeliveryZone zone = f.getZoneId() == null ? null : zoneService.getZone(f.getZoneId());
        Shipper shipper = new Shipper(f.getUsername(), passwordEncoder.encode(f.getPassword()),
                f.getFullName().trim(), f.getPhone(), f.getVehiclePlate(), zone);
        return shipperRepository.save(shipper);
    }

    @Transactional
    public void updateShopProfile(Long shopId, ShopProfileForm f) {
        Shop shop = getShop(shopId);
        Ward ward = zoneService.getWard(f.getWardId());
        if (!ward.isServed()) {
            throw new BusinessException(ward + " nằm ngoài phạm vi phục vụ");
        }
        shop.updateContact(f.getFullName().trim(), f.getPhone());
        shop.updateProfile(f.getShopName().trim(), f.getAddress().trim(), ward);
        shop.updateBankAccount(blankToNull(f.getBankName()), blankToNull(f.getBankAccountNo()),
                blankToNull(f.getBankAccountHolder()));
    }

    /** Khoa / mo khoa tai khoan shop hoac shipper. Khong cho khoa quan tri vien. */
    @Transactional
    public void setActive(Long userId, boolean active) {
        User user = getUser(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Không thể khóa tài khoản quản trị viên");
        }
        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
    }

    public Shop getShop(Long id) {
        return shopRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy shop"));
    }

    public Shipper getShipper(Long id) {
        return shipperRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy shipper"));
    }

    public List<Shop> allShops() {
        return shopRepository.findAllByOrderByShopNameAsc();
    }

    public List<Shipper> allShippers() {
        return shipperRepository.findAllByOrderByFullNameAsc();
    }

    public List<Shipper> activeShippers() {
        return shipperRepository.findByActiveTrueOrderByFullNameAsc();
    }

    private void requireUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Tên đăng nhập " + username + " đã được sử dụng");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
