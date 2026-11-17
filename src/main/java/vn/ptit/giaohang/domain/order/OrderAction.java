package vn.ptit.giaohang.domain.order;

/**
 * Cac thao tac lam thay doi trang thai don hang.
 */
public enum OrderAction {
    ASSIGN("phân công shipper"),
    CANCEL("hủy đơn"),
    PICK_UP("xác nhận đã lấy hàng"),
    DELIVER_SUCCESS("xác nhận giao thành công"),
    DELIVER_FAIL("báo giao thất bại"),
    RETRY("giao lại"),
    START_RETURN("chuyển hoàn"),
    CONFIRM_RETURNED("xác nhận đã hoàn hàng");

    private final String label;

    OrderAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
