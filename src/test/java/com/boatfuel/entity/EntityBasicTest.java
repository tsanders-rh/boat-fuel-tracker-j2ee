package com.boatfuel.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Basic tests for entities to ensure JPA mappings are correct.
 * Critical for javax.persistence → jakarta.persistence migration validation.
 */
class EntityBasicTest {

    @Test
    void testUserCreation() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        user.setPasswordHash("hashedpassword");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getDisplayName());
    }

    @Test
    void testFuelUpCreation() {
        User user = new User();
        user.setEmail("captain@boat.com");

        FuelUp fuelUp = new FuelUp();
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("15.5"));
        fuelUp.setPricePerGallon(new BigDecimal("3.89"));

        assertEquals(new BigDecimal("15.5"), fuelUp.getGallons());
        assertEquals(new BigDecimal("3.89"), fuelUp.getPricePerGallon());
        assertEquals(user, fuelUp.getUser());
    }

    @Test
    void testFuelUpTotalCostCalculation() {
        FuelUp fuelUp = new FuelUp();
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));

        // Total cost should be automatically calculated
        assertNotNull(fuelUp.getTotalCost());
        assertEquals(0, new BigDecimal("40.00").compareTo(fuelUp.getTotalCost()));
    }
}
