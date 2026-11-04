<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Confirm Delete" />
<c:set var="activeMenu" value="invoices" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Delete Invoice?</h1>
</div>

<div class="card narrow">
    <div class="alert alert-error">
        You are about to permanently delete this invoice and all its items. This cannot be undone.
    </div>

    <table class="summary">
        <tr><th>Invoice No.</th><td><c:out value="${invoice.invoiceNumber}" /></td></tr>
        <tr><th>Date</th><td><c:out value="${invoice.invoiceDate}" /></td></tr>
        <tr><th>Customer</th><td><c:out value="${invoice.customerName}" /></td></tr>
        <tr><th>Items</th><td><c:out value="${fn:length(invoice.items)}" /></td></tr>
        <tr><th>Grand Total</th>
            <td><c:out value="${currencySymbol}" /><fmt:formatNumber value="${invoice.grandTotal}" minFractionDigits="2" maxFractionDigits="2" /></td></tr>
        <tr><th>Status</th><td><c:out value="${invoice.status}" /></td></tr>
    </table>

    <form method="post" action="${ctx}/invoices" class="form-actions">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${invoice.id}">
        <input type="hidden" name="confirm" value="yes">
        <button type="submit" class="btn btn-danger">Yes, delete this invoice</button>
        <a class="btn btn-light" href="${ctx}/invoices?action=view&amp;id=${invoice.id}">No, go back</a>
    </form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>