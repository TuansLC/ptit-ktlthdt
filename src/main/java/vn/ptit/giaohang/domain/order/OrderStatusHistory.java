package vn.ptit.giaohang.domain.order;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Nhat ky moi lan don hang doi trang thai: ai thao tac, luc nao, ghi chu gi.
 */
@Entity
@Table(name = "order_status_histories")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus toStatus;

    @Column(nullable = false, length = 100)
    private String actorName;

    @Column(length = 30)
    private String actorRole;

    @Column(length = 500)
    private String note;

    private LocalDateTime createdAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(Order order, OrderStatus fromStatus, OrderStatus toStatus,
                              String actorName, String actorRole, String note) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getFromStatus() {
        return fromStatus;
    }

    public OrderStatus getToStatus() {
        return toStatus;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
