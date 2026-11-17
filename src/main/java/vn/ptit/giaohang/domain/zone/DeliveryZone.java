package vn.ptit.giaohang.domain.zone;

import jakarta.persistence.*;

/**
 * Vung giao hang do quan tri vien dinh nghia, vi du "Vung 1 - Trung tam".
 * Phi van chuyen tinh theo viec dia chi lay va dia chi giao co cung vung hay khong.
 */
@Entity
@Table(name = "delivery_zones")
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    protected DeliveryZone() {
    }

    public DeliveryZone(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public boolean sameAs(DeliveryZone other) {
        if (other == null) {
            return false;
        }
        return this == other || (id != null && id.equals(other.id));
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
