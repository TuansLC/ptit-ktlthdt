package vn.ptit.giaohang.domain.pricing;

public enum ServiceType {
    TIEU_CHUAN("Tiêu chuẩn", "Giao trong ngày hoặc ngày hôm sau"),
    HOA_TOC("Hỏa tốc", "Giao trong 2 giờ, chỉ áp dụng khi lấy và giao trong cùng một vùng");

    private final String label;
    private final String description;

    ServiceType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
