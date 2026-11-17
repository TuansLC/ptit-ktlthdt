package vn.ptit.giaohang.domain.finance;

public enum SettlementStatus {
    CHO_THANH_TOAN("Chờ thanh toán", "warning"),
    DA_THANH_TOAN("Đã thanh toán", "success");

    private final String label;
    private final String badge;

    SettlementStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}
