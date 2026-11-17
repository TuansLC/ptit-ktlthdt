package vn.ptit.giaohang.domain.finance;

import jakarta.persistence.*;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.user.Shipper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phieu nop tien COD: shipper nop tien thu ho cua nhieu don ve cong ty, quan tri vien xac nhan.
 */
@Entity
@Table(name = "cod_remittances")
public class CodRemittance extends Voucher {

    @ManyToOne(optional = false)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    @OneToMany(mappedBy = "codRemittance")
    private List<Order> orders = new ArrayList<>();

    private long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RemittanceStatus status;

    @Column(length = 100)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    protected CodRemittance() {
    }

    public CodRemittance(Shipper shipper, List<Order> pendingOrders, String note) {
        super(note);
        if (pendingOrders.isEmpty()) {
            throw new BusinessException("Không có đơn nào có tiền thu hộ cần nộp");
        }
        this.shipper = shipper;
        this.status = RemittanceStatus.CHO_XAC_NHAN;
        for (Order order : pendingOrders) {
            if (!order.isAssignedTo(shipper)) {
                throw new BusinessException("Đơn " + order.getCode() + " không do shipper này giao");
            }
            order.attachToRemittance(this);
            orders.add(order);
            totalAmount += order.getCollectedAmount();
        }
    }

    @Override
    protected String codePrefix() {
        return "NP";
    }

    @Override
    public long getAmount() {
        return totalAmount;
    }

    @Override
    public String getStatusLabel() {
        return status.getLabel();
    }

    @Override
    public String getStatusBadge() {
        return status.getBadge();
    }

    public boolean isConfirmed() {
        return status == RemittanceStatus.DA_XAC_NHAN;
    }

    public void confirm(String reviewer) {
        requirePending();
        this.status = RemittanceStatus.DA_XAC_NHAN;
        markReviewed(reviewer);
    }

    /** Tu choi phieu: cac don duoc go ra de shipper lap phieu moi. */
    public void reject(String reviewer, String reason) {
        requirePending();
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do từ chối");
        }
        for (Order order : orders) {
            order.detachFromRemittance();
        }
        orders.clear();
        this.status = RemittanceStatus.TU_CHOI;
        appendNote("Từ chối: " + reason);
        markReviewed(reviewer);
    }

    private void requirePending() {
        if (status != RemittanceStatus.CHO_XAC_NHAN) {
            throw new BusinessException("Phiếu " + getCode() + " đã được xử lý");
        }
    }

    private void markReviewed(String reviewer) {
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public Shipper getShipper() {
        return shipper;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public RemittanceStatus getStatus() {
        return status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
