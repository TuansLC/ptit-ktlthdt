package vn.ptit.giaohang.domain.user;

import jakarta.persistence.*;
import vn.ptit.giaohang.domain.zone.DeliveryZone;

/**
 * Shipper lay va giao hang. Moi shipper phu trach mot vung giao hang (co the de trong).
 */
@Entity
@Table(name = "shippers")
public class Shipper extends User {

    @Column(length = 20)
    private String vehiclePlate;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private DeliveryZone zone;

    protected Shipper() {
    }

    public Shipper(String username, String passwordHash, String fullName, String phone,
                   String vehiclePlate, DeliveryZone zone) {
        super(username, passwordHash, fullName, phone);
        this.vehiclePlate = vehiclePlate;
        this.zone = zone;
    }

    @Override
    public Role getRole() {
        return Role.SHIPPER;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public DeliveryZone getZone() {
        return zone;
    }
}
