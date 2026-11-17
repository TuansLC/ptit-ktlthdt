package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.*;

public class ShipperForm {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Pattern(regexp = "^[a-zA-Z0-9_.]{4,50}$", message = "Tên đăng nhập 4-50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới")
    private String username;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
    private String phone;

    @Size(max = 20)
    private String vehiclePlate;

    private Long zoneId;

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

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }
}
