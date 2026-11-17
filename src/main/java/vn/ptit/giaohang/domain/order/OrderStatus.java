package vn.ptit.giaohang.domain.order;

/**
 * Trang thai don hang. Moi gia tri gan voi mot doi tuong OrderState xu ly hanh vi cua trang thai do.
 * Gia tri enum duoc luu xuong DB; doi tuong state khong luu, chi la hanh vi.
 */
public enum OrderStatus {
    CHO_PHAN_CONG("Chờ phân công", "secondary", new WaitingAssignmentState()),
    CHO_LAY_HANG("Chờ lấy hàng", "info", new WaitingPickupState()),
    DANG_GIAO("Đang giao", "primary", new DeliveringState()),
    GIAO_THAT_BAI("Giao thất bại", "warning", new FailedDeliveryState()),
    DANG_HOAN("Đang hoàn", "dark", new ReturningState()),
    GIAO_THANH_CONG("Giao thành công", "success", new FinalState()),
    DA_HOAN("Đã hoàn", "danger", new FinalState()),
    DA_HUY("Đã hủy", "light", new FinalState());

    private final String label;
    private final String badge;
    private final OrderState state;

    OrderStatus(String label, String badge, OrderState state) {
        this.label = label;
        this.badge = badge;
        this.state = state;
    }

    public String getLabel() {
        return label;
    }

    /** Mau Bootstrap cua nhan trang thai. */
    public String getBadge() {
        return badge;
    }

    public OrderState getState() {
        return state;
    }

    public boolean isFinal() {
        return this == GIAO_THANH_CONG || this == DA_HOAN || this == DA_HUY;
    }
}
