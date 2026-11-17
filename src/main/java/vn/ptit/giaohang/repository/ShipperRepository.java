package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.user.Shipper;

import java.util.List;

public interface ShipperRepository extends JpaRepository<Shipper, Long> {

    List<Shipper> findAllByOrderByFullNameAsc();

    List<Shipper> findByActiveTrueOrderByFullNameAsc();
}
