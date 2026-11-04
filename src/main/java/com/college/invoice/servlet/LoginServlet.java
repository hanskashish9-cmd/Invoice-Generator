package com.college.invoice.servlet;

import com.college.invoice.dao.UserDAO;
import com.college.invoice.filter.AuthFilter;
import com.college.invoice.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/login.jsp";
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;   // 30 minutes

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        if ("1".equals(request.getParameter("logout"))) {
            request.setAttribute("message", "You have been logged out successfully.");
        }
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username") == null
                ? "" : request.getParameter("username").trim();
        String password = request.getParameter("password") == null
                ? "" : request.getParameter("password");

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError(request, response, username,
                    "Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                showLoginError(request, response, username, "Invalid username or password.");
                return;
            }

            // Throw away any old session and start a fresh one
            HttpSession old = request.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

            response.sendRedirect(request.getContextPath() + "/dashboard");

        } catch (SQLException e) {
            getServletContext().log("Login failed because of a database error", e);
            showLoginError(request, response, username,
                    "Cannot connect to the database. Check that MySQL is running "
                  + "and that db.properties is correct.");
        }
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response,
                                String username, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("username", username);
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }
}
