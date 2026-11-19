<html>
	<head></head>
	<body>
		<#if links?? && links?is_enumerable>
			<#list links as link>
				<a href="<#if request??>${request.scheme!""}</#if>://<#if request?? && request.serverName??>${request.serverName}</#if>:<#if request??>${request.serverPort?c}</#if>/${link}">${link}</a><br/>
			</#list>
		</#if>
	</body>
</html>