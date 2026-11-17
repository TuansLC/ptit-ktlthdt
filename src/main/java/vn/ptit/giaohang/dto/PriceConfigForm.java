package vn.ptit.giaohang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import vn.ptit.giaohang.domain.pricing.PriceConfig;

public class PriceConfigForm {

    @NotNull @Min(0)
    private Long sameZoneFee;

    @NotNull @Min(0)
    private Long crossZoneFee;

    @NotNull @Min(0)
    private Integer includedWeightGram;

    @NotNull @Min(1)
    private Integer stepWeightGram;

    @NotNull @Min(0)
    private Long extraFeePerStep;

    @NotNull @Min(1)
    private Integer maxWeightGram;

    @NotNull @Min(0)
    private Long surcharge;

    public static PriceConfigForm from(PriceConfig c) {
        PriceConfigForm f = new PriceConfigForm();
        f.sameZoneFee = c.getSameZoneFee();
        f.crossZoneFee = c.getCrossZoneFee();
        f.includedWeightGram = c.getIncludedWeightGram();
        f.stepWeightGram = c.getStepWeightGram();
        f.extraFeePerStep = c.getExtraFeePerStep();
        f.maxWeightGram = c.getMaxWeightGram();
        f.surcharge = c.getSurcharge();
        return f;
    }

    public Long getSameZoneFee() {
        return sameZoneFee;
    }

    public void setSameZoneFee(Long sameZoneFee) {
        this.sameZoneFee = sameZoneFee;
    }

    public Long getCrossZoneFee() {
        return crossZoneFee;
    }

    public void setCrossZoneFee(Long crossZoneFee) {
        this.crossZoneFee = crossZoneFee;
    }

    public Integer getIncludedWeightGram() {
        return includedWeightGram;
    }

    public void setIncludedWeightGram(Integer includedWeightGram) {
        this.includedWeightGram = includedWeightGram;
    }

    public Integer getStepWeightGram() {
        return stepWeightGram;
    }

    public void setStepWeightGram(Integer stepWeightGram) {
        this.stepWeightGram = stepWeightGram;
    }

    public Long getExtraFeePerStep() {
        return extraFeePerStep;
    }

    public void setExtraFeePerStep(Long extraFeePerStep) {
        this.extraFeePerStep = extraFeePerStep;
    }

    public Integer getMaxWeightGram() {
        return maxWeightGram;
    }

    public void setMaxWeightGram(Integer maxWeightGram) {
        this.maxWeightGram = maxWeightGram;
    }

    public Long getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(Long surcharge) {
        this.surcharge = surcharge;
    }
}
