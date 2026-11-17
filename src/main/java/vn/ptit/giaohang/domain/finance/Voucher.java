package vn.ptit.giaohang.domain.finance;

import jakarta.persistence.*;
import vn.ptit.giaohang.common.CodeGenerator;

import java.time.LocalDateTime;

/**
 * Lop truu tuong cho cac loai phieu tien: phieu nop COD va phieu doi soat.
 * Phan chung (ma phieu, ngay tao, cach sinh ma) nam o day; lop con quy dinh tien to ma,
 * so tien va trang thai rieng.
 */
@MappedSuperclass
public abstract class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String code;

    private LocalDateTime createdAt;

    @Column(length = 500)
    private String note;

    protected Voucher() {
    }

    protected Voucher(String note) {
        this.note = note;
    }

    /** Tien to ma phieu, vi du "NP" (nop tien) hoac "DS" (doi soat). */
    protected abstract String codePrefix();

    /** So tien chinh cua phieu. */
    public abstract long getAmount();

    public abstract String getStatusLabel();

    public abstract String getStatusBadge();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** Goi sau khi luu lan dau, khi da co id. */
    public void assignCode() {
        if (code == null) {
            code = CodeGenerator.generate(codePrefix(), id);
        }
    }

    protected void appendNote(String extra) {
        if (extra == null || extra.isBlank()) {
            return;
        }
        note = (note == null || note.isBlank()) ? extra.trim() : note + " | " + extra.trim();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getNote() {
        return note;
    }
}
