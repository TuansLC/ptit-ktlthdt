package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.finance.SettlementStatus;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByShopIdOrderByCreatedAtDesc(Long shopId);

    List<Settlement> findAllByOrderByCreatedAtDesc();

    long countByStatus(SettlementStatus status);

    /** Chi dung khi tao du lieu mau. */
    @Modifying
    @Query("update Settlement s set s.code = :code, s.createdAt = :at, s.paidAt = :at where s.id = :id")
    int backdate(@Param("id") Long id, @Param("code") String code, @Param("at") LocalDateTime at);
}
