<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/common/config.jspf" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login | Invoice Generator</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body class="login-body">

<div class="login-card">
    <h1>Invoice Generator</h1>
    <p class="muted">Please log in to continue</p>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}" /></div>
    </c:if>
    <c:if test="${not empty message}">
        <div class="alert alert-success"><c:out value="${message}" /></div>
    </c:if>

    <form method="post" action="${ctx}/login">
        <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" maxlength="50"
                   value="${fn:escapeXml(username)}" autofocus>
        </div>
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" maxlength="100">
        </div>
        <button type="submit" class="btn btn-primary btn-block">Login</button>
    </form>

    <p class="demo-note">Demo login: <strong>admin</strong> / <strong>admin123</strong></p>
</div>

</body>
</html>