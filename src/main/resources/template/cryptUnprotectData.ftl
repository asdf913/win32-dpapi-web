<html>
	<head></head>
	<body>
		<fieldset style="width:1px">
			<legend><#if request?? && request.servletPath?? && request.servletPath?is_string>${request.servletPath[1..]}</#if></legend>
			<form method="post" action=""<#if request??>${request.scheme!""}</#if>://<#if request?? && request.serverName??>${request.serverName}</#if>:<#if request??>${request.serverPort?c}</#if><#if request?? && request.servletPath??>${request.servletPath}</#if>"">
				<table>
					<tbody>
						<tr><td>Input</td><td><input name="input" value="<#if request??>${request.getParameter("input")!""}</#if>" style="field-sizing:content;resize:none;width:92vw;box-sizing:border-box"/></td></tr>
						<tr><td>&nbsp;</td><td><input type="submit"/></td></tr>
						<#if output??>
							<tr style="vertical-align:top"><td>Output</td><td><textarea style="field-sizing:content;resize:none;width:92vw;box-sizing:border-box">${output}</textarea></td><td><input type="button" value="Copy" onclick="navigator.clipboard.writeText(document.getElementsByTagName('textarea')[0].value);"/></td><tr/>
						</#if>
						<tr><td><a href="/">Back</a></td></tr>
					</tbody>
				</table>
			</form>
		</fieldset>
	</body>
</html>