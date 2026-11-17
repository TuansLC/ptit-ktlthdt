package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.pricing.*;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.PriceConfigForm;
import vn.ptit.giaohang.repository.PriceConfigRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PricingService {

    private final PriceConfigRepository priceConfigRepository;
    private final ShippingFeeStrategyFactory strategyFactory;

    public PricingService(PriceConfigRepository priceConfigRepository, ShippingFeeStrategyFactory strategyFactory) {
        this.priceConfigRepository = priceConfigRepository;
        this.strategyFactory = strategyFactory;
    }

    /** Tinh phi: lay bang gia cua dich vu, chon strategy qua factory roi giao cho strategy tinh. */
    public FeeQuote quote(Ward pickupWard, Ward deliveryWard, int weightGram, ServiceType serviceType) {
        PriceConfig config = getConfig(serviceType);
        ShippingFeeStrategy strategy = strategyFactory.get(serviceType);
        return strategy.calculate(new FeeRequest(pickupWard, deliveryWard, weightGram, serviceType), config);
    }

    public List<PriceConfig> allConfigs() {
        return priceConfigRepository.findAll().stream()
                .sorted(Comparator.comparing(PriceConfig::getServiceType))
                .toList();
    }

    public PriceConfig getConfig(ServiceType type) {
        return priceConfigRepository.findByServiceType(type)
                .orElseThrow(() -> new NotFoundException("Chưa có bảng giá cho dịch vụ " + type.getLabel()));
    }

    @Transactional
    public void updateConfig(ServiceType type, PriceConfigForm f) {
        getConfig(type).update(f.getSameZoneFee(), f.getCrossZoneFee(), f.getIncludedWeightGram(),
                f.getStepWeightGram(), f.getExtraFeePerStep(), f.getMaxWeightGram(), f.getSurcharge());
    }
}
