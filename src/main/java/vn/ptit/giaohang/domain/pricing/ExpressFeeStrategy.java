package vn.ptit.giaohang.domain.pricing;

import org.springframework.stereotype.Component;
import vn.ptit.giaohang.common.BusinessException;

/**
 * Dich vu hoa toc: chi nhan don lay va giao trong cung mot vung, cong them phu phi co dinh.
 */
@Component
public class ExpressFeeStrategy extends AbstractShippingFeeStrategy {

    @Override
    public ServiceType getServiceType() {
        return ServiceType.HOA_TOC;
    }

    @Override
    protected void validateSpecific(FeeRequest request, PriceConfig config) {
        if (!request.isSameZone()) {
            throw new BusinessException("Dịch vụ Hỏa tốc chỉ áp dụng khi lấy hàng và giao hàng trong cùng một vùng");
        }
    }

    @Override
    protected long surcharge(FeeRequest request, PriceConfig config) {
        return config.getSurcharge();
    }
}
