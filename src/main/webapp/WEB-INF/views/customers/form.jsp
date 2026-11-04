<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="isNew" value="${customer.id == 0}" />
<c:set var="pageTitle" value="${isNew ? 'Add Customer' : 'Edit Customer'}" />
<c:set var="activeMenu" value="customers" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1><c:out value="${pageTitle}" /></h1>
</div>

<div class="card narrow">
    <form method="post" action="${ctx}/customers">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="id" value="${customer.id}">

        <div class="form-group ${not empty errors.name ? 'has-error' : ''}">
            <label for="name">Name *</label>
            <input type="text" id="name" name="name" maxlength="100" value="${fn:escapeXml(customer.name)}">
            <c:if test="${not empty errors.name}"><div class="field-error"><c:out value="${errors.name}" /></div></c:if>
        </div>

        <div class="form-group ${not empty errors.email ? 'has-error' : ''}">
            <label for="email">Email</label>
            <input type="text" id="email" name="email" maxlength="100" value="${fn:escapeXml(customer.email)}">
            <c:if test="${not empty errors.email}"><div class="field-error"><c:out value="${errors.email}" /></div></c:if>
        </div>

        <div class="form-group ${not empty errors.phone ? 'has-error' : ''}">
            <label for="phone">Phone</label>
            <input type="text" id="phone" name="phone" maxlength="20" value="${fn:escapeXml(customer.phone)}">
            <c:if test="${not empty errors.phone}"><div class="field-error"><c:out value="${errors.phone}" /></div></c:if>
        </div>

        <div class="form-group ${not empty errors.address ? 'has-error' : ''}">
            <label for="address">Address</label>
            <textarea id="address" name="address" rows="3" maxlength="255"><c:out value="${customer.address}" /></textarea>
            <c:if test="${not empty errors.address}"><div class="field-error"><c:out value="${errors.address}" /></div></c:if>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Save Customer</button>
            <a class="btn btn-light" href="${ctx}/customers">Cancel</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>