package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.*;

public class RegisterShopForm {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Pattern(regexp = "^[a-zA-Z0-9_.]{4,50}$", message = "Tên đăng nhập 4-50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    @NotBlank(message = "Vui lòng nhập họ tên chủ shop")
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

    @AssertTrue(message = "Mật khẩu nhập lại không khớp")
    public boolean isPasswordConfirmed() {
        return password == null || password.equals(confirmPassword);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
}
