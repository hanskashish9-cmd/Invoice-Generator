<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Customers" />
<c:set var="activeMenu" value="customers" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Customers</h1>
    <a class="btn btn-primary" href="${ctx}/customers?action=new">+ Add Customer</a>
</div>

<div class="card">
    <c:choose>
        <c:when test="${empty customers}">
            <p class="muted">No customers yet. Click "Add Customer" to create one.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Address</th>
                            <th class="actions-col">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cust" items="${customers}" varStatus="st">
                            <tr>
                                <td><c:out value="${st.count}" /></td>
                                <td><c:out value="${cust.name}" /></td>
                                <td><c:out value="${cust.email}" /></td>
                                <td><c:out value="${cust.phone}" /></td>
                                <td><c:out value="${cust.address}" /></td>
                                <td class="actions-col">
                                    <a class="btn btn-light btn-sm" href="${ctx}/customers?action=edit&amp;id=${cust.id}">Edit</a>
                                    <form method="post" action="${ctx}/customers" class="inline-form"
                                          onsubmit="return confirm('Delete this customer? Old invoices keep their saved customer details.');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${cust.id}">
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