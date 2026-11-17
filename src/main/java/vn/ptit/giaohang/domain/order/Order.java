package vn.ptit.giaohang.domain.order;

import jakarta.persistence.*;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.CodeGenerator;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.pricing.FeeQuote;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.user.User;
import vn.ptit.giaohang.domain.zone.Ward;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Don giao hang.
 * Dong goi: trang thai, shipper va tien thu ho chi thay doi qua cac phuong thuc nghiep vu ben duoi.
 * Cac phuong thuc doi trang thai uy quyen cho doi tuong OrderState hien tai (State pattern).
 * Cac setter noi bo de package-private de chi lop trang thai trong cung package goi duoc.
 */
@Entity
@Table(name = "orders")
public class Order {

    public static final int MAX_DELIVERY_ATTEMPTS = 3;
    private static final String CODE_PREFIX = "GH";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String code;

    @Version
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    // ---- Thong tin lay hang ----
    @Column(nullable = false)
    private String pickupAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_ward_id")
    private Ward pickupWard;

    // ---- Thong tin nguoi nhan ----
    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false)
    private String receiverAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "delivery_ward_id")
    private Ward deliveryWard;

    // ---- Hang hoa va tien ----
    @Column(nullable = false)
    private String itemDescription;

    private int weightGram;

    /** Tien shipper thu ho tu nguoi nhan (0 neu khong thu ho). */
    private long codAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType serviceType;

    /** Phi van chuyen, tru vao tien doi soat cua shop. */
    private long shippingFee;

    @Column(length = 500)
    private String note;

    // ---- Trang thai giao hang ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    private int failedAttempts;

    @Column(length = 500)
    private String lastFailReason;

    /** So tien shipper da thu thuc te khi giao thanh cong. */
    private long collectedAmount;

    private LocalDateTime deliveredAt;

    // ---- Doi soat ----
    @ManyToOne
    @JoinColumn(name = "cod_remittance_id")
    private CodRemittance codRemittance;

    @ManyToOne
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Order() {
    }

    public Order(Shop shop, String pickupAddress, Ward pickupWard,
                 String receiverName, String receiverPhone, String receiverAddress, Ward deliveryWard,
                 String itemDescription, int weightGram, long codAmount, FeeQuote fee, String note) {
        if (codAmount < 0) {
            throw new BusinessException("Tiền thu hộ không được âm");
        }
        this.shop = shop;
        this.pickupAddress = pickupAddress;
        this.pickupWard = pickupWard;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
        this.deliveryWard = deliveryWard;
        this.itemDescription = itemDescription;
        this.weightGram = weightGram;
        this.codAmount = codAmount;
        this.serviceType = fee.serviceType();
        this.shippingFee = fee.total();
        this.note = note;
        this.status = OrderStatus.CHO_PHAN_CONG;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Goi sau khi luu lan dau, khi da co id. */
    public void assignCode() {
        if (code == null) {
            code = CodeGenerator.generate(CODE_PREFIX, id);
        }
    }

    // ================= Thao tac nghiep vu (uy quyen cho State) =================

    private OrderState state() {
        return status.getState();
    }

    public Set<OrderAction> allowedActions() {
        return state().allowedActions(this);
    }

    public boolean allows(OrderAction action) {
        return state().allows(this, action);
    }

    public void assignShipper(Shipper shipper) {
        state().assignShipper(this, shipper);
    }

    public void cancel() {
        state().cancel(this);
    }

    public void pickUp() {
        state().pickUp(this);
    }

    public void deliverSuccess(long collectedAmount) {
        state().deliverSuccess(this, collectedAmount);
    }

    public void deliverFail(String reason) {
        state().deliverFail(this, reason);
    }

    public void retryDelivery(Shipper shipper) {
        state().retryDelivery(this, shipper);
    }

    public void startReturn() {
        state().startReturn(this);
    }

    public void confirmReturned() {
        state().confirmReturned(this);
    }

    // ================= Thay doi noi bo, chi lop State goi =================

    void moveTo(OrderStatus next) {
        this.status = next;
    }

    void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    void markDelivered(long collectedAmount) {
        this.collectedAmount = collectedAmount;
        this.deliveredAt = LocalDateTime.now();
    }

    void recordFailedAttempt(String reason) {
        this.failedAttempts++;
        this.lastFailReason = reason;
    }

    // ================= Quy tac nghiep vu =================

    public int getMaxDeliveryAttempts() {
        return MAX_DELIVERY_ATTEMPTS;
    }

    public boolean canRetryDelivery() {
        return failedAttempts < MAX_DELIVERY_ATTEMPTS;
    }

    public boolean belongsTo(Shop shop) {
        return sameUser(this.shop, shop);
    }

    public boolean isAssignedTo(Shipper shipper) {
        return sameUser(this.shipper, shipper);
    }

    private static boolean sameUser(User a, User b) {
        if (a == null || b == null) {
            return false;
        }
        return a == b || (a.getId() != null && a.getId().equals(b.getId()));
    }

    /** Shipper da thu tien nhung chua nop ve cong ty. */
    public boolean isCodPendingRemittance() {
        return status == OrderStatus.GIAO_THANH_CONG && collectedAmount > 0 && codRemittance == null;
    }

    /**
     * Don du dieu kien doi soat voi shop: da hoan, hoac da giao thanh cong va
     * tien thu ho (neu co) da duoc cong ty xac nhan nhan tu shipper.
     */
    public boolean isReadyForSettlement() {
        if (settlement != null) {
            return false;
        }
        if (status == OrderStatus.DA_HOAN) {
            return true;
        }
        if (status != OrderStatus.GIAO_THANH_CONG) {
            return false;
        }
        return collectedAmount == 0 || (codRemittance != null && codRemittance.isConfirmed());
    }

    /** Tien thu ho tra cho shop (chi tinh don giao thanh cong). */
    public long settlementCod() {
        return status == OrderStatus.GIAO_THANH_CONG ? collectedAmount : 0;
    }

    public void attachToRemittance(CodRemittance remittance) {
        if (!isCodPendingRemittance()) {
            throw new BusinessException("Đơn " + code + " không có tiền thu hộ cần nộp");
        }
        this.codRemittance = remittance;
    }

    public void detachFromRemittance() {
        this.codRemittance = null;
    }

    public void attachToSettlement(Settlement settlement) {
        if (!isReadyForSettlement()) {
            throw new BusinessException("Đơn " + code + " chưa đủ điều kiện đối soát");
        }
        this.settlement = settlement;
    }

    // ================= Getter =================

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Shop getShop() {
        return shop;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public Ward getPickupWard() {
        return pickupWard;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public Ward getDeliveryWard() {
        return deliveryWard;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getWeightGram() {
        return weightGram;
    }

    public long getCodAmount() {
        return codAmount;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public String getNote() {
        return note;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public String getLastFailReason() {
        return lastFailReason;
    }

    public long getCollectedAmount() {
        return collectedAmount;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public CodRemittance getCodRemittance() {
        return codRemittance;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
