package com.boatfuel.servlet;

import com.boatfuel.ejb.FuelUpService;
import com.boatfuel.entity.FuelUp;
import com.boatfuel.util.FileSystemHelper;
import com.boatfuel.util.JNDILookupHelper;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Legacy Servlet using old patterns (anti-pattern)
 * Konveyor violations:
 * - Extends HttpServlet (old style, should use @WebServlet)
 * - Manual EJB lookup with JNDI
 * - No dependency injection
 * - Session management in servlet
 * - Using PrintWriter instead of JSP/templating
 */
public class FuelUpServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FuelUpServlet.class);

    private FuelUpService fuelUpService;

    /**
     * Initialize servlet with EJB lookup (anti-pattern)
     * Konveyor will flag: Manual JNDI EJB lookup, should use @EJB injection
     */
    @Override
    public void init() throws ServletException {
        try {
            logger.info("Initializing FuelUpServlet");

            // Manual JNDI lookup (anti-pattern - should use @EJB injection)
            Context ctx = new InitialContext();
            fuelUpService = (FuelUpService) ctx.lookup("java:global/boat-fuel-tracker/FuelUpService");

            logger.info("FuelUpService EJB lookup successful");

        } catch (Exception e) {
            logger.error("Failed to initialize EJB", e);
            throw new ServletException("Cannot initialize EJB", e);
        }
    }

    /**
     * Handle GET requests with HTML generation in servlet (anti-pattern)
     * Konveyor will flag: HTML in servlet, should use JSP/JSF/templates
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            // For testing: set a default userId if not in session
            userId = "testuser";
            session.setAttribute("userId", userId);
            logger.info("No userId in session, using default: " + userId);
        }

        try {
            // Audit log to file system (anti-pattern)
            FileSystemHelper.writeAuditLog("User " + userId + " accessed fuel-ups");

            List<FuelUp> fuelUps = fuelUpService.getFuelUpsByUser(userId);

            // Generating HTML in servlet (anti-pattern)
            out.println("<html>");
            out.println("<head><title>Boat Fuel Tracker</title></head>");
            out.println("<body>");
            out.println("<h1>Your Fuel-Ups</h1>");
            out.println("<table border='1'>");
            out.println("<tr><th>Date</th><th>Gallons</th><th>Price/Gal</th><th>Total</th></tr>");

            for (FuelUp fuelUp : fuelUps) {
                out.println("<tr>");
                out.println("<td>" + fuelUp.getDate() + "</td>");
                out.println("<td>" + fuelUp.getGallons() + "</td>");
                out.println("<td>$" + fuelUp.getPricePerGallon() + "</td>");
                out.println("<td>$" + fuelUp.getTotalCost() + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            logger.error("Error retrieving fuel-ups", e);
            out.println("<h1>Error: " + e.getMessage() + "</h1>");
        }
    }

    /**
     * Handle POST with manual session management (anti-pattern)
     * Konveyor will flag: Manual parameter extraction, no validation framework
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            // For testing: set a default userId if not in session
            userId = "testuser";
            session.setAttribute("userId", userId);
            logger.info("No userId in session, using default: " + userId);
        }

        try {
            // Manual parameter extraction (anti-pattern - should use form validation framework)
            String dateStr = request.getParameter("date");
            String gallonsStr = request.getParameter("gallons");
            String pricePerGallonStr = request.getParameter("pricePerGallon");
            String engineHoursStr = request.getParameter("engineHours");
            String location = request.getParameter("location");
            String notes = request.getParameter("notes");

            logger.info("Processing fuel-up submission from user: " + userId);
            logger.info("Date: " + dateStr + ", Gallons: " + gallonsStr + ", Price: " + pricePerGallonStr);

            // Manual parsing with no validation (anti-pattern)
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            BigDecimal gallons = new BigDecimal(gallonsStr);
            BigDecimal pricePerGallon = new BigDecimal(pricePerGallonStr);
            BigDecimal engineHours = (engineHoursStr != null && !engineHoursStr.isEmpty())
                ? new BigDecimal(engineHoursStr) : null;
            BigDecimal totalCost = gallons.multiply(pricePerGallon);

            // Create FuelUp entity
            FuelUp fuelUp = new FuelUp();
            fuelUp.setDate(date);
            fuelUp.setGallons(gallons);
            fuelUp.setPricePerGallon(pricePerGallon);
            fuelUp.setTotalCost(totalCost);
            fuelUp.setEngineHours(engineHours);
            fuelUp.setLocation(location);
            fuelUp.setNotes(notes);

            // Set user (anti-pattern: direct entity manipulation)
            com.boatfuel.entity.User user = new com.boatfuel.entity.User();
            user.setUserId(userId);
            fuelUp.setUser(user);

            // Save via EJB
            fuelUpService.createFuelUp(fuelUp);

            // Audit log to file system (anti-pattern)
            FileSystemHelper.writeAuditLog("User " + userId + " added fuel-up: " + gallons + " gallons");

            logger.info("Fuel-up added successfully");

            // Return success response
            out.println("<html><body><h1>Success</h1></body></html>");

        } catch (Exception e) {
            logger.error("Error adding fuel-up", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error adding fuel-up: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying FuelUpServlet");
        super.destroy();
    }
}
