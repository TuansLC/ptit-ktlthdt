package vn.ptit.giaohang.domain.pricing;

/**
 * Strategy pattern: moi loai dich vu co mot cach tinh phi rieng.
 * Them dich vu moi chi can them mot lop cai dat interface nay, khong sua code cu (nguyen ly Open/Closed).
 */
public interface ShippingFeeStrategy {

    /** Loai dich vu ma strategy nay phu trach. */
    ServiceType getServiceType();

    FeeQuote calculate(FeeRequest request, PriceConfig config);
}
