package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.zone.Ward;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findAllByOrderByNameAsc();

    List<Ward> findByZoneIsNotNullOrderByNameAsc();
}
