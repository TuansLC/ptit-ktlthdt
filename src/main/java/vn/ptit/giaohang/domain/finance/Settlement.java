package vn.ptit.giaohang.domain.finance;

import jakarta.persistence.*;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.user.Shop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phieu doi soat voi shop.
 * Tien tra shop = tong tien thu ho cua don giao thanh cong - tong phi van chuyen cua moi don trong phieu.
 * Neu ket qua am, shop la ben phai tra tien cho cong ty.
 * Thong tin ngan hang duoc chup lai tai thoi diem lap phieu de shop doi tai khoan sau nay khong lam sai lich su.
 */
@Entity
@Table(name = "settlements")
public class Settlement extends Voucher {

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "settlement")
    private List<Order> orders = new ArrayList<>();

    private long totalCod;
    private long totalFee;
    private long payoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(length = 100)
    private String bankName;

    @Column(length = 30)
    private String bankAccountNo;

    @Column(length = 100)
    private String bankAccountHolder;

    @Column(length = 100)
    private String paymentRef;

    private LocalDateTime paidAt;

    protected Settlement() {
    }

    public Settlement(Shop shop, List<Order> readyOrders, String note) {
        super(note);
        if (readyOrders.isEmpty()) {
            throw new BusinessException("Shop " + shop.getShopName() + " không có đơn nào cần đối soát");
        }
        this.shop = shop;
        this.status = SettlementStatus.CHO_THANH_TOAN;
        this.bankName = shop.getBankName();
        this.bankAccountNo = shop.getBankAccountNo();
        this.bankAccountHolder = shop.getBankAccountHolder();
        // Kiem tra va tinh tien truoc, chi gan don vao phieu khi moi dieu kien deu dat
        for (Order order : readyOrders) {
            if (!order.belongsTo(shop)) {
                throw new BusinessException("Đơn " + order.getCode() + " không thuộc shop này");
            }
            if (!order.isReadyForSettlement()) {
                throw new BusinessException("Đơn " + order.getCode() + " chưa đủ điều kiện đối soát");
            }
            totalCod += order.settlementCod();
            totalFee += order.getShippingFee();
        }
        this.payoutAmount = totalCod - totalFee;
        if (payoutAmount > 0 && !shop.hasBankAccount()) {
            throw new BusinessException("Shop " + shop.getShopName()
                    + " chưa cập nhật tài khoản ngân hàng nhận tiền đối soát");
        }
        for (Order order : readyOrders) {
            order.attachToSettlement(this);
            orders.add(order);
        }
    }

    @Override
    protected String codePrefix() {
        return "DS";
    }

    @Override
    public long getAmount() {
        return payoutAmount;
    }

    @Override
    public String getStatusLabel() {
        return status.getLabel();
    }

    @Override
    public String getStatusBadge() {
        return status.getBadge();
    }

    public void markPaid(String paymentRef) {
        if (status == SettlementStatus.DA_THANH_TOAN) {
            throw new BusinessException("Phiếu " + getCode() + " đã thanh toán");
        }
        if (paymentRef == null || paymentRef.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã giao dịch chuyển khoản");
        }
        this.paymentRef = paymentRef.trim();
        this.paidAt = LocalDateTime.now();
        this.status = SettlementStatus.DA_THANH_TOAN;
    }

    public boolean isShopOwing() {
        return payoutAmount < 0;
    }

    public Shop getShop() {
        return shop;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public long getTotalCod() {
        return totalCod;
    }

    public long getTotalFee() {
        return totalFee;
    }

    public long getPayoutAmount() {
        return payoutAmount;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
