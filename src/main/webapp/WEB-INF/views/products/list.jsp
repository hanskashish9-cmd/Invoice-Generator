<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Products" />
<c:set var="activeMenu" value="products" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Products / Services</h1>
    <a class="btn btn-primary" href="${ctx}/products?action=new">+ Add Product</a>
</div>

<div class="card">
    <c:choose>
        <c:when test="${empty products}">
            <p class="muted">No products yet. Click "Add Product" to create one.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>Description</th>
                            <th class="num">Unit Price</th>
                            <th class="actions-col">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prod" items="${products}" varStatus="st">
                            <tr>
                                <td><c:out value="${st.count}" /></td>
                                <td><c:out value="${prod.name}" /></td>
                                <td><c:out value="${prod.description}" /></td>
                                <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${prod.unitPrice}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td class="actions-col">
                                    <a class="btn btn-light btn-sm" href="${ctx}/products?action=edit&amp;id=${prod.id}">Edit</a>
                                    <form method="post" action="${ctx}/products" class="inline-form"
                                          onsubmit="return confirm('Delete this product? Old invoices keep their saved item details.');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${prod.id}">
                                        <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>