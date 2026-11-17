package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ZoneForm {

    @NotBlank(message = "Vui lòng nhập mã vùng")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã vùng 1-10 ký tự, chữ in hoa hoặc số")
    private String code;

    @NotBlank(message = "Vui lòng nhập tên vùng")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
