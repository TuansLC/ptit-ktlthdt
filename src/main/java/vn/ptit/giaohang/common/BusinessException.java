package vn.ptit.giaohang.common;

/**
 * Loi vi pham quy tac nghiep vu. Thong diep duoc hien thi truc tiep cho nguoi dung.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
