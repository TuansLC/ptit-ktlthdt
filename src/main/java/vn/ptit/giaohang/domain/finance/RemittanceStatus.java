package vn.ptit.giaohang.domain.finance;

public enum RemittanceStatus {
    CHO_XAC_NHAN("Chờ xác nhận", "warning"),
    DA_XAC_NHAN("Đã xác nhận", "success"),
    TU_CHOI("Từ chối", "danger");

    private final String label;
    private final String badge;

    RemittanceStatus(String label, String badge) {
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
