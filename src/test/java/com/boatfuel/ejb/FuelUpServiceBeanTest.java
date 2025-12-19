package com.boatfuel.ejb;

import com.boatfuel.entity.FuelUp;
import com.boatfuel.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for FuelUpServiceBean EJB.
 * Tests EJB business logic with mocked persistence layer.
 * Critical for validating EJB functionality before and after migration.
 */
@ExtendWith(MockitoExtension.class)
class FuelUpServiceBeanTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private FuelUpServiceBean fuelUpService;

    private User testUser;
    private FuelUp testFuelUp;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("test-user-123");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");

        testFuelUp = new FuelUp();
        testFuelUp.setId(1L);
        testFuelUp.setUser(testUser);
        testFuelUp.setDate(new Date());
        testFuelUp.setGallons(new BigDecimal("15.5"));
        testFuelUp.setPricePerGallon(new BigDecimal("3.89"));
    }

    @Test
    void testCreateFuelUp() {
        // Arrange
        doNothing().when(entityManager).persist(any(FuelUp.class));

        // Act
        FuelUp result = fuelUpService.createFuelUp(testFuelUp);

        // Assert
        assertNotNull(result);
        assertEquals(testFuelUp, result);
        verify(entityManager, times(1)).persist(testFuelUp);
    }

    @Test
    void testCreateFuelUpThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(entityManager).persist(any(FuelUp.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fuelUpService.createFuelUp(testFuelUp);
        });
    }

    @Test
    void testGetFuelUpsByUser() {
        // Arrange
        String userId = "test-user-123";
        List<FuelUp> expectedFuelUps = new ArrayList<>();
        expectedFuelUps.add(testFuelUp);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedFuelUps);

        // Act
        List<FuelUp> result = fuelUpService.getFuelUpsByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFuelUp, result.get(0));
        verify(entityManager, times(1)).createQuery(anyString());
        verify(query, times(1)).setParameter("userId", userId);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testGetFuelUpsByUserReturnsEmptyList() {
        // Arrange
        String userId = "non-existent-user";
        List<FuelUp> emptyList = new ArrayList<>();

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(emptyList);

        // Act
        List<FuelUp> result = fuelUpService.getFuelUpsByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetFuelUpsByUserThrowsException() {
        // Arrange
        String userId = "test-user-123";
        when(entityManager.createQuery(anyString())).thenThrow(new RuntimeException("Query error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fuelUpService.getFuelUpsByUser(userId);
        });
    }

    @Test
    void testDeleteFuelUp() {
        // Arrange
        Long fuelUpId = 1L;
        when(entityManager.find(FuelUp.class, fuelUpId)).thenReturn(testFuelUp);
        doNothing().when(entityManager).remove(any(FuelUp.class));

        // Act
        fuelUpService.deleteFuelUp(fuelUpId);

        // Assert
        verify(entityManager, times(1)).find(FuelUp.class, fuelUpId);
        verify(entityManager, times(1)).remove(testFuelUp);
    }

    @Test
    void testDeleteFuelUpNotFound() {
        // Arrange
        Long fuelUpId = 999L;
        when(entityManager.find(FuelUp.class, fuelUpId)).thenReturn(null);

        // Act
        fuelUpService.deleteFuelUp(fuelUpId);

        // Assert
        verify(entityManager, times(1)).find(FuelUp.class, fuelUpId);
        verify(entityManager, never()).remove(any(FuelUp.class));
    }

    @Test
    void testDeleteFuelUpThrowsException() {
        // Arrange
        Long fuelUpId = 1L;
        when(entityManager.find(FuelUp.class, fuelUpId)).thenReturn(testFuelUp);
        doThrow(new RuntimeException("Delete error")).when(entityManager).remove(any(FuelUp.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fuelUpService.deleteFuelUp(fuelUpId);
        });
    }

    @Test
    void testEntityManagerPersistCallsCorrectMethod() {
        // Arrange
        FuelUp newFuelUp = new FuelUp();
        newFuelUp.setUser(testUser);
        newFuelUp.setDate(new Date());
        newFuelUp.setGallons(new BigDecimal("20.0"));
        newFuelUp.setPricePerGallon(new BigDecimal("4.00"));

        // Act
        fuelUpService.createFuelUp(newFuelUp);

        // Assert
        verify(entityManager).persist(argThat((FuelUp fuelUp) ->
            fuelUp.getGallons().equals(new BigDecimal("20.0")) &&
            fuelUp.getPricePerGallon().equals(new BigDecimal("4.00"))
        ));
    }

    @Test
    void testQueryParameterBinding() {
        // Arrange
        String userId = "specific-user-id";
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());

        // Act
        fuelUpService.getFuelUpsByUser(userId);

        // Assert
        verify(query).setParameter(eq("userId"), eq(userId));
    }

    @Test
    void testMultipleFuelUpsRetrieval() {
        // Arrange
        String userId = "test-user-123";
        List<FuelUp> expectedFuelUps = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            FuelUp fuelUp = new FuelUp();
            fuelUp.setId((long) i);
            fuelUp.setUser(testUser);
            fuelUp.setDate(new Date());
            fuelUp.setGallons(new BigDecimal("10.0"));
            fuelUp.setPricePerGallon(new BigDecimal("4.00"));
            expectedFuelUps.add(fuelUp);
        }

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedFuelUps);

        // Act
        List<FuelUp> result = fuelUpService.getFuelUpsByUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }
}
