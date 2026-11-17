package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.user.Shop;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findAllByOrderByShopNameAsc();
}
