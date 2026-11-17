package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;

import java.util.Optional;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {

    Optional<PriceConfig> findByServiceType(ServiceType serviceType);
}
