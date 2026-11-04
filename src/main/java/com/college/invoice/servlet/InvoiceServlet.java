package com.college.invoice.servlet;

import com.college.invoice.dao.CustomerDAO;
import com.college.invoice.dao.InvoiceDAO;
import com.college.invoice.dao.ProductDAO;
import com.college.invoice.model.Customer;
import com.college.invoice.model.Invoice;
import com.college.invoice.model.InvoiceItem;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/invoices")
public class InvoiceServlet extends HttpServlet {

    private static final String LIST_VIEW    = "/WEB-INF/views/invoices/list.jsp";
    private static final String FORM_VIEW    = "/WEB-INF/views/invoices/form.jsp";
    private static final String DETAIL_VIEW  = "/WEB-INF/views/invoices/view.jsp";
    private static final String CONFIRM_VIEW = "/WEB-INF/views/invoices/confirm-delete.jsp";
    private static final String ERROR_VIEW   = "/WEB-INF/views/error.jsp";

    /** Tax rate (in percent) shown by default on the new-invoice form. Change it here. */
    private static final String DEFAULT_TAX_RATE = "10.00";

    private static final int ROW_COUNT = 5;          // blank item rows shown on the form
    private static final int MAX_ROWS = 50;          // most item rows accepted in one invoice
    private static final int MAX_QUANTITY = 10000;
    private static final BigDecimal MAX_UNIT_PRICE = new BigDecimal("99999999.99");
    private static final BigDecimal MAX_TAX_RATE = new BigDecimal("100");
    private static final BigDecimal MAX_TOTAL = new BigDecimal("9999999999.99");   // DECIMAL(12,2) limit

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /** One item row exactly as the user typed it (so the form can be shown again after an error). */
    public static class RowInput {
        private final String productId;
        private final String quantity;
        private final String unitPrice;

        public RowInput(String productId, String quantity, String unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        public String getProductId() { return productId; }
        public String getQuantity() { return quantity; }
        public String getUnitPrice() { return unitPrice; }
    }

    // =====================================================================
    //  GET: list / new / view / confirmDelete
    // =====================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("new".equals(action)) {
                showNewForm(request, response);
            } else if ("view".equals(action)) {
                showInvoice(request, response, DETAIL_VIEW);
            } else if ("confirmDelete".equals(action)) {
                showInvoice(request, response, CONFIRM_VIEW);
            } else {
                request.setAttribute("invoices", invoiceDAO.findAll());
                request.getRequestDispatcher(LIST_VIEW).forward(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while loading invoices.", e);
        }
    }

    /** Loads one invoice (with items) and forwards to the given page. */
    private void showInvoice(HttpServletRequest request, HttpServletResponse response, String view)
            throws SQLException, ServletException, IOException {
        Invoice invoice = invoiceDAO.findById(parseId(request.getParameter("id")));
        if (invoice == null) {
            flashError(request, "Invoice not found.");
            redirectToList(request, response);
            return;
        }
        request.setAttribute("invoice", invoice);
        request.getRequestDispatcher(view).forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        showForm(request, response,
                "", LocalDate.now().toString(), DEFAULT_TAX_RATE, Invoice.STATUS_DRAFT,
                blankRows(), new LinkedHashMap<>(), new ArrayList<>());
    }

    // =====================================================================
    //  POST: save / updateStatus / delete
    // =====================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        try {
            if ("save".equals(action)) {
                saveInvoice(request, response);
            } else if ("updateStatus".equals(action)) {
                updateStatus(request, response);
            } else if ("delete".equals(action)) {
                deleteInvoice(request, response);
            } else {
                redirectToList(request, response);
            }
        } catch (SQLException e) {
            showError(request, response, "Database error while saving invoice data.", e);
        }
    }

    // ---------------------------------------------------------------------
    //  Create invoice
    // ---------------------------------------------------------------------
    private void saveInvoice(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String customerIdInput = trim(request.getParameter("customerId"));
        String dateInput = trim(request.getParameter("invoiceDate"));
        String taxInput = trim(request.getParameter("taxRate"));
        String statusInput = trim(request.getParameter("status"));
        if (statusInput.isEmpty()) {
            statusInput = Invoice.STATUS_DRAFT;
        }

        Map<String, String> errors = new LinkedHashMap<>();
        List<String> itemErrors = new ArrayList<>();

        // ----- customer -----
        Customer customer = null;
        int customerId = parseId(customerIdInput);
        if (customerId == 0) {
            errors.put("customerId", "Please select a customer.");
        } else {
            customer = customerDAO.findById(customerId);
            if (customer == null) {
                errors.put("customerId", "The selected customer was not found.");
            }
        }

        // ----- date -----
        LocalDate invoiceDate = null;
        if (dateInput.isEmpty()) {
            errors.put("invoiceDate", "Invoice date is required.");
        } else {
            try {
                invoiceDate = LocalDate.parse(dateInput);       // format yyyy-MM-dd
                if (invoiceDate.getYear() < 2000 || invoiceDate.getYear() > 2100) {
                    errors.put("invoiceDate", "Year must be between 2000 and 2100.");
                }
            } catch (DateTimeParseException e) {
                errors.put("invoiceDate", "Enter a valid date.");
            }
        }

        // ----- tax rate -----
        BigDecimal taxRate = taxInput.isEmpty() ? null : parseDecimal(taxInput, MAX_TAX_RATE);
        if (taxRate == null) {
            errors.put("taxRate", "Enter a tax rate from 0 to 100 (up to 2 decimal places).");
        }

        // ----- status -----
        if (!Invoice.isValidStatus(statusInput)) {
            errors.put("status", "Choose Draft, Unpaid or Paid.");
        }

        // ----- item rows (3 parallel arrays: productId[], quantity[], unitPrice[]) -----
        String[] productIds = valuesOf(request, "productId");
        String[] quantities = valuesOf(request, "quantity");
        String[] unitPrices = valuesOf(request, "unitPrice");

        int rowCount = Math.max(productIds.length, Math.max(quantities.length, unitPrices.length));
        if (rowCount > MAX_ROWS) {
            itemErrors.add("Too many rows. An invoice can have at most " + MAX_ROWS + " items.");
            rowCount = MAX_ROWS;
        }

        Map<Integer, Product> productMap = loadProductMap();
        List<RowInput> rows = new ArrayList<>();
        List<InvoiceItem> items = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            String pid = valueAt(productIds, i);
            String qty = valueAt(quantities, i);
            String price = valueAt(unitPrices, i);
            rows.add(new RowInput(pid, qty, price));

            if (pid.isEmpty() && qty.isEmpty() && price.isEmpty()) {
                continue;                                       // completely empty row: ignore
            }
            String label = "Row " + (i + 1) + ": ";

            int productId = parseId(pid);
            Product product = productId == 0 ? null : productMap.get(productId);
            if (product == null) {
                itemErrors.add(label + "please select a product or service.");
                continue;
            }

            int quantity = parseQuantity(qty);
            if (quantity == 0) {
                itemErrors.add(label + "quantity must be a whole number from 1 to " + MAX_QUANTITY + ".");
                continue;
            }

            BigDecimal unitPrice;
            if (price.isEmpty()) {
                unitPrice = product.getUnitPrice();             // leave blank = use the product's price
            } else {
                unitPrice = parseDecimal(price, MAX_UNIT_PRICE);
                if (unitPrice == null) {
                    itemErrors.add(label + "unit price must be 0 or more, with at most 2 decimal places.");
                    continue;
                }
            }

            InvoiceItem item = new InvoiceItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());             // copied (snapshot) into the invoice
            item.setDescription(product.getDescription());
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            items.add(item);
        }

        if (items.isEmpty() && itemErrors.isEmpty()) {
            itemErrors.add("Add at least one product or service to the invoice.");
        }
        while (rows.size() < ROW_COUNT) {
            rows.add(new RowInput("", "", ""));
        }

        // ----- build the invoice and calculate totals on the server -----
        Invoice invoice = null;
        if (errors.isEmpty() && itemErrors.isEmpty()) {
            invoice = new Invoice();
            invoice.setCustomerId(customer.getId());
            invoice.setCustomerName(customer.getName());        // copied (snapshot) into the invoice
            invoice.setCustomerEmail(customer.getEmail());
            invoice.setCustomerPhone(customer.getPhone());
            invoice.setCustomerAddress(customer.getAddress());
            invoice.setInvoiceDate(invoiceDate);
            invoice.setTaxRate(taxRate);
            invoice.setStatus(statusInput);
            invoice.setItems(items);
            invoice.calculateTotals();

            if (invoice.getGrandTotal().compareTo(MAX_TOTAL) > 0) {
                itemErrors.add("The invoice total is too large. Reduce quantities or prices.");
            }
        }

        if (!errors.isEmpty() || !itemErrors.isEmpty()) {
            showForm(request, response, customerIdInput, dateInput, taxInput, statusInput,
                     rows, errors, itemErrors);
            return;
        }

        invoiceDAO.insert(invoice);                             // transaction: invoice + items together

        flashSuccess(request, "Invoice " + invoice.getInvoiceNumber() + " was created.");
        response.sendRedirect(request.getContextPath()
                + "/invoices?action=view&id=" + invoice.getId());
    }

    // ---------------------------------------------------------------------
    //  Update status
    // ---------------------------------------------------------------------
    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseId(request.getParameter("id"));
        String status = trim(request.getParameter("status"));
        boolean backToList = "list".equals(request.getParameter("returnTo"));

        if (!Invoice.isValidStatus(status)) {
            flashError(request, "Invalid status.");
        } else if (invoiceDAO.updateStatus(id, status)) {
            flashSuccess(request, "Invoice status changed to " + status + ".");
            if (!backToList) {
                response.sendRedirect(request.getContextPath() + "/invoices?action=view&id=" + id);
                return;
            }
        } else {
            flashError(request, "Invoice not found.");
        }
        redirectToList(request, response);
    }

    // ---------------------------------------------------------------------
    //  Delete (only after the confirmation page)
    // ---------------------------------------------------------------------
    private void deleteInvoice(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseId(request.getParameter("id"));

        if (!"yes".equals(request.getParameter("confirm"))) {
            // not confirmed: send the user to the confirmation page instead
            response.sendRedirect(request.getContextPath() + "/invoices?action=confirmDelete&id=" + id);
            return;
        }
        if (invoiceDAO.delete(id)) {
            flashSuccess(request, "Invoice was deleted.");
        } else {
            flashError(request, "Invoice not found.");
        }
        redirectToList(request, response);
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    /** Puts everything the invoice form needs into the request and shows the form. */
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          String customerIdInput, String invoiceDateInput, String taxRateInput,
                          String statusInput, List<RowInput> rows,
                          Map<String, String> errors, List<String> itemErrors)
            throws SQLException, ServletException, IOException {
        request.setAttribute("customers", customerDAO.findAll());
        request.setAttribute("products", productDAO.findAll());
        request.setAttribute("customerIdInput", customerIdInput);
        request.setAttribute("invoiceDateInput", invoiceDateInput);
        request.setAttribute("taxRateInput", taxRateInput);
        request.setAttribute("statusInput", statusInput);
        request.setAttribute("rows", rows);
        request.setAttribute("errors", errors);
        request.setAttribute("itemErrors", itemErrors);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private List<RowInput> blankRows() {
        List<RowInput> rows = new ArrayList<>();
        for (int i = 0; i < ROW_COUNT; i++) {
            rows.add(new RowInput("", "", ""));
        }
        return rows;
    }

    private Map<Integer, Product> loadProductMap() throws SQLException {
        Map<Integer, Product> map = new HashMap<>();
        for (Product p : productDAO.findAll()) {
            map.put(p.getId(), p);
        }
        return map;
    }

    /** Returns the number with 2 decimals, or null if invalid, negative, too big or has >2 decimals. */
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

    /** Returns a quantity from 1 to MAX_QUANTITY, or 0 if the text is not valid. */
    private static int parseQuantity(String text) {
        try {
            int q = Integer.parseInt(trim(text));
            return (q >= 1 && q <= MAX_QUANTITY) ? q : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String[] valuesOf(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return values == null ? new String[0] : values;
    }

    private static String valueAt(String[] values, int index) {
        return (index < values.length && values[index] != null) ? values[index].trim() : "";
    }

    private void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/invoices");
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
}