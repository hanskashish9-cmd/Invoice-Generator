<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Invoice ${invoice.invoiceNumber}" />
<c:set var="activeMenu" value="invoices" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head no-print">
    <h1>Invoice Details</h1>
    <div>
        <a class="btn btn-light" href="${ctx}/invoices">Back to list</a>
        <button type="button" class="btn btn-light" onclick="window.print()">Print</button>
        <a class="btn btn-danger" href="${ctx}/invoices?action=confirmDelete&amp;id=${invoice.id}">Delete</a>
    </div>
</div>

<div class="card invoice">

    <div class="invoice-top">
        <div>
            <h2 class="invoice-title">INVOICE</h2>
            <div class="invoice-meta">
                <div><span class="meta-label">Invoice No.:</span> <strong><c:out value="${invoice.invoiceNumber}" /></strong></div>
                <div><span class="meta-label">Invoice Date:</span> <c:out value="${invoice.invoiceDate}" /></div>
                <div><span class="meta-label">Status:</span>
                    <span class="badge badge-${fn:toLowerCase(invoice.status)}"><c:out value="${fn:substring(invoice.status, 0, 1)}${fn:toLowerCase(fn:substring(invoice.status, 1, fn:length(invoice.status)))}" /></span>
                </div>
            </div>
        </div>
        <div class="bill-to">
            <div class="meta-label">Bill To</div>
            <div><strong><c:out value="${invoice.customerName}" /></strong></div>
            <c:if test="${not empty invoice.customerAddress}"><div><c:out value="${invoice.customerAddress}" /></div></c:if>
            <c:if test="${not empty invoice.customerEmail}"><div><c:out value="${invoice.customerEmail}" /></div></c:if>
            <c:if test="${not empty invoice.customerPhone}"><div><c:out value="${invoice.customerPhone}" /></div></c:if>
        </div>
    </div>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Item</th>
                    <th>Description</th>
                    <th class="num">Qty</th>
                    <th class="num">Unit Price</th>
                    <th class="num">Line Total</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${invoice.items}" varStatus="st">
                    <tr>
                        <td><c:out value="${st.count}" /></td>
                        <td><c:out value="${item.productName}" /></td>
                        <td><c:out value="${item.description}" /></td>
                        <td class="num"><c:out value="${item.quantity}" /></td>
                        <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2" /></td>
                        <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${item.lineTotal}" minFractionDigits="2" maxFractionDigits="2" /></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <table class="totals">
        <tr>
            <td>Subtotal</td>
            <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${invoice.subtotal}" minFractionDigits="2" maxFractionDigits="2" /></td>
        </tr>
        <tr>
            <td>Tax (<fmt:formatNumber value="${invoice.taxRate}" minFractionDigits="2" maxFractionDigits="2" />%)</td>
            <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${invoice.taxAmount}" minFractionDigits="2" maxFractionDigits="2" /></td>
        </tr>
        <tr class="grand">
            <td>Grand Total</td>
            <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${invoice.grandTotal}" minFractionDigits="2" maxFractionDigits="2" /></td>
        </tr>
    </table>
</div>

<div class="card narrow no-print">
    <h2>Update payment status</h2>
    <form method="post" action="${ctx}/invoices" class="status-form">
        <input type="hidden" name="action" value="updateStatus">
        <input type="hidden" name="id" value="${invoice.id}">
        <select name="status">
            <option value="DRAFT" ${invoice.status eq 'DRAFT' ? 'selected' : ''}>Draft</option>
            <option value="UNPAID" ${invoice.status eq 'UNPAID' ? 'selected' : ''}>Unpaid</option>
            <option value="PAID" ${invoice.status eq 'PAID' ? 'selected' : ''}>Paid</option>
        </select>
        <button type="submit" class="btn btn-primary">Update Status</button>
    </form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>