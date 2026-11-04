<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Create Invoice" />
<c:set var="activeMenu" value="invoices" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Create Invoice</h1>
</div>

<c:if test="${empty customers}">
    <div class="alert alert-error">
        There are no customers yet. <a href="${ctx}/customers?action=new">Add a customer</a> first.
    </div>
</c:if>
<c:if test="${empty products}">
    <div class="alert alert-error">
        There are no products or services yet. <a href="${ctx}/products?action=new">Add a product</a> first.
    </div>
</c:if>

<c:if test="${not empty itemErrors}">
    <div class="alert alert-error">
        <strong>Please fix the item rows:</strong>
        <ul class="error-list">
            <c:forEach var="msg" items="${itemErrors}">
                <li><c:out value="${msg}" /></li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form method="post" action="${ctx}/invoices">
    <input type="hidden" name="action" value="save">

    <div class="card">
        <h2>Invoice details</h2>
        <div class="grid-2">
            <div class="form-group ${not empty errors.customerId ? 'has-error' : ''}">
                <label for="customerId">Customer *</label>
                <select id="customerId" name="customerId">
                    <option value="">-- Select customer --</option>
                    <c:forEach var="cust" items="${customers}">
                        <option value="${cust.id}" ${cust.id.toString() eq customerIdInput ? 'selected' : ''}><c:out value="${cust.name}" /></option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.customerId}"><div class="field-error"><c:out value="${errors.customerId}" /></div></c:if>
            </div>

            <div class="form-group ${not empty errors.invoiceDate ? 'has-error' : ''}">
                <label for="invoiceDate">Invoice Date *</label>
                <input type="date" id="invoiceDate" name="invoiceDate" value="${fn:escapeXml(invoiceDateInput)}">
                <c:if test="${not empty errors.invoiceDate}"><div class="field-error"><c:out value="${errors.invoiceDate}" /></div></c:if>
            </div>

            <div class="form-group ${not empty errors.taxRate ? 'has-error' : ''}">
                <label for="taxRate">Tax Rate (%) *</label>
                <input type="text" id="taxRate" name="taxRate" inputmode="decimal" value="${fn:escapeXml(taxRateInput)}">
                <c:if test="${not empty errors.taxRate}"><div class="field-error"><c:out value="${errors.taxRate}" /></div></c:if>
            </div>

            <div class="form-group ${not empty errors.status ? 'has-error' : ''}">
                <label for="status">Status *</label>
                <select id="status" name="status">
                    <option value="DRAFT" ${statusInput eq 'DRAFT' ? 'selected' : ''}>Draft</option>
                    <option value="UNPAID" ${statusInput eq 'UNPAID' ? 'selected' : ''}>Unpaid</option>
                    <option value="PAID" ${statusInput eq 'PAID' ? 'selected' : ''}>Paid</option>
                </select>
                <c:if test="${not empty errors.status}"><div class="field-error"><c:out value="${errors.status}" /></div></c:if>
            </div>
        </div>
    </div>

    <div class="card">
        <h2>Items</h2>
        <p class="muted">
            Choose a product or service and enter a quantity (whole number, 1 or more).
            Leave <em>Unit Price</em> empty to use the product's own price, or type a different price
            for this invoice. Empty rows are ignored. Line totals, subtotal, tax and grand total are
            calculated automatically when you save.
        </p>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Product / Service</th>
                        <th>Quantity</th>
                        <th>Unit Price (<c:out value="${fn:trim(currencySymbol)}" />) - optional</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="row" items="${rows}" varStatus="st">
                        <tr>
                            <td><c:out value="${st.count}" /></td>
                            <td>
                                <select name="productId">
                                    <option value="">-- Select --</option>
                                    <c:forEach var="prod" items="${products}">
                                        <option value="${prod.id}" ${prod.id.toString() eq row.productId ? 'selected' : ''}>
                                            <c:out value="${prod.name}" />
                                            (<c:out value="${currencySymbol}" /><fmt:formatNumber value="${prod.unitPrice}" minFractionDigits="2" maxFractionDigits="2" />)
                                        </option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td><input type="text" name="quantity" inputmode="numeric" class="input-sm"
                                       value="${fn:escapeXml(row.quantity)}"></td>
                            <td><input type="text" name="unitPrice" inputmode="decimal" class="input-sm"
                                       value="${fn:escapeXml(row.unitPrice)}"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Save Invoice</button>
        <a class="btn btn-light" href="${ctx}/invoices">Cancel</a>
    </div>
</form>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>