package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.*;
import vn.ptit.giaohang.domain.pricing.ServiceType;

public class OrderForm {

    @NotBlank(message = "Vui lòng nhập địa chỉ lấy hàng")
    @Size(max = 255)
    private String pickupAddress;

    @NotNull(message = "Vui lòng chọn phường lấy hàng")
    private Long pickupWardId;

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    @Size(max = 100)
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại người nhận")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
    private String receiverPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    @Size(max = 255)
    private String receiverAddress;

    @NotNull(message = "Vui lòng chọn phường giao hàng")
    private Long deliveryWardId;

    @NotBlank(message = "Vui lòng nhập nội dung hàng hóa")
    @Size(max = 255)
    private String itemDescription;

    @NotNull(message = "Vui lòng nhập khối lượng")
    @Min(value = 1, message = "Khối lượng phải lớn hơn 0")
    @Max(value = 50000, message = "Khối lượng tối đa 50 kg")
    private Integer weightGram;

    @NotNull(message = "Vui lòng nhập tiền thu hộ (nhập 0 nếu không thu hộ)")
    @Min(value = 0, message = "Tiền thu hộ không được âm")
    @Max(value = 50_000_000, message = "Tiền thu hộ tối đa 50.000.000 đ")
    private Long codAmount;

    @NotNull(message = "Vui lòng chọn dịch vụ")
    private ServiceType serviceType = ServiceType.TIEU_CHUAN;

    @Size(max = 500)
    private String note;

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Long getPickupWardId() {
        return pickupWardId;
    }

    public void setPickupWardId(Long pickupWardId) {
        this.pickupWardId = pickupWardId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public Long getDeliveryWardId() {
        return deliveryWardId;
    }

    public void setDeliveryWardId(Long deliveryWardId) {
        this.deliveryWardId = deliveryWardId;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public Integer getWeightGram() {
        return weightGram;
    }

    public void setWeightGram(Integer weightGram) {
        this.weightGram = weightGram;
    }

    public Long getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(Long codAmount) {
        this.codAmount = codAmount;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
