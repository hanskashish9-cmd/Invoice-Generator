<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Dashboard" />
<c:set var="activeMenu" value="dashboard" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="page-head">
    <h1>Dashboard</h1>
    <a class="btn btn-primary" href="${ctx}/invoices?action=new">+ Create Invoice</a>
</div>

<div class="stats">
    <div class="stat">
        <div class="stat-number"><c:out value="${customerCount}" /></div>
        <div class="stat-label">Customers</div>
    </div>
    <div class="stat">
        <div class="stat-number"><c:out value="${productCount}" /></div>
        <div class="stat-label">Products / Services</div>
    </div>
    <div class="stat">
        <div class="stat-number"><c:out value="${invoiceCount}" /></div>
        <div class="stat-label">Invoices</div>
    </div>
    <div class="stat">
        <div class="stat-number"><c:out value="${draftCount}" /></div>
        <div class="stat-label">Draft</div>
    </div>
    <div class="stat">
        <div class="stat-number"><c:out value="${unpaidCount}" /></div>
        <div class="stat-label">Unpaid</div>
    </div>
    <div class="stat">
        <div class="stat-number"><c:out value="${paidCount}" /></div>
        <div class="stat-label">Paid</div>
    </div>
</div>

<div class="stats stats-2">
    <div class="stat stat-warn">
        <div class="stat-number">
            <c:out value="${currencySymbol}" /><fmt:formatNumber value="${unpaidAmount}" minFractionDigits="2" maxFractionDigits="2" />
        </div>
        <div class="stat-label">Amount waiting to be paid (Unpaid invoices)</div>
    </div>
    <div class="stat stat-good">
        <div class="stat-number">
            <c:out value="${currencySymbol}" /><fmt:formatNumber value="${paidAmount}" minFractionDigits="2" maxFractionDigits="2" />
        </div>
        <div class="stat-label">Amount received (Paid invoices)</div>
    </div>
</div>

<div class="card">
    <div class="page-head">
        <h2>Recent invoices</h2>
        <a href="${ctx}/invoices">View all</a>
    </div>

    <c:choose>
        <c:when test="${empty recentInvoices}">
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
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inv" items="${recentInvoices}">
                            <tr>
                                <td><a href="${ctx}/invoices?action=view&amp;id=${inv.id}"><c:out value="${inv.invoiceNumber}" /></a></td>
                                <td><c:out value="${inv.invoiceDate}" /></td>
                                <td><c:out value="${inv.customerName}" /></td>
                                <td class="num"><c:out value="${currencySymbol}" /><fmt:formatNumber value="${inv.grandTotal}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td><span class="badge badge-${fn:toLowerCase(inv.status)}"><c:out value="${fn:substring(inv.status, 0, 1)}${fn:toLowerCase(fn:substring(inv.status, 1, fn:length(inv.status)))}" /></span></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>