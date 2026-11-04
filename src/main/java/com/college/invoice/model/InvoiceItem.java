package com.college.invoice.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceItem {
    private int id;
    private int invoiceId;
    private Integer productId;          // can be null if the product was deleted later
    private String productName;
    private String description;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public InvoiceItem() {}

    /** line total = unit price x quantity (2 decimal places) */
    public void calculateLineTotal() {
        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }
        lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                             .setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}