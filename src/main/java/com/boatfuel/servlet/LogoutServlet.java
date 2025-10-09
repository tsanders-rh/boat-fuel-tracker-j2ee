package com.boatfuel.servlet;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Legacy logout servlet (anti-pattern)
 * Konveyor violations:
 * - Manual session invalidation
 * - HTTP Basic Auth can't be truly cleared from browser
 * - No modern security framework
 */
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("User logout requested");

        // Invalidate session (anti-pattern: manual session management)
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("userId");
            logger.info("Invalidating session for user: " + userId);
            session.invalidate();
        }

        // Send 401 to clear HTTP Basic Auth credentials
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=\"Boat Fuel Tracker\"");
        response.setContentType("text/html");

        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Logged Out</h2>");
        response.getWriter().println("<p>You have been logged out. Please close your browser to complete logout.</p>");
        response.getWriter().println("<p><a href=\"" + request.getContextPath() + "/\">Login Again</a></p>");
        response.getWriter().println("</body></html>");
    }
}
