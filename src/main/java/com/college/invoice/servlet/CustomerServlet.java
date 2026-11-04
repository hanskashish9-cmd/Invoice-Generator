package com.college.invoice.servlet;

import com.college.invoice.dao.CustomerDAO;
import com.college.invoice.model.Customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {

    private static final String LIST_VIEW = "/WEB-INF/views/customers/list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/customers/form.jsp";
    private static final String ERROR_VIEW = "/WEB-INF/views/error.jsp";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-() ]{5,20}$");

    private final CustomerDAO customerDAO = new CustomerDAO();

    // =====================================================================
    //  GET: list / new / edit
    // =====================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("new".equals(action)) {
                showForm(request, response, new Customer(), new LinkedHashMap<>());
            } else if ("edit".equals(action)) {
                Customer c = customerDAO.findById(parseId(request.getParameter("id")));
                if (c == null) {
                    flashError(request, "Customer not found.");
                    redirectToList(request, response);
                } else {
                    showForm(request, response, c, new LinkedHashMap<>());
                }
            } else {
                request.setAttribute("customers", customerDAO.findAll());
                request.getRequestDispatcher(LIST_VIEW).forward(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while loading customers.", e);
        }
    }

    // =====================================================================
    //  POST: save / delete
    // =====================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        try {
            if ("save".equals(action)) {
                saveCustomer(request, response);
            } else if ("delete".equals(action)) {
                deleteCustomer(request, response);
            } else {
                redirectToList(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while saving customer data.", e);
        }
    }

    private void saveCustomer(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Customer c = new Customer();
        c.setId(parseId(request.getParameter("id")));          // 0 means "new customer"
        c.setName(trim(request.getParameter("name")));
        c.setEmail(blankToNull(request.getParameter("email")));
        c.setPhone(blankToNull(request.getParameter("phone")));
        c.setAddress(blankToNull(request.getParameter("address")));

        Map<String, String> errors = validate(c);
        if (!errors.isEmpty()) {
            showForm(request, response, c, errors);             // show the form again with messages
            return;
        }

        if (c.getId() == 0) {
            customerDAO.insert(c);
            flashSuccess(request, "Customer \"" + c.getName() + "\" was added.");
        } else if (customerDAO.update(c)) {
            flashSuccess(request, "Customer \"" + c.getName() + "\" was updated.");
        } else {
            flashError(request, "Customer not found, nothing was updated.");
        }
        redirectToList(request, response);                      // redirect after successful POST
    }

    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        if (customerDAO.delete(parseId(request.getParameter("id")))) {
            flashSuccess(request, "Customer was deleted. Old invoices keep their saved customer details.");
        } else {
            flashError(request, "Customer not found.");
        }
        redirectToList(request, response);
    }

    // =====================================================================
    //  Validation
    // =====================================================================
    private Map<String, String> validate(Customer c) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (c.getName().isEmpty()) {
            errors.put("name", "Customer name is required.");
        } else if (c.getName().length() > 100) {
            errors.put("name", "Name must be at most 100 characters.");
        }
        if (c.getEmail() != null
                && (c.getEmail().length() > 100 || !EMAIL_PATTERN.matcher(c.getEmail()).matches())) {
            errors.put("email", "Enter a valid email address (max 100 characters).");
        }
        if (c.getPhone() != null && !PHONE_PATTERN.matcher(c.getPhone()).matches()) {
            errors.put("phone", "Phone must be 5 to 20 characters: digits, spaces, + - ( ) only.");
        }
        if (c.getAddress() != null && c.getAddress().length() > 255) {
            errors.put("address", "Address must be at most 255 characters.");
        }
        return errors;
    }

    // =====================================================================
    //  Helpers
    // =====================================================================
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Customer customer, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("customer", customer);
        request.setAttribute("errors", errors);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           String message, Exception e) throws ServletException, IOException {
        getServletContext().log(message, e);
        request.setAttribute("errorMessage", message);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        request.getRequestDispatcher(ERROR_VIEW).forward(request, response);
    }

    private void flashSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    private void flashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }

    private static int parseId(String text) {
        try {
            int v = Integer.parseInt(trim(text));
            return v > 0 ? v : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String blankToNull(String s) {
        String t = trim(s);
        return t.isEmpty() ? null : t;
    }
}