package vn.ptit.giaohang.domain.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Lop truu tuong cho moi tai khoan trong he thong.
 * Moi lop con (Shop, Shipper, Admin) tu khai bao vai tro cua minh qua getRole().
 * Anh xa JPA theo chien luoc JOINED: bang users chua thuoc tinh chung,
 * moi lop con co bang rieng chua thuoc tinh rieng.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    private boolean active = true;

    private LocalDateTime createdAt;

    protected User() {
    }

    protected User(String username, String passwordHash, String fullName, String phone) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
    }

    /** Da hinh: moi loai tai khoan tra ve vai tro rieng. */
    public abstract Role getRole();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void updateContact(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
