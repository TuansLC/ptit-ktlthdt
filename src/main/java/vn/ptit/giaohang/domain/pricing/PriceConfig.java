package vn.ptit.giaohang.domain.pricing;

import jakarta.persistence.*;
import vn.ptit.giaohang.common.BusinessException;

/**
 * Bang gia cuoc cho mot loai dich vu. Quan tri vien chinh sua duoc tren giao dien.
 * Don vi tien: VND. Don vi khoi luong: gram.
 */
@Entity
@Table(name = "price_configs")
public class PriceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private ServiceType serviceType;

    /** Cuoc co ban khi lay va giao cung mot vung. */
    private long sameZoneFee;

    /** Cuoc co ban khi lay va giao khac vung. */
    private long crossZoneFee;

    /** Khoi luong da gom trong cuoc co ban. */
    private int includedWeightGram;

    /** Moi buoc khoi luong vuot muc tinh them extraFeePerStep. */
    private int stepWeightGram;

    private long extraFeePerStep;

    /** Khoi luong toi da nhan cho dich vu nay. */
    private int maxWeightGram;

    /** Phu phi co dinh cua dich vu (hoa toc). */
    private long surcharge;

    protected PriceConfig() {
    }

    public PriceConfig(ServiceType serviceType, long sameZoneFee, long crossZoneFee, int includedWeightGram,
                       int stepWeightGram, long extraFeePerStep, int maxWeightGram, long surcharge) {
        this.serviceType = serviceType;
        update(sameZoneFee, crossZoneFee, includedWeightGram, stepWeightGram, extraFeePerStep, maxWeightGram, surcharge);
    }

    public void update(long sameZoneFee, long crossZoneFee, int includedWeightGram, int stepWeightGram,
                       long extraFeePerStep, int maxWeightGram, long surcharge) {
        if (sameZoneFee < 0 || crossZoneFee < 0 || extraFeePerStep < 0 || surcharge < 0) {
            throw new BusinessException("Giá cước không được âm");
        }
        if (stepWeightGram <= 0) {
            throw new BusinessException("Bước khối lượng phải lớn hơn 0");
        }
        if (includedWeightGram < 0 || maxWeightGram <= includedWeightGram) {
            throw new BusinessException("Khối lượng tối đa phải lớn hơn khối lượng tính trong cước cơ bản");
        }
        this.sameZoneFee = sameZoneFee;
        this.crossZoneFee = crossZoneFee;
        this.includedWeightGram = includedWeightGram;
        this.stepWeightGram = stepWeightGram;
        this.extraFeePerStep = extraFeePerStep;
        this.maxWeightGram = maxWeightGram;
        this.surcharge = surcharge;
    }

    public Long getId() {
        return id;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public long getSameZoneFee() {
        return sameZoneFee;
    }

    public long getCrossZoneFee() {
        return crossZoneFee;
    }

    public int getIncludedWeightGram() {
        return includedWeightGram;
    }

    public int getStepWeightGram() {
        return stepWeightGram;
    }

    public long getExtraFeePerStep() {
        return extraFeePerStep;
    }

    public int getMaxWeightGram() {
        return maxWeightGram;
    }

    public long getSurcharge() {
        return surcharge;
    }
}
