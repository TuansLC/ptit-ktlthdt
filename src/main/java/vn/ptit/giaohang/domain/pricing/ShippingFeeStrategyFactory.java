package vn.ptit.giaohang.domain.pricing;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory: tra ve strategy tinh phi tuong ung voi loai dich vu khach chon.
 * Spring tu tiem tat ca cac lop cai dat ShippingFeeStrategy vao constructor.
 */
@Component
public class ShippingFeeStrategyFactory {

    private final Map<ServiceType, ShippingFeeStrategy> strategies = new EnumMap<>(ServiceType.class);

    public ShippingFeeStrategyFactory(List<ShippingFeeStrategy> strategyList) {
        for (ShippingFeeStrategy strategy : strategyList) {
            strategies.put(strategy.getServiceType(), strategy);
        }
        for (ServiceType type : ServiceType.values()) {
            if (!strategies.containsKey(type)) {
                throw new IllegalStateException("Chưa có cách tính phí cho dịch vụ " + type);
            }
        }
    }

    public ShippingFeeStrategy get(ServiceType type) {
        return strategies.get(type);
    }
}
