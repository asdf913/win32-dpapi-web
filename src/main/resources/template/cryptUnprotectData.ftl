<html>
	<head>
		<script>
		if(typeof document==="object"&&document!==null&&typeof document.addEventListener==="function"){
			document.addEventListener("DOMContentLoaded",()=>{
				[...document.querySelectorAll("td")].find(el => window.getComputedStyle(el).visibility=="hidden").style.height=window.getComputedStyle(document.querySelector("input").parentElement.parentElement).height;
			});
			<#if throwable??>
    		function closeAlert(){
    			var topAlert=document.getElementById("topAlert");
    			if(topAlert!==null){topAlert.hidden=true;}
    		}
    		var topAlert=document.getElementById("topAlert");
    		if(topAlert!==null&&topAlert.style!=null){
    			var margin=window.getComputedStyle(document.getElementsByTagName("body")[0]).margin;
    			topAlert.style.marginTop=topAlert.style.marginLeft=topAlert.style.marginRight="-"+margin;
    		}
			</#if>
		}
		</script>
		<#if throwable??>
			<style>
			.top-alert{
				display:flex;
				justify-content:space-between;
				align-items:center;
				background-color:#fff3cd;
				color:#664d03;
				border-bottom:1px solid #ffe69c;
				padding:12px 20px;
			}

			.top-alert[hidden]{display:none;}

			.close-btn{
				background:none;
				border:none;
				font-size:1.5rem;
				line-height:1;
				color:inherit;
				cursor:pointer;
				padding:0 4px;
			}

			.close-btn:hover{opacity:0.7;}

			</style>
		</#if>
	</head>
	<body>
		<#if throwable??>
			<div id="topAlert" class="top-alert" role="alert" style="margin-top:-8px;margin-left:-8px;margin-right:-8px;">
				<span>${throwable}</span>
				<button type="button" class="close-btn" aria-label="Close alert" onclick="closeAlert()">&times;</button>
			</div>
		</#if>
		<fieldset style="width:1px">
			<legend><#if request?? && request.servletPath?? && request.servletPath?is_string>${request.servletPath[1..]}</#if></legend>
			<form method="post" action=""<#if request??>${request.scheme!""}</#if>://<#if request?? && request.serverName??>${request.serverName}</#if>:<#if request??>${request.serverPort?c}</#if><#if request?? && request.servletPath??>${request.servletPath}</#if>"">
				<table>
					<tbody>
						<tr><td>Input</td><td><input name="input" value="<#if request??>${request.getParameter("input")!""}</#if>" style="field-sizing:content;resize:none;width:90vw;box-sizing:border-box"/></td></tr>
						<tr><td>Encoding</td><td><select name="encoding"><#if encodings??><#list encodings as x><option value="${x}" <#if encoding?? && encoding==x>selected="selected"<#elseif request?? && request.getParameter("encoding")?? && request.getParameter("encoding")==x>selected="selected"</#if>>${x}</option></#list></#if></select></td></tr>
						<tr><td>Flags</td><td><input name="flags" value="<#if flags??>${flags}<#elseif request??>${request.getParameter("flags")!""}</#if>" style="field-sizing:content;resize:none;width:90vw;box-sizing:border-box"/></td></tr>
						<tr><td>Entropy</td><td><input name="entropy" value="<#if request??>${request.getParameter("entropy")!""}</#if>" style="field-sizing:content;resize:none;width:90vw;box-sizing:border-box"/></td></tr>
						<tr style="visibility:hidden"><td>Description</td><td></td></tr>
						<tr><td>&nbsp;</td><td><input type="submit"/></td></tr>
						<#if output??>
							<tr style="vertical-align:top"><td>Output</td><td><textarea style="field-sizing:content;resize:none;width:90vw;box-sizing:border-box">${output}</textarea></td><td><input type="button" value="Copy" onclick="navigator.clipboard.writeText(document.getElementsByTagName('textarea')[0].value);"/></td><tr/>
						</#if>
						<tr><td><a href="/">Back</a></td></tr>
					</tbody>
				</table>
			</form>
		</fieldset>
	</body>
</html>