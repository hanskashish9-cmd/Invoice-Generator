package com.college.invoice.servlet;

import com.college.invoice.dao.CustomerDAO;
import com.college.invoice.dao.InvoiceDAO;
import com.college.invoice.dao.ProductDAO;
import com.college.invoice.model.Invoice;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("customerCount", customerDAO.count());
            request.setAttribute("productCount", productDAO.count());
            request.setAttribute("invoiceCount", invoiceDAO.count());

            request.setAttribute("draftCount", invoiceDAO.countByStatus(Invoice.STATUS_DRAFT));
            request.setAttribute("unpaidCount", invoiceDAO.countByStatus(Invoice.STATUS_UNPAID));
            request.setAttribute("paidCount", invoiceDAO.countByStatus(Invoice.STATUS_PAID));

            request.setAttribute("unpaidAmount", invoiceDAO.sumByStatus(Invoice.STATUS_UNPAID));
            request.setAttribute("paidAmount", invoiceDAO.sumByStatus(Invoice.STATUS_PAID));

            List<Invoice> all = invoiceDAO.findAll();
            request.setAttribute("recentInvoices",
                    all.size() > 5 ? all.subList(0, 5) : all);

            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log("Dashboard database error", e);
            request.setAttribute("errorMessage",
                    "Could not load the dashboard. Please check the database connection.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}