package vn.ptit.giaohang.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.ptit.giaohang.domain.user.Role;
import vn.ptit.giaohang.domain.user.User;

import java.util.Collection;
import java.util.List;

/**
 * Thong tin nguoi dung dang dang nhap, luu trong session.
 * Chi giu id va thong tin hien thi, khong giu entity de tranh du lieu cu.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final boolean active;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.active = user.isActive();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
