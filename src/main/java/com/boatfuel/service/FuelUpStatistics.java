package com.boatfuel.service;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for fuel-up statistics
 */
public class FuelUpStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalFillups;
    private BigDecimal totalGallons;
    private BigDecimal totalSpent;
    private BigDecimal averagePricePerGallon;

    public FuelUpStatistics() {
    }

    public FuelUpStatistics(int totalFillups, BigDecimal totalGallons, BigDecimal totalSpent, BigDecimal averagePricePerGallon) {
        this.totalFillups = totalFillups;
        this.totalGallons = totalGallons;
        this.totalSpent = totalSpent;
        this.averagePricePerGallon = averagePricePerGallon;
    }

    public int getTotalFillups() {
        return totalFillups;
    }

    public void setTotalFillups(int totalFillups) {
        this.totalFillups = totalFillups;
    }

    public BigDecimal getTotalGallons() {
        return totalGallons;
    }

    public void setTotalGallons(BigDecimal totalGallons) {
        this.totalGallons = totalGallons;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getAveragePricePerGallon() {
        return averagePricePerGallon;
    }

    public void setAveragePricePerGallon(BigDecimal averagePricePerGallon) {
        this.averagePricePerGallon = averagePricePerGallon;
    }
}
