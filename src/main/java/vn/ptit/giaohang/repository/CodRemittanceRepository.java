package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.finance.RemittanceStatus;
import vn.ptit.giaohang.domain.user.Shipper;

import java.util.List;

public interface CodRemittanceRepository extends JpaRepository<CodRemittance, Long> {

    List<CodRemittance> findByShipperOrderByCreatedAtDesc(Shipper shipper);

    List<CodRemittance> findAllByOrderByCreatedAtDesc();

    long countByStatus(RemittanceStatus status);
}
