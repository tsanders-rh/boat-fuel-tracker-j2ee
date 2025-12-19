package com.boatfuel.persistence;

import com.boatfuel.entity.FuelUp;
import com.boatfuel.entity.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Integration tests for JPA persistence operations.
 * Uses H2 in-memory database to test CRUD operations and queries.
 * Critical for validating javax.persistence → jakarta.persistence migration.
 */
class PersistenceIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;

    @BeforeAll
    static void setupEntityManagerFactory() {
        emf = Persistence.createEntityManagerFactory("BoatFuelTrackerPU-Test");
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
    }

    @AfterEach
    void tearDown() {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        if (em != null) {
            em.close();
        }
    }

    @Test
    void testPersistUser() {
        tx.begin();

        User user = new User();
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        user.setPasswordHash("hashedpassword");
        user.setIsAdmin(false);

        em.persist(user);
        tx.commit();

        assertNotNull(user.getUserId());

        // Verify persistence
        User foundUser = em.find(User.class, user.getUserId());
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals("Test User", foundUser.getDisplayName());
    }

    @Test
    void testPersistFuelUp() {
        tx.begin();

        // Create and persist user first
        User user = new User();
        user.setEmail("captain@boat.com");
        user.setDisplayName("Captain");
        em.persist(user);

        // Create and persist fuel-up
        FuelUp fuelUp = new FuelUp();
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("15.5"));
        fuelUp.setPricePerGallon(new BigDecimal("3.89"));
        fuelUp.setLocation("Test Marina");

        em.persist(fuelUp);
        tx.commit();

        assertNotNull(fuelUp.getId());

        // Verify persistence
        FuelUp foundFuelUp = em.find(FuelUp.class, fuelUp.getId());
        assertNotNull(foundFuelUp);
        assertEquals(new BigDecimal("15.5"), foundFuelUp.getGallons());
        assertEquals("Test Marina", foundFuelUp.getLocation());
        assertNotNull(foundFuelUp.getUser());
    }

    @Test
    void testUpdateUser() {
        tx.begin();

        User user = new User();
        user.setEmail("original@example.com");
        user.setDisplayName("Original Name");
        em.persist(user);
        tx.commit();

        String userId = user.getUserId();

        // Update
        tx.begin();
        User managedUser = em.find(User.class, userId);
        managedUser.setDisplayName("Updated Name");
        managedUser.setIsAdmin(true);
        tx.commit();

        // Verify update
        em.clear(); // Clear persistence context
        User updatedUser = em.find(User.class, userId);
        assertEquals("Updated Name", updatedUser.getDisplayName());
        assertTrue(updatedUser.getIsAdmin());
    }

    @Test
    void testDeleteFuelUp() {
        tx.begin();

        User user = new User();
        user.setEmail("delete@example.com");
        em.persist(user);

        FuelUp fuelUp = new FuelUp();
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));
        em.persist(fuelUp);
        tx.commit();

        Long fuelUpId = fuelUp.getId();

        // Delete
        tx.begin();
        FuelUp managedFuelUp = em.find(FuelUp.class, fuelUpId);
        em.remove(managedFuelUp);
        tx.commit();

        // Verify deletion
        FuelUp deletedFuelUp = em.find(FuelUp.class, fuelUpId);
        assertNull(deletedFuelUp);
    }

    @Test
    void testQueryFuelUpsByUser() {
        tx.begin();

        User user = new User();
        user.setEmail("query@example.com");
        em.persist(user);

        // Create multiple fuel-ups
        for (int i = 0; i < 3; i++) {
            FuelUp fuelUp = new FuelUp();
            fuelUp.setUser(user);
            fuelUp.setDate(new Date());
            fuelUp.setGallons(new BigDecimal("10.0"));
            fuelUp.setPricePerGallon(new BigDecimal("4.00"));
            em.persist(fuelUp);
        }
        tx.commit();

        // Query
        TypedQuery<FuelUp> query = em.createQuery(
            "SELECT f FROM FuelUp f WHERE f.user.userId = :userId ORDER BY f.date DESC",
            FuelUp.class);
        query.setParameter("userId", user.getUserId());
        List<FuelUp> fuelUps = query.getResultList();

        assertEquals(3, fuelUps.size());
        for (FuelUp fuelUp : fuelUps) {
            assertEquals(user.getUserId(), fuelUp.getUser().getUserId());
        }
    }

    @Test
    void testOneToManyRelationship() {
        tx.begin();

        User user = new User();
        user.setEmail("relationship@example.com");
        em.persist(user);

        FuelUp fuelUp1 = new FuelUp();
        fuelUp1.setUser(user);
        fuelUp1.setDate(new Date());
        fuelUp1.setGallons(new BigDecimal("10.0"));
        fuelUp1.setPricePerGallon(new BigDecimal("4.00"));
        em.persist(fuelUp1);

        FuelUp fuelUp2 = new FuelUp();
        fuelUp2.setUser(user);
        fuelUp2.setDate(new Date());
        fuelUp2.setGallons(new BigDecimal("15.0"));
        fuelUp2.setPricePerGallon(new BigDecimal("3.50"));
        em.persist(fuelUp2);

        tx.commit();

        // Clear and reload
        em.clear();
        User reloadedUser = em.find(User.class, user.getUserId());

        // The fuelUps collection should be lazily loaded
        assertNotNull(reloadedUser);
        List<FuelUp> fuelUps = reloadedUser.getFuelUps();
        assertNotNull(fuelUps);
        assertEquals(2, fuelUps.size());
    }

    @Test
    void testManyToOneRelationship() {
        tx.begin();

        User user = new User();
        user.setEmail("manytoone@example.com");
        em.persist(user);

        FuelUp fuelUp = new FuelUp();
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));
        em.persist(fuelUp);

        tx.commit();

        // Clear and reload
        em.clear();
        FuelUp reloadedFuelUp = em.find(FuelUp.class, fuelUp.getId());

        assertNotNull(reloadedFuelUp);
        assertNotNull(reloadedFuelUp.getUser());
        assertEquals("manytoone@example.com", reloadedFuelUp.getUser().getEmail());
    }

    @Test
    void testNamedQueryOrJpqlQuery() {
        tx.begin();

        User user1 = new User();
        user1.setEmail("user1@example.com");
        em.persist(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        em.persist(user2);

        tx.commit();

        // Test JPQL query
        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.email LIKE :emailPattern",
            User.class);
        query.setParameter("emailPattern", "%example.com");
        List<User> users = query.getResultList();

        assertTrue(users.size() >= 2);
    }

    @Test
    void testEntityManagerFlush() {
        tx.begin();

        User user = new User();
        user.setEmail("flush@example.com");
        em.persist(user);
        em.flush(); // Force synchronization to database

        assertNotNull(user.getUserId());

        tx.commit();
    }

    @Test
    void testEntityManagerClear() {
        tx.begin();

        User user = new User();
        user.setEmail("clear@example.com");
        em.persist(user);
        tx.commit();

        String userId = user.getUserId();

        // Clear persistence context
        em.clear();

        // Entity should no longer be managed
        assertFalse(em.contains(user));

        // But should still be in database
        User foundUser = em.find(User.class, userId);
        assertNotNull(foundUser);
    }

    @Test
    void testCascadeOperations() {
        tx.begin();

        User user = new User();
        user.setEmail("cascade@example.com");

        FuelUp fuelUp = new FuelUp();
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("10.0"));
        fuelUp.setPricePerGallon(new BigDecimal("4.00"));

        // Persist user (with cascade, fuel-up might be persisted too, but in this case we manage it separately)
        em.persist(user);
        em.persist(fuelUp);

        tx.commit();

        assertNotNull(user.getUserId());
        assertNotNull(fuelUp.getId());
    }
}
