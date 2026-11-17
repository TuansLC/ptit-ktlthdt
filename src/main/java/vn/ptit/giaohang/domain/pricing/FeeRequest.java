package vn.ptit.giaohang.domain.pricing;

import vn.ptit.giaohang.domain.zone.Ward;

/**
 * Du lieu dau vao de tinh phi van chuyen.
 */
public record FeeRequest(Ward pickupWard, Ward deliveryWard, int weightGram, ServiceType serviceType) {

    public boolean isSameZone() {
        return pickupWard.getZone() != null && pickupWard.getZone().sameAs(deliveryWard.getZone());
    }
}
