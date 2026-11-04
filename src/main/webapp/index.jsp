<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Opening the application root sends the user to the dashboard.
     If the user is not logged in, the filter then redirects to /login. --%>
<c:redirect url="/dashboard" />