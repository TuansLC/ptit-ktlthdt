package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.finance.SettlementStatus;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByShopIdOrderByCreatedAtDesc(Long shopId);

    List<Settlement> findAllByOrderByCreatedAtDesc();

    long countByStatus(SettlementStatus status);
}
