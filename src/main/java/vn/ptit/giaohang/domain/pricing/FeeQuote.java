package vn.ptit.giaohang.domain.pricing;

/**
 * Ket qua tinh phi, giu chi tiet tung thanh phan de hien thi cho shop.
 */
public record FeeQuote(ServiceType serviceType, boolean sameZone, long baseFee, long weightFee, long surcharge) {

    public long total() {
        return baseFee + weightFee + surcharge;
    }

    public String routeLabel() {
        return sameZone ? "Cùng vùng" : "Khác vùng";
    }
}
