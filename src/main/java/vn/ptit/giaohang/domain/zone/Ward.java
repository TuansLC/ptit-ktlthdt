package vn.ptit.giaohang.domain.zone;

import jakarta.persistence.*;

/**
 * Phuong cua Ha Noi theo dia gioi hanh chinh 2 cap (tu 01/7/2025).
 * Phuong chua duoc gan vung giao hang nghia la nam ngoai pham vi phuc vu.
 */
@Entity
@Table(name = "wards")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private DeliveryZone zone;

    protected Ward() {
    }

    public Ward(String name, DeliveryZone zone) {
        this.name = name;
        this.zone = zone;
    }

    public boolean isServed() {
        return zone != null;
    }

    public void assignZone(DeliveryZone zone) {
        this.zone = zone;
    }

    public void removeFromService() {
        this.zone = null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeliveryZone getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return "Phường " + name;
    }
}
