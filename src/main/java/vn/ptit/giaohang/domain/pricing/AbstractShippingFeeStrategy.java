package vn.ptit.giaohang.domain.pricing;

import vn.ptit.giaohang.common.BusinessException;

/**
 * Template Method: co dinh trinh tu tinh phi (kiem tra -> cuoc co ban -> cuoc vuot can -> phu phi).
 * Lop con chi ghi de cac buoc khac biet: validateSpecific() va surcharge().
 */
public abstract class AbstractShippingFeeStrategy implements ShippingFeeStrategy {

    @Override
    public final FeeQuote calculate(FeeRequest request, PriceConfig config) {
        validateCommon(request, config);
        validateSpecific(request, config);

        boolean sameZone = request.isSameZone();
        long baseFee = sameZone ? config.getSameZoneFee() : config.getCrossZoneFee();
        long weightFee = weightFee(request.weightGram(), config);
        return new FeeQuote(getServiceType(), sameZone, baseFee, weightFee, surcharge(request, config));
    }

    private void validateCommon(FeeRequest request, PriceConfig config) {
        if (request.pickupWard() == null || request.deliveryWard() == null) {
            throw new BusinessException("Vui lòng chọn phường lấy hàng và phường giao hàng");
        }
        if (!request.pickupWard().isServed()) {
            throw new BusinessException(request.pickupWard() + " nằm ngoài phạm vi phục vụ");
        }
        if (!request.deliveryWard().isServed()) {
            throw new BusinessException(request.deliveryWard() + " nằm ngoài phạm vi phục vụ");
        }
        if (request.weightGram() <= 0) {
            throw new BusinessException("Khối lượng phải lớn hơn 0");
        }
        if (request.weightGram() > config.getMaxWeightGram()) {
            throw new BusinessException("Dịch vụ " + getServiceType().getLabel() + " chỉ nhận hàng tối đa "
                    + config.getMaxWeightGram() + " g");
        }
    }

    /** Cuoc vuot can: moi buoc (lam tron len) vuot qua khoi luong co ban tinh them mot muc phi. */
    protected long weightFee(int weightGram, PriceConfig config) {
        int over = weightGram - config.getIncludedWeightGram();
        if (over <= 0) {
            return 0;
        }
        int steps = (over + config.getStepWeightGram() - 1) / config.getStepWeightGram();
        return steps * config.getExtraFeePerStep();
    }

    /** Hook: kiem tra rieng cua tung dich vu. Mac dinh khong kiem tra gi them. */
    protected void validateSpecific(FeeRequest request, PriceConfig config) {
    }

    /** Hook: phu phi rieng cua tung dich vu. */
    protected abstract long surcharge(FeeRequest request, PriceConfig config);
}
