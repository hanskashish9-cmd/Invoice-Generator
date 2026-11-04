<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<c:set var="pageTitle" value="Error" />
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="card narrow center-text">
    <h2>Something went wrong</h2>
    <c:choose>
        <c:when test="${not empty errorMessage}">
            <p><c:out value="${errorMessage}" /></p>
        </c:when>
        <c:otherwise>
            <p>The page you asked for could not be found, or an unexpected error happened.</p>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${not empty sessionScope.loggedUser}">
            <a class="btn btn-primary" href="${ctx}/dashboard">Back to Dashboard</a>
        </c:when>
        <c:otherwise>
            <a class="btn btn-primary" href="${ctx}/login">Go to Login</a>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>