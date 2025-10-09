package com.boatfuel.service;

import com.boatfuel.entity.FuelUp;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * FuelUpService - CDI bean replacing the legacy EJB 2.x SessionBean
 */
@ApplicationScoped
public class FuelUpService {

    /**
     * Create a new fuel-up record
     */
    @Transactional
    public FuelUp createFuelUp(FuelUp fuelUp) {
        Log.infof("Creating new fuel-up for user: %s", fuelUp.user.userId);
        fuelUp.persist();
        return fuelUp;
    }

    /**
     * Get all fuel-ups for a specific user
     */
    public List<FuelUp> getFuelUpsByUser(String userId) {
        Log.debugf("Getting fuel-ups for user: %s", userId);
        return FuelUp.findByUser(userId);
    }

    /**
     * Get fuel-ups for a user within a date range
     */
    public List<FuelUp> getFuelUpsByUserBetweenDates(String userId, LocalDate startDate, LocalDate endDate) {
        Log.debugf("Getting fuel-ups for user %s between %s and %s", userId, startDate, endDate);
        return FuelUp.findByUserBetweenDates(userId, startDate, endDate);
    }

    /**
     * Delete a fuel-up record
     */
    @Transactional
    public void deleteFuelUp(Long fuelUpId) {
        Log.infof("Deleting fuel-up: %d", fuelUpId);
        FuelUp fuelUp = FuelUp.findById(fuelUpId);
        if (fuelUp != null) {
            fuelUp.delete();
        }
    }

    /**
     * Get statistics for a user's fuel-ups
     */
    public FuelUpStatistics getStatistics(String userId) {
        Log.debugf("Calculating statistics for user: %s", userId);

        List<FuelUp> fuelUps = FuelUp.findByUser(userId);

        if (fuelUps.isEmpty()) {
            return new FuelUpStatistics(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        int count = fuelUps.size();
        BigDecimal totalGallons = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalPriceSum = BigDecimal.ZERO;

        for (FuelUp fuelUp : fuelUps) {
            totalGallons = totalGallons.add(fuelUp.gallons);
            totalSpent = totalSpent.add(fuelUp.totalCost);
            totalPriceSum = totalPriceSum.add(fuelUp.pricePerGallon);
        }

        BigDecimal avgPrice = totalPriceSum.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);

        return new FuelUpStatistics(count, totalGallons, totalSpent, avgPrice);
    }
}
