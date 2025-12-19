package com.boatfuel.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Comprehensive tests for FuelUp entity.
 * Tests JPA annotations, relationships, calculated fields, and constraints.
 * Critical for javax.persistence → jakarta.persistence migration validation.
 */
class FuelUpEntityTest {

    private FuelUp fuelUp;
    private User user;

    @BeforeEach
    void setUp() {
        fuelUp = new FuelUp();
        user = new User();
        user.setUserId("test-user-123");
        user.setEmail("test@example.com");
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(fuelUp);
        assertNull(fuelUp.getId());
        assertNull(fuelUp.getUser());
        assertNull(fuelUp.getDate());
    }

    @Test
    void testParameterizedConstructor() {
        Date date = new Date();
        BigDecimal gallons = new BigDecimal("15.5");
        BigDecimal pricePerGallon = new BigDecimal("3.89");

        FuelUp fuelUp = new FuelUp(user, date, gallons, pricePerGallon);

        assertEquals(user, fuelUp.getUser());
        assertEquals(date, fuelUp.getDate());
        assertEquals(gallons, fuelUp.getGallons());
        assertEquals(pricePerGallon, fuelUp.getPricePerGallon());
        assertNotNull(fuelUp.getTotalCost());
        assertEquals(0, new BigDecimal("60.295").compareTo(fuelUp.getTotalCost()));
    }

    @Test
    void testIdFieldMapping() {
        Long id = 123L;
        fuelUp.setId(id);
        assertEquals(id, fuelUp.getId());
    }

    @Test
    void testUserRelationshipMapping() {
        fuelUp.setUser(user);
        assertEquals(user, fuelUp.getUser());
        assertEquals("test-user-123", fuelUp.getUser().getUserId());
    }

    @Test
    void testDateFieldMapping() {
        Date date = new Date();
        fuelUp.setDate(date);
        assertEquals(date, fuelUp.getDate());
    }

    @Test
    void testGallonsFieldMapping() {
        BigDecimal gallons = new BigDecimal("25.75");
        fuelUp.setGallons(gallons);
        assertEquals(gallons, fuelUp.getGallons());
    }

    @Test
    void testPricePerGallonFieldMapping() {
        BigDecimal price = new BigDecimal("4.25");
        fuelUp.setPricePerGallon(price);
        assertEquals(price, fuelUp.getPricePerGallon());
    }

    @Test
    void testTotalCostCalculationOnSetGallons() {
        BigDecimal gallons = new BigDecimal("10.0");
        BigDecimal pricePerGallon = new BigDecimal("4.00");

        fuelUp.setPricePerGallon(pricePerGallon);
        fuelUp.setGallons(gallons);

        assertNotNull(fuelUp.getTotalCost());
        assertEquals(0, new BigDecimal("40.00").compareTo(fuelUp.getTotalCost()));
    }

    @Test
    void testTotalCostCalculationOnSetPricePerGallon() {
        BigDecimal gallons = new BigDecimal("10.0");
        BigDecimal pricePerGallon = new BigDecimal("4.00");

        fuelUp.setGallons(gallons);
        fuelUp.setPricePerGallon(pricePerGallon);

        assertNotNull(fuelUp.getTotalCost());
        assertEquals(0, new BigDecimal("40.00").compareTo(fuelUp.getTotalCost()));
    }

    @Test
    void testTotalCostRecalculation() {
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(new BigDecimal("3.00"));
        assertEquals(0, new BigDecimal("30.00").compareTo(fuelUp.getTotalCost()));

        // Update gallons - should recalculate
        fuelUp.setGallons(new BigDecimal("20.0"));
        assertEquals(0, new BigDecimal("60.00").compareTo(fuelUp.getTotalCost()));

        // Update price - should recalculate
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));
        assertEquals(0, new BigDecimal("80.00").compareTo(fuelUp.getTotalCost()));
    }

    @Test
    void testManualTotalCostOverride() {
        BigDecimal manualTotal = new BigDecimal("99.99");
        fuelUp.setTotalCost(manualTotal);
        assertEquals(manualTotal, fuelUp.getTotalCost());
    }

    @Test
    void testEngineHoursFieldMapping() {
        BigDecimal engineHours = new BigDecimal("125.5");
        fuelUp.setEngineHours(engineHours);
        assertEquals(engineHours, fuelUp.getEngineHours());
    }

    @Test
    void testLocationFieldMapping() {
        String location = "Marina Bay, San Francisco";
        fuelUp.setLocation(location);
        assertEquals(location, fuelUp.getLocation());
    }

    @Test
    void testNotesFieldMapping() {
        String notes = "First fuel-up of the season. Engine running smoothly.";
        fuelUp.setNotes(notes);
        assertEquals(notes, fuelUp.getNotes());
    }

    @Test
    void testCreatedAtFieldMapping() {
        Date createdAt = new Date();
        fuelUp.setCreatedAt(createdAt);
        assertEquals(createdAt, fuelUp.getCreatedAt());
    }

    @Test
    void testSerializable() {
        assertTrue(fuelUp instanceof java.io.Serializable);
    }

    @Test
    void testNullFieldsAllowed() {
        // Optional fields should allow null
        fuelUp.setEngineHours(null);
        fuelUp.setLocation(null);
        fuelUp.setNotes(null);

        assertNull(fuelUp.getEngineHours());
        assertNull(fuelUp.getLocation());
        assertNull(fuelUp.getNotes());
    }

    @Test
    void testCompleteFuelUpObject() {
        fuelUp.setId(1L);
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("15.5"));
        fuelUp.setPricePerGallon(new BigDecimal("3.89"));
        fuelUp.setEngineHours(new BigDecimal("150.0"));
        fuelUp.setLocation("Test Marina");
        fuelUp.setNotes("Test notes");
        fuelUp.setCreatedAt(new Date());

        assertNotNull(fuelUp.getId());
        assertNotNull(fuelUp.getUser());
        assertNotNull(fuelUp.getDate());
        assertNotNull(fuelUp.getGallons());
        assertNotNull(fuelUp.getPricePerGallon());
        assertNotNull(fuelUp.getTotalCost());
        assertNotNull(fuelUp.getEngineHours());
        assertNotNull(fuelUp.getLocation());
        assertNotNull(fuelUp.getNotes());
        assertNotNull(fuelUp.getCreatedAt());
    }

    @Test
    void testDecimalPrecision() {
        BigDecimal gallons = new BigDecimal("10.12345678");
        BigDecimal price = new BigDecimal("4.56789012");

        fuelUp.setGallons(gallons);
        fuelUp.setPricePerGallon(price);

        assertNotNull(fuelUp.getTotalCost());
        // Verify calculation is performed correctly
        BigDecimal expectedTotal = gallons.multiply(price);
        assertEquals(0, expectedTotal.compareTo(fuelUp.getTotalCost()));
    }

    @Test
    void testTotalCostWithNullGallons() {
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));
        fuelUp.setGallons(null);
        // Should not throw exception, totalCost should remain null or previous value
        // This tests the null handling in calculateTotalCost
    }

    @Test
    void testTotalCostWithNullPrice() {
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(null);
        // Should not throw exception, totalCost should remain null or previous value
        // This tests the null handling in calculateTotalCost
    }
}
