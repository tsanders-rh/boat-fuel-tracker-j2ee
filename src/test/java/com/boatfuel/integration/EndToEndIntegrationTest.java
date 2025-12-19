package com.boatfuel.integration;

import com.boatfuel.ejb.FuelUpServiceBean;
import com.boatfuel.entity.FuelUp;
import com.boatfuel.entity.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * End-to-end integration tests simulating complete workflows.
 * Tests the full stack from entity to EJB using real JPA persistence.
 * Critical for validating complete application behavior before and after migration.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private EntityTransaction tx;
    private FuelUpServiceBean fuelUpService;

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
    void setUp() throws Exception {
        em = emf.createEntityManager();
        tx = em.getTransaction();

        // Create FuelUpServiceBean and inject EntityManager using reflection
        fuelUpService = new FuelUpServiceBean();
        java.lang.reflect.Field field = FuelUpServiceBean.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(fuelUpService, em);
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
    @Order(1)
    void testCompleteUserRegistrationWorkflow() {
        tx.begin();

        // Simulate user registration
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setDisplayName("New User");
        user.setPasswordHash("$2a$10$hashedpassword");
        user.setIsAdmin(false);

        em.persist(user);
        tx.commit();

        // Verify user was created
        assertNotNull(user.getUserId());

        // Verify user can be retrieved
        User retrievedUser = em.find(User.class, user.getUserId());
        assertNotNull(retrievedUser);
        assertEquals("newuser@example.com", retrievedUser.getEmail());
    }

    @Test
    @Order(2)
    void testCompleteFuelUpEntryWorkflow() {
        tx.begin();

        // Create user
        User user = new User();
        user.setEmail("boatowner@example.com");
        user.setDisplayName("Boat Owner");
        em.persist(user);

        // User adds first fuel-up
        FuelUp fuelUp1 = new FuelUp(user, new Date(), new BigDecimal("15.5"), new BigDecimal("3.89"));
        fuelUp1.setLocation("Marina Bay");
        fuelUp1.setEngineHours(new BigDecimal("100.0"));
        fuelUpService.createFuelUp(fuelUp1);

        // User adds second fuel-up
        FuelUp fuelUp2 = new FuelUp(user, new Date(), new BigDecimal("20.0"), new BigDecimal("3.95"));
        fuelUp2.setLocation("Harbor Point");
        fuelUp2.setEngineHours(new BigDecimal("125.5"));
        fuelUpService.createFuelUp(fuelUp2);

        tx.commit();

        // User views their fuel-ups
        List<FuelUp> fuelUps = fuelUpService.getFuelUpsByUser(user.getUserId());

        assertEquals(2, fuelUps.size());
        assertTrue(fuelUps.stream().anyMatch(f -> f.getLocation().equals("Marina Bay")));
        assertTrue(fuelUps.stream().anyMatch(f -> f.getLocation().equals("Harbor Point")));
    }

    @Test
    @Order(3)
    void testFuelUpDeletionWorkflow() {
        tx.begin();

        // Create user and fuel-up
        User user = new User();
        user.setEmail("deletetest@example.com");
        em.persist(user);

        FuelUp fuelUp = new FuelUp(user, new Date(), new BigDecimal("10.0"), new BigDecimal("4.00"));
        fuelUpService.createFuelUp(fuelUp);

        tx.commit();

        Long fuelUpId = fuelUp.getId();

        // Delete fuel-up
        tx.begin();
        fuelUpService.deleteFuelUp(fuelUpId);
        tx.commit();

        // Verify deletion
        FuelUp deletedFuelUp = em.find(FuelUp.class, fuelUpId);
        assertNull(deletedFuelUp);
    }

    @Test
    @Order(4)
    void testMultipleUsersIndependentData() {
        tx.begin();

        // Create two users
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setDisplayName("User One");
        em.persist(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setDisplayName("User Two");
        em.persist(user2);

        // User 1 adds fuel-ups
        FuelUp user1FuelUp1 = new FuelUp(user1, new Date(), new BigDecimal("15.0"), new BigDecimal("3.50"));
        FuelUp user1FuelUp2 = new FuelUp(user1, new Date(), new BigDecimal("20.0"), new BigDecimal("3.60"));
        fuelUpService.createFuelUp(user1FuelUp1);
        fuelUpService.createFuelUp(user1FuelUp2);

        // User 2 adds fuel-ups
        FuelUp user2FuelUp1 = new FuelUp(user2, new Date(), new BigDecimal("12.0"), new BigDecimal("3.80"));
        fuelUpService.createFuelUp(user2FuelUp1);

        tx.commit();

        // Verify each user sees only their own data
        List<FuelUp> user1FuelUps = fuelUpService.getFuelUpsByUser(user1.getUserId());
        List<FuelUp> user2FuelUps = fuelUpService.getFuelUpsByUser(user2.getUserId());

        assertEquals(2, user1FuelUps.size());
        assertEquals(1, user2FuelUps.size());

        // Verify data integrity
        assertTrue(user1FuelUps.stream().allMatch(f -> f.getUser().getUserId().equals(user1.getUserId())));
        assertTrue(user2FuelUps.stream().allMatch(f -> f.getUser().getUserId().equals(user2.getUserId())));
    }

    @Test
    @Order(5)
    void testFuelUpUpdateWorkflow() {
        tx.begin();

        // Create user and fuel-up
        User user = new User();
        user.setEmail("updatetest@example.com");
        em.persist(user);

        FuelUp fuelUp = new FuelUp(user, new Date(), new BigDecimal("10.0"), new BigDecimal("4.00"));
        fuelUp.setNotes("Initial notes");
        em.persist(fuelUp);

        tx.commit();

        Long fuelUpId = fuelUp.getId();

        // Update fuel-up
        tx.begin();
        FuelUp managedFuelUp = em.find(FuelUp.class, fuelUpId);
        managedFuelUp.setGallons(new BigDecimal("12.5"));
        managedFuelUp.setPricePerGallon(new BigDecimal("4.25"));
        managedFuelUp.setNotes("Updated notes");
        tx.commit();

        // Verify update
        em.clear();
        FuelUp updatedFuelUp = em.find(FuelUp.class, fuelUpId);
        assertEquals(0, new BigDecimal("12.5").compareTo(updatedFuelUp.getGallons()));
        assertEquals(0, new BigDecimal("4.25").compareTo(updatedFuelUp.getPricePerGallon()));
        assertEquals("Updated notes", updatedFuelUp.getNotes());
    }

    @Test
    @Order(6)
    void testUserWithMultipleFuelUpsRelationship() {
        tx.begin();

        // Create user
        User user = new User();
        user.setEmail("relationship@example.com");
        em.persist(user);

        // Add multiple fuel-ups
        for (int i = 0; i < 5; i++) {
            FuelUp fuelUp = new FuelUp(
                user,
                new Date(),
                new BigDecimal("10.0").add(new BigDecimal(i)),
                new BigDecimal("4.00")
            );
            em.persist(fuelUp);
        }

        tx.commit();

        // Load user and verify relationship
        em.clear();
        User loadedUser = em.find(User.class, user.getUserId());
        assertNotNull(loadedUser);
        assertNotNull(loadedUser.getFuelUps());
        assertEquals(5, loadedUser.getFuelUps().size());
    }

    @Test
    @Order(7)
    void testTransactionRollback() {
        tx.begin();

        User user = new User();
        user.setEmail("rollback@example.com");
        em.persist(user);

        FuelUp fuelUp = new FuelUp(user, new Date(), new BigDecimal("10.0"), new BigDecimal("4.00"));
        em.persist(fuelUp);

        // Rollback instead of commit
        tx.rollback();

        // Verify nothing was persisted
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", "rollback@example.com");
        List<User> users = query.getResultList();

        assertEquals(0, users.size());
    }

    @Test
    @Order(8)
    void testCompleteUserJourney() {
        tx.begin();

        // Step 1: User registers
        User user = new User();
        user.setEmail("journey@example.com");
        user.setDisplayName("Journey User");
        user.setPasswordHash("$2a$10$hashed");
        user.setIsAdmin(false);
        em.persist(user);

        tx.commit();

        // Step 2: User logs in and adds first fuel-up
        tx.begin();
        FuelUp fuelUp1 = new FuelUp(user, new Date(), new BigDecimal("15.5"), new BigDecimal("3.89"));
        fuelUp1.setLocation("Home Marina");
        fuelUp1.setEngineHours(new BigDecimal("50.0"));
        fuelUp1.setNotes("First fuel-up of the season");
        fuelUpService.createFuelUp(fuelUp1);
        tx.commit();

        // Step 3: User adds more fuel-ups over time
        tx.begin();
        FuelUp fuelUp2 = new FuelUp(user, new Date(), new BigDecimal("18.0"), new BigDecimal("3.95"));
        fuelUp2.setLocation("Island Dock");
        fuelUp2.setEngineHours(new BigDecimal("75.5"));
        fuelUpService.createFuelUp(fuelUp2);

        FuelUp fuelUp3 = new FuelUp(user, new Date(), new BigDecimal("12.0"), new BigDecimal("4.10"));
        fuelUp3.setLocation("Harbor Point");
        fuelUp3.setEngineHours(new BigDecimal("95.0"));
        fuelUpService.createFuelUp(fuelUp3);
        tx.commit();

        // Step 4: User views their complete history
        List<FuelUp> allFuelUps = fuelUpService.getFuelUpsByUser(user.getUserId());
        assertEquals(3, allFuelUps.size());

        // Step 5: User updates last login
        tx.begin();
        User managedUser = em.find(User.class, user.getUserId());
        managedUser.setLastLogin(new Date());
        tx.commit();

        // Verify complete journey
        em.clear();
        User finalUser = em.find(User.class, user.getUserId());
        assertNotNull(finalUser);
        assertNotNull(finalUser.getLastLogin());
        assertEquals(3, finalUser.getFuelUps().size());

        // Verify data integrity
        BigDecimal totalGallons = finalUser.getFuelUps().stream()
            .map(FuelUp::getGallons)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("45.5").compareTo(totalGallons));
    }
}
