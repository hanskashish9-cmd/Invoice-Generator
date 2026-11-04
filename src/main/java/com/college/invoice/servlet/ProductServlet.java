package com.college.invoice.servlet;

import com.college.invoice.dao.ProductDAO;
import com.college.invoice.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private static final String LIST_VIEW = "/WEB-INF/views/products/list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/products/form.jsp";
    private static final String ERROR_VIEW = "/WEB-INF/views/error.jsp";

    /** Largest price the DECIMAL(10,2) column can hold. */
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final ProductDAO productDAO = new ProductDAO();

    // =====================================================================
    //  GET: list / new / edit
    // =====================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("new".equals(action)) {
                showForm(request, response, new Product(), "", new LinkedHashMap<>());
            } else if ("edit".equals(action)) {
                Product p = productDAO.findById(parseId(request.getParameter("id")));
                if (p == null) {
                    flashError(request, "Product not found.");
                    redirectToList(request, response);
                } else {
                    showForm(request, response, p, p.getUnitPrice().toPlainString(),
                             new LinkedHashMap<>());
                }
            } else {
                request.setAttribute("products", productDAO.findAll());
                request.getRequestDispatcher(LIST_VIEW).forward(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while loading products.", e);
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
                saveProduct(request, response);
            } else if ("delete".equals(action)) {
                deleteProduct(request, response);
            } else {
                redirectToList(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while saving product data.", e);
        }
    }

    private void saveProduct(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String priceInput = trim(request.getParameter("unitPrice"));

        Product p = new Product();
        p.setId(parseId(request.getParameter("id")));           // 0 means "new product"
        p.setName(trim(request.getParameter("name")));
        p.setDescription(blankToNull(request.getParameter("description")));

        Map<String, String> errors = new LinkedHashMap<>();

        if (p.getName().isEmpty()) {
            errors.put("name", "Name is required.");
        } else if (p.getName().length() > 120) {
            errors.put("name", "Name must be at most 120 characters.");
        }
        if (p.getDescription() != null && p.getDescription().length() > 500) {
            errors.put("description", "Description must be at most 500 characters.");
        }
        if (priceInput.isEmpty()) {
            errors.put("unitPrice", "Unit price is required.");
        } else {
            BigDecimal price = parseDecimal(priceInput, MAX_PRICE);
            if (price == null) {
                errors.put("unitPrice",
                        "Enter a valid price, 0 or more, with at most 2 decimal places (e.g. 499.99).");
            } else {
                p.setUnitPrice(price);
            }
        }

        if (!errors.isEmpty()) {
            showForm(request, response, p, priceInput, errors);
            return;
        }

        if (p.getId() == 0) {
            productDAO.insert(p);
            flashSuccess(request, "Product \"" + p.getName() + "\" was added.");
        } else if (productDAO.update(p)) {
            flashSuccess(request, "Product \"" + p.getName() + "\" was updated.");
        } else {
            flashError(request, "Product not found, nothing was updated.");
        }
        redirectToList(request, response);
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        if (productDAO.delete(parseId(request.getParameter("id")))) {
            flashSuccess(request, "Product was deleted. Old invoices keep their saved item details.");
        } else {
            flashError(request, "Product not found.");
        }
        redirectToList(request, response);
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    /** Returns the number with 2 decimals, or null if it is invalid, negative, too big or has >2 decimals. */
    private static BigDecimal parseDecimal(String text, BigDecimal max) {
        try {
            BigDecimal v = new BigDecimal(text.trim());
            if (v.signum() < 0 || v.compareTo(max) > 0 || v.stripTrailingZeros().scale() > 2) {
                return null;
            }
            return v.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Product product, String priceInput, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("product", product);
        request.setAttribute("priceInput", priceInput);         // exactly what the user typed
        request.setAttribute("errors", errors);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/products");
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