package com.boatfuel.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FuelUp entity using Quarkus Hibernate ORM with Panache
 */
@Entity
@Table(name = "FUEL_UPS", indexes = {
    @Index(name = "IDX_FUEL_DATE", columnList = "FUEL_DATE")
})
public class FuelUp extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    public User user;

    @Column(name = "FUEL_DATE", nullable = false)
    public LocalDate date;

    @Column(name = "GALLONS", precision = 10, scale = 2, nullable = false)
    public BigDecimal gallons;

    @Column(name = "PRICE_PER_GALLON", precision = 10, scale = 2, nullable = false)
    public BigDecimal pricePerGallon;

    @Column(name = "TOTAL_COST", precision = 10, scale = 2)
    public BigDecimal totalCost;

    @Column(name = "ENGINE_HOURS", precision = 10, scale = 1)
    public BigDecimal engineHours;

    @Column(name = "LOCATION", length = 500)
    public String location;

    @Column(name = "NOTES", length = 2000)
    public String notes;

    @Column(name = "CREATED_AT")
    public LocalDateTime createdAt;

    // Default constructor
    public FuelUp() {
    }

    // Constructor
    public FuelUp(User user, LocalDate date, BigDecimal gallons, BigDecimal pricePerGallon) {
        this.user = user;
        this.date = date;
        this.gallons = gallons;
        this.pricePerGallon = pricePerGallon;
        this.totalCost = gallons.multiply(pricePerGallon);
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        calculateTotalCost();
    }

    @PreUpdate
    public void preUpdate() {
        calculateTotalCost();
    }

    private void calculateTotalCost() {
        if (gallons != null && pricePerGallon != null) {
            this.totalCost = gallons.multiply(pricePerGallon);
        }
    }

    // Static helper methods for Panache queries
    public static List<FuelUp> findByUser(String userId) {
        return list("user.userId = ?1 order by date desc", userId);
    }

    public static List<FuelUp> findByUserBetweenDates(String userId, LocalDate startDate, LocalDate endDate) {
        return list("user.userId = ?1 and date between ?2 and ?3 order by date desc", userId, startDate, endDate);
    }

    public static long countByUser(String userId) {
        return count("user.userId", userId);
    }
}
