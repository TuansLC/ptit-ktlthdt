package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.*;
import vn.ptit.giaohang.domain.user.Shop;

public class ShopProfileForm {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
    private String phone;

    @NotBlank(message = "Vui lòng nhập tên shop")
    @Size(max = 150)
    private String shopName;

    @NotBlank(message = "Vui lòng nhập địa chỉ lấy hàng")
    @Size(max = 255)
    private String address;

    @NotNull(message = "Vui lòng chọn phường")
    private Long wardId;

    @Size(max = 100)
    private String bankName;

    @Pattern(regexp = "^\\d{0,30}$", message = "Số tài khoản chỉ gồm chữ số")
    private String bankAccountNo;

    @Size(max = 100)
    private String bankAccountHolder;

    public static ShopProfileForm from(Shop shop) {
        ShopProfileForm form = new ShopProfileForm();
        form.fullName = shop.getFullName();
        form.phone = shop.getPhone();
        form.shopName = shop.getShopName();
        form.address = shop.getAddress();
        form.wardId = shop.getWard().getId();
        form.bankName = shop.getBankName();
        form.bankAccountNo = shop.getBankAccountNo();
        form.bankAccountHolder = shop.getBankAccountHolder();
        return form;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder;
    }

    public void setBankAccountHolder(String bankAccountHolder) {
        this.bankAccountHolder = bankAccountHolder;
    }
}
