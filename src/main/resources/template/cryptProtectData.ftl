<html>
	<head></head>
	<body><#if request?? && request.servletPath?? && request.servletPath?is_string>${request.servletPath[1..]}</#if></body>
</html>