package vn.ptit.giaohang.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Quan tri vien, kiem vai tro dieu phoi don hang.
 */
@Entity
@Table(name = "admins")
public class Admin extends User {

    protected Admin() {
    }

    public Admin(String username, String passwordHash, String fullName, String phone) {
        super(username, passwordHash, fullName, phone);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
