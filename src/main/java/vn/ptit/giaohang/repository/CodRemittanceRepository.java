package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.finance.RemittanceStatus;
import vn.ptit.giaohang.domain.user.Shipper;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CodRemittanceRepository extends JpaRepository<CodRemittance, Long> {

    List<CodRemittance> findByShipperOrderByCreatedAtDesc(Shipper shipper);

    List<CodRemittance> findAllByOrderByCreatedAtDesc();

    long countByStatus(RemittanceStatus status);

    /** Chi dung khi tao du lieu mau. */
    @Modifying
    @Query("update CodRemittance r set r.code = :code, r.createdAt = :at, r.reviewedAt = :at where r.id = :id")
    int backdate(@Param("id") Long id, @Param("code") String code, @Param("at") LocalDateTime at);
}
