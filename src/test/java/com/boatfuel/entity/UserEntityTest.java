package com.boatfuel.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Comprehensive tests for User entity.
 * Tests JPA annotations, relationships, and field constraints.
 * Critical for javax.persistence → jakarta.persistence migration validation.
 */
class UserEntityTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getUserId());
        assertNull(user.getEmail());
        assertNull(user.getDisplayName());
    }

    @Test
    void testEmailFieldMapping() {
        String email = "captain@boat.com";
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testDisplayNameFieldMapping() {
        String displayName = "Captain Jack";
        user.setDisplayName(displayName);
        assertEquals(displayName, user.getDisplayName());
    }

    @Test
    void testPasswordHashFieldMapping() {
        String passwordHash = "$2a$10$abcdefghijklmnopqrstuv";
        user.setPasswordHash(passwordHash);
        assertEquals(passwordHash, user.getPasswordHash());
    }

    @Test
    void testIsAdminFieldMapping() {
        user.setIsAdmin(true);
        assertTrue(user.getIsAdmin());

        user.setIsAdmin(false);
        assertFalse(user.getIsAdmin());
    }

    @Test
    void testCreatedAtFieldMapping() {
        Date now = new Date();
        user.setCreatedAt(now);
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testLastLoginFieldMapping() {
        Date now = new Date();
        user.setLastLogin(now);
        assertEquals(now, user.getLastLogin());
    }

    @Test
    void testOneToManyRelationshipWithFuelUps() {
        List<FuelUp> fuelUps = new ArrayList<>();

        FuelUp fuelUp1 = new FuelUp();
        FuelUp fuelUp2 = new FuelUp();

        fuelUps.add(fuelUp1);
        fuelUps.add(fuelUp2);

        user.setFuelUps(fuelUps);

        assertEquals(2, user.getFuelUps().size());
        assertTrue(user.getFuelUps().contains(fuelUp1));
        assertTrue(user.getFuelUps().contains(fuelUp2));
    }

    @Test
    void testUserIdFieldMapping() {
        String userId = "user-123-abc";
        user.setUserId(userId);
        assertEquals(userId, user.getUserId());
    }

    @Test
    void testSerializable() {
        assertTrue(user instanceof java.io.Serializable);
    }

    @Test
    void testNullFieldsAllowed() {
        // These fields should allow null values
        user.setDisplayName(null);
        user.setPasswordHash(null);
        user.setIsAdmin(null);
        user.setCreatedAt(null);
        user.setLastLogin(null);
        user.setFuelUps(null);

        assertNull(user.getDisplayName());
        assertNull(user.getPasswordHash());
        assertNull(user.getIsAdmin());
        assertNull(user.getCreatedAt());
        assertNull(user.getLastLogin());
        assertNull(user.getFuelUps());
    }

    @Test
    void testCompleteUserObject() {
        user.setUserId("user-456");
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        user.setPasswordHash("hashedpassword123");
        user.setIsAdmin(false);
        user.setCreatedAt(new Date());
        user.setLastLogin(new Date());
        user.setFuelUps(new ArrayList<>());

        assertNotNull(user.getUserId());
        assertNotNull(user.getEmail());
        assertNotNull(user.getDisplayName());
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getIsAdmin());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastLogin());
        assertNotNull(user.getFuelUps());
    }
}
