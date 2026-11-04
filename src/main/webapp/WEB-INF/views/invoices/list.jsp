<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Invoices" />
<c:set var="activeMenu" value="invoices" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Invoices</h1>
    <a class="btn btn-primary" href="${ctx}/invoices?action=new">+ Create Invoice</a>
</div>

<div class="card">
    <c:choose>
        <c:when test="${empty invoices}">
            <p class="muted">No invoices yet. Click "Create Invoice" to add the first one.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Invoice No.</th>
                            <th>Date</th>
                            <th>Customer</th>
                            <th class="num">Grand Total</th>
                            <th>Status</th>
                            <th>Change Status</th>
                            <th class="actions-col">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inv" items="${invoices}">
                            <tr>
                                <td><a href="${ctx}/invoices?action=view&amp;id=${inv.id}"><c:out value="${inv.invoiceNumber}" /></a></td>
                                <td><c:out value="${inv.invoiceDate}" /></td>
                                <td><c:out value="${inv.customerName}" /></td>
                                <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${inv.grandTotal}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td><span class="badge badge-${fn:toLowerCase(inv.status)}"><c:out value="${fn:substring(inv.status, 0, 1)}${fn:toLowerCase(fn:substring(inv.status, 1, fn:length(inv.status)))}" /></span></td>
                                <td>
                                    <form method="post" action="${ctx}/invoices" class="inline-form">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="id" value="${inv.id}">
                                        <input type="hidden" name="returnTo" value="list">
                                        <select name="status" class="select-sm">
                                            <option value="DRAFT" ${inv.status eq 'DRAFT' ? 'selected' : ''}>Draft</option>
                                            <option value="UNPAID" ${inv.status eq 'UNPAID' ? 'selected' : ''}>Unpaid</option>
                                            <option value="PAID" ${inv.status eq 'PAID' ? 'selected' : ''}>Paid</option>
                                        </select>
                                        <button type="submit" class="btn btn-light btn-sm">Update</button>
                                    </form>
                                </td>
                                <td class="actions-col">
                                    <a class="btn btn-light btn-sm" href="${ctx}/invoices?action=view&amp;id=${inv.id}">View</a>
                                    <a class="btn btn-danger btn-sm" href="${ctx}/invoices?action=confirmDelete&amp;id=${inv.id}">Delete</a>
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