package com.boatfuel.servlet;

import com.boatfuel.ejb.FuelUpService;
import com.boatfuel.entity.FuelUp;
import com.boatfuel.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for FuelUpServlet.
 * Tests servlet request handling, session management, and EJB interaction.
 * Critical for validating servlet layer before and after migration.
 */
@ExtendWith(MockitoExtension.class)
class FuelUpServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FuelUpService fuelUpService;

    private FuelUpServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new FuelUpServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        // Use reflection to inject the mocked service
        java.lang.reflect.Field field = FuelUpServlet.class.getDeclaredField("fuelUpService");
        field.setAccessible(true);
        field.set(servlet, fuelUpService);
    }

    @Test
    void testDoGetWithUserIdInSession() throws Exception {
        // Arrange
        String userId = "test-user-123";
        List<FuelUp> fuelUps = createTestFuelUps();

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.getFuelUpsByUser(userId)).thenReturn(fuelUps);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/html");
        verify(fuelUpService).getFuelUpsByUser(userId);
        String output = stringWriter.toString();
        assertTrue(output.contains("<html>"));
        assertTrue(output.contains("Your Fuel-Ups"));
    }

    @Test
    void testDoGetWithoutUserIdInSession() throws Exception {
        // Arrange
        List<FuelUp> fuelUps = new ArrayList<>();

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.getFuelUpsByUser("testuser")).thenReturn(fuelUps);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(session).setAttribute("userId", "testuser");
        verify(fuelUpService).getFuelUpsByUser("testuser");
    }

    @Test
    void testDoGetRendersHtmlTable() throws Exception {
        // Arrange
        String userId = "test-user-123";
        List<FuelUp> fuelUps = createTestFuelUps();

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.getFuelUpsByUser(userId)).thenReturn(fuelUps);

        // Act
        servlet.doGet(request, response);

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("<table"));
        assertTrue(output.contains("<tr>"));
        assertTrue(output.contains("<td>"));
        assertTrue(output.contains("15.5"));
        assertTrue(output.contains("3.89"));
    }

    @Test
    void testDoGetHandlesException() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.getFuelUpsByUser(userId)).thenThrow(new RuntimeException("Database error"));

        // Act
        servlet.doGet(request, response);

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Error"));
    }

    @Test
    void testDoPostCreatesNewFuelUp() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("15.5");
        when(request.getParameter("pricePerGallon")).thenReturn("3.89");
        when(request.getParameter("engineHours")).thenReturn("125.5");
        when(request.getParameter("location")).thenReturn("Test Marina");
        when(request.getParameter("notes")).thenReturn("Test notes");
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.createFuelUp(any(FuelUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(response).setContentType("text/html");
        verify(fuelUpService).createFuelUp(any(FuelUp.class));
        String output = stringWriter.toString();
        assertTrue(output.contains("Success"));
    }

    @Test
    void testDoPostWithoutUserIdInSession() throws Exception {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("15.5");
        when(request.getParameter("pricePerGallon")).thenReturn("3.89");
        when(request.getParameter("engineHours")).thenReturn(null);
        when(request.getParameter("location")).thenReturn("Test Marina");
        when(request.getParameter("notes")).thenReturn("Test notes");
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.createFuelUp(any(FuelUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(session).setAttribute("userId", "testuser");
        verify(fuelUpService).createFuelUp(any(FuelUp.class));
    }

    @Test
    void testDoPostWithOptionalFieldsNull() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("10.0");
        when(request.getParameter("pricePerGallon")).thenReturn("4.00");
        when(request.getParameter("engineHours")).thenReturn(null);
        when(request.getParameter("location")).thenReturn(null);
        when(request.getParameter("notes")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.createFuelUp(any(FuelUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(fuelUpService).createFuelUp(any(FuelUp.class));
        String output = stringWriter.toString();
        assertTrue(output.contains("Success"));
    }

    @Test
    void testDoPostWithEmptyEngineHours() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("10.0");
        when(request.getParameter("pricePerGallon")).thenReturn("4.00");
        when(request.getParameter("engineHours")).thenReturn("");
        when(request.getParameter("location")).thenReturn("Marina");
        when(request.getParameter("notes")).thenReturn("Notes");
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.createFuelUp(any(FuelUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(fuelUpService).createFuelUp(any(FuelUp.class));
    }

    @Test
    void testDoPostHandlesException() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("15.5");
        when(request.getParameter("pricePerGallon")).thenReturn("3.89");
        when(request.getParameter("engineHours")).thenReturn(null);
        when(request.getParameter("location")).thenReturn(null);
        when(request.getParameter("notes")).thenReturn(null);
        when(fuelUpService.createFuelUp(any(FuelUp.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error adding fuel-up"));
    }

    @Test
    void testDoPostInvalidDateFormat() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("invalid-date");
        when(request.getParameter("gallons")).thenReturn("15.5");
        when(request.getParameter("pricePerGallon")).thenReturn("3.89");

        // Act
        servlet.doPost(request, response);

        // Assert - servlet catches exception and sends error
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error adding fuel-up"));
    }

    @Test
    void testDoPostInvalidNumberFormat() throws Exception {
        // Arrange
        String userId = "test-user-123";

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(request.getParameter("date")).thenReturn("2024-01-15");
        when(request.getParameter("gallons")).thenReturn("not-a-number");
        when(request.getParameter("pricePerGallon")).thenReturn("3.89");

        // Act
        servlet.doPost(request, response);

        // Assert - servlet catches exception and sends error
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), contains("Error adding fuel-up"));
    }

    @Test
    void testSessionManagement() throws Exception {
        // Arrange
        String userId = "test-user-123";
        List<FuelUp> fuelUps = new ArrayList<>();

        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(response.getWriter()).thenReturn(printWriter);
        when(fuelUpService.getFuelUpsByUser(userId)).thenReturn(fuelUps);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request).getSession(true);
        verify(session).getAttribute("userId");
    }

    private List<FuelUp> createTestFuelUps() {
        List<FuelUp> fuelUps = new ArrayList<>();

        User user = new User();
        user.setUserId("test-user-123");
        user.setEmail("test@example.com");

        FuelUp fuelUp = new FuelUp();
        fuelUp.setId(1L);
        fuelUp.setUser(user);
        fuelUp.setDate(new Date());
        fuelUp.setGallons(new BigDecimal("15.5"));
        fuelUp.setPricePerGallon(new BigDecimal("3.89"));
        fuelUp.setTotalCost(new BigDecimal("60.295"));

        fuelUps.add(fuelUp);
        return fuelUps;
    }
}
