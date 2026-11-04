package com.college.invoice.dao;

import com.college.invoice.model.Invoice;
import com.college.invoice.model.InvoiceItem;
import com.college.invoice.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    // ---------- helper: convert one database row to an Invoice object ----------
    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));

        int customerId = rs.getInt("customer_id");
        inv.setCustomerId(rs.wasNull() ? null : customerId);

        inv.setCustomerName(rs.getString("customer_name"));
        inv.setCustomerEmail(rs.getString("customer_email"));
        inv.setCustomerPhone(rs.getString("customer_phone"));
        inv.setCustomerAddress(rs.getString("customer_address"));
        inv.setInvoiceDate(rs.getDate("invoice_date").toLocalDate());
        inv.setTaxRate(rs.getBigDecimal("tax_rate"));
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setTaxAmount(rs.getBigDecimal("tax_amount"));
        inv.setGrandTotal(rs.getBigDecimal("grand_total"));
        inv.setStatus(rs.getString("status"));
        return inv;
    }

    private InvoiceItem mapItem(ResultSet rs) throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setId(rs.getInt("id"));
        item.setInvoiceId(rs.getInt("invoice_id"));

        int productId = rs.getInt("product_id");
        item.setProductId(rs.wasNull() ? null : productId);

        item.setProductName(rs.getString("product_name"));
        item.setDescription(rs.getString("description"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setLineTotal(rs.getBigDecimal("line_total"));
        return item;
    }

    // ---------- list of all invoices (without items), newest first ----------
    public List<Invoice> findAll() throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapInvoice(rs));
            }
        }
        return list;
    }

    // ---------- one invoice WITH its items; returns null if not found ----------
    public Invoice findById(int id) throws SQLException {
        Invoice invoice = null;
        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM invoices WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        invoice = mapInvoice(rs);
                    }
                }
            }

            if (invoice == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY id")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        invoice.getItems().add(mapItem(rs));
                    }
                }
            }
        }
        return invoice;
    }

    // ---------- save invoice + items in ONE transaction ----------
    public void insert(Invoice invoice) throws SQLException {
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new IllegalArgumentException("An invoice must have at least one item.");
        }
        invoice.calculateTotals();   // always recalculate on the server

        String insertInvoice =
            "INSERT INTO invoices (invoice_number, customer_id, customer_name, customer_email, "
          + "customer_phone, customer_address, invoice_date, tax_rate, subtotal, tax_amount, "
          + "grand_total, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertItem =
            "INSERT INTO invoice_items (invoice_id, product_id, product_name, description, "
          + "quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);                       // START transaction

            // 1) insert the invoice with a temporary unique number
            int invoiceId;
            try (PreparedStatement ps = con.prepareStatement(
                    insertInvoice, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "TEMP-" + System.nanoTime());
                if (invoice.getCustomerId() == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, invoice.getCustomerId());
                }
                ps.setString(3, invoice.getCustomerName());
                ps.setString(4, invoice.getCustomerEmail());
                ps.setString(5, invoice.getCustomerPhone());
                ps.setString(6, invoice.getCustomerAddress());
                ps.setDate(7, Date.valueOf(invoice.getInvoiceDate()));
                ps.setBigDecimal(8, invoice.getTaxRate());
                ps.setBigDecimal(9, invoice.getSubtotal());
                ps.setBigDecimal(10, invoice.getTaxAmount());
                ps.setBigDecimal(11, invoice.getGrandTotal());
                ps.setString(12, invoice.getStatus());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Could not get the new invoice id.");
                    }
                    invoiceId = keys.getInt(1);
                }
            }

            // 2) replace the temporary number with the real one, e.g. INV-2026-0007
            String invoiceNumber = String.format("INV-%d-%04d",
                    invoice.getInvoiceDate().getYear(), invoiceId);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE invoices SET invoice_number = ? WHERE id = ?")) {
                ps.setString(1, invoiceNumber);
                ps.setInt(2, invoiceId);
                ps.executeUpdate();
            }

            // 3) insert all line items
            try (PreparedStatement ps = con.prepareStatement(insertItem)) {
                for (InvoiceItem item : invoice.getItems()) {
                    ps.setInt(1, invoiceId);
                    if (item.getProductId() == null) {
                        ps.setNull(2, Types.INTEGER);
                    } else {
                        ps.setInt(2, item.getProductId());
                    }
                    ps.setString(3, item.getProductName());
                    ps.setString(4, item.getDescription());
                    ps.setInt(5, item.getQuantity());
                    ps.setBigDecimal(6, item.getUnitPrice());
                    ps.setBigDecimal(7, item.getLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();                                   // everything saved together
            invoice.setId(invoiceId);
            invoice.setInvoiceNumber(invoiceNumber);

        } catch (SQLException | RuntimeException e) {
            rollbackQuietly(con);                           // nothing is saved
            throw e;
        } finally {
            closeQuietly(con);
        }
    }

    // ---------- change status (DRAFT / UNPAID / PAID) ----------
    public boolean updateStatus(int id, String status) throws SQLException {
        if (!Invoice.isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        String sql = "UPDATE invoices SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- delete (items are removed automatically by ON DELETE CASCADE) ----------
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- numbers for the dashboard ----------
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Total grand_total of all invoices with this status (0.00 if none). */
    public BigDecimal sumByStatus(String status) throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM invoices WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    // ---------- small helpers for the transaction ----------
    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }

    private void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }
}