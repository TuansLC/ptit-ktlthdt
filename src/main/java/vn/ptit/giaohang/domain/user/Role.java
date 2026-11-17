package vn.ptit.giaohang.domain.user;

public enum Role {
    SHOP("Chủ shop", "/shop"),
    SHIPPER("Shipper", "/shipper"),
    ADMIN("Quản trị viên", "/admin");

    private final String label;
    private final String homeUrl;

    Role(String label, String homeUrl) {
        this.label = label;
        this.homeUrl = homeUrl;
    }

    public String getLabel() {
        return label;
    }

    /** Trang dau tien sau khi dang nhap. */
    public String getHomeUrl() {
        return homeUrl;
    }

    /** Ten quyen theo quy uoc cua Spring Security. */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
