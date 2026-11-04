package com.college.invoice.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {

    public static final String STATUS_DRAFT  = "DRAFT";
    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PAID   = "PAID";

    private int id;
    private String invoiceNumber;
    private Integer customerId;         // can be null if the customer was deleted later
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private LocalDate invoiceDate;
    private BigDecimal taxRate = BigDecimal.ZERO;      // percent, e.g. 10.00
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String status = STATUS_DRAFT;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    /** Returns true only for DRAFT, UNPAID or PAID. */
    public static boolean isValidStatus(String s) {
        return STATUS_DRAFT.equals(s) || STATUS_UNPAID.equals(s) || STATUS_PAID.equals(s);
    }

    /** Calculates every line total, then subtotal, tax amount and grand total. */
    public void calculateTotals() {
        if (taxRate == null) {
            taxRate = BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            item.calculateLineTotal();
            sum = sum.add(item.getLineTotal());
        }
        subtotal = sum.setScale(2, RoundingMode.HALF_UP);
        taxAmount = subtotal.multiply(taxRate)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        grandTotal = subtotal.add(taxAmount);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}