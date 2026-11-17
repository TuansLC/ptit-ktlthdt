package vn.ptit.giaohang.domain.user;

import jakarta.persistence.*;
import vn.ptit.giaohang.domain.zone.Ward;

/**
 * Shop gui hang. Luu dia chi lay hang mac dinh va tai khoan ngan hang nhan tien doi soat.
 */
@Entity
@Table(name = "shops")
public class Shop extends User {

    @Column(nullable = false, length = 150)
    private String shopName;

    @Column(nullable = false)
    private String address;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(length = 100)
    private String bankName;

    @Column(length = 30)
    private String bankAccountNo;

    @Column(length = 100)
    private String bankAccountHolder;

    protected Shop() {
    }

    public Shop(String username, String passwordHash, String fullName, String phone,
                String shopName, String address, Ward ward) {
        super(username, passwordHash, fullName, phone);
        this.shopName = shopName;
        this.address = address;
        this.ward = ward;
    }

    @Override
    public Role getRole() {
        return Role.SHOP;
    }

    public void updateProfile(String shopName, String address, Ward ward) {
        this.shopName = shopName;
        this.address = address;
        this.ward = ward;
    }

    public void updateBankAccount(String bankName, String bankAccountNo, String bankAccountHolder) {
        this.bankName = bankName;
        this.bankAccountNo = bankAccountNo;
        this.bankAccountHolder = bankAccountHolder;
    }

    public boolean hasBankAccount() {
        return bankAccountNo != null && !bankAccountNo.isBlank();
    }

    public String getShopName() {
        return shopName;
    }

    public String getAddress() {
        return address;
    }

    public Ward getWard() {
        return ward;
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
}
