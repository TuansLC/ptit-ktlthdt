package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.ptit.giaohang.domain.user.Shipper;

public class ShipperEditForm {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
    private String phone;

    @Size(max = 20)
    private String vehiclePlate;

    private Long zoneId;

    public static ShipperEditForm from(Shipper s) {
        ShipperEditForm f = new ShipperEditForm();
        f.fullName = s.getFullName();
        f.phone = s.getPhone();
        f.vehiclePlate = s.getVehiclePlate();
        f.zoneId = s.getZone() == null ? null : s.getZone().getId();
        return f;
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
