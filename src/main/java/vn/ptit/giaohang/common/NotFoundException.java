package vn.ptit.giaohang.common;

/**
 * Khong tim thay du lieu, hoac nguoi dung khong co quyen xem du lieu do.
 * Dung chung mot loi cho ca hai truong hop de khong lo thong tin cua nguoi khac.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
