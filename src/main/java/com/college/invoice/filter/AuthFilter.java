package com.college.invoice.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    /** Name of the session attribute that stores the logged-in User. */
    public static final String SESSION_USER = "loggedUser";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        // Pages that anyone may open without logging in
        boolean isPublic = path.equals("/login") || path.startsWith("/css/");

        HttpSession session = request.getSession(false);   // do not create a new session
        boolean loggedIn = session != null && session.getAttribute(SESSION_USER) != null;

        if (isPublic || loggedIn) {
            if (!isPublic) {
                // stop the browser from caching protected pages
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
            }
            chain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}