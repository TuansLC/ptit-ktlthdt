package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.zone.DeliveryZone;

import java.util.List;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {

    List<DeliveryZone> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);
}
