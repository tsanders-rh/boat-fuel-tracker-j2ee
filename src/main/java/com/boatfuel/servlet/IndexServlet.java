package com.boatfuel.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves the index page with dynamic username injection
 */
public class IndexServlet extends HttpServlet {

    private String template;

    @Override
    public void init() throws ServletException {
        try {
            // Load the HTML template
            InputStream is = getServletContext().getResourceAsStream("/index-template.html");
            template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ServletException("Cannot load index template", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        // Get the authenticated user
        String username = request.getRemoteUser();
        if (username == null) {
            username = "Unknown";
        }

        // Replace placeholder with actual username
        String html = template.replace("{{USERNAME}}", username);

        response.getWriter().print(html);
    }
}
