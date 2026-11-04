<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="isNew" value="${product.id == 0}" />
<c:set var="pageTitle" value="${isNew ? 'Add Product / Service' : 'Edit Product / Service'}" />
<c:set var="activeMenu" value="products" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1><c:out value="${pageTitle}" /></h1>
</div>

<div class="card narrow">
    <form method="post" action="${ctx}/products">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="id" value="${product.id}">

        <div class="form-group ${not empty errors.name ? 'has-error' : ''}">
            <label for="name">Name *</label>
            <input type="text" id="name" name="name" maxlength="120" value="${fn:escapeXml(product.name)}">
            <c:if test="${not empty errors.name}"><div class="field-error"><c:out value="${errors.name}" /></div></c:if>
        </div>

        <div class="form-group ${not empty errors.description ? 'has-error' : ''}">
            <label for="description">Description</label>
            <textarea id="description" name="description" rows="3" maxlength="500"><c:out value="${product.description}" /></textarea>
            <c:if test="${not empty errors.description}"><div class="field-error"><c:out value="${errors.description}" /></div></c:if>
        </div>

        <div class="form-group ${not empty errors.unitPrice ? 'has-error' : ''}">
            <label for="unitPrice">Unit Price (<c:out value="${fn:trim(currencySymbol)}" />) *</label>
            <input type="text" id="unitPrice" name="unitPrice" inputmode="decimal"
                   placeholder="e.g. 499.99" value="${fn:escapeXml(priceInput)}">
            <c:if test="${not empty errors.unitPrice}"><div class="field-error"><c:out value="${errors.unitPrice}" /></div></c:if>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Save Product</button>
            <a class="btn btn-light" href="${ctx}/products">Cancel</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>