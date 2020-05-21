<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.admin.AdminApplication"%>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore" %>
<%@ page language="java" import="ca.bc.gov.ols.config.ConfigurationParameter" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.stream.Collectors" %>

<%@ include file="header.jsp"%> 

<%
 	AdminApplication adminApp = AdminApplication.adminApplication();
 GeocoderConfigurationStore configStore = adminApp.getConfigStore();
 List<ConfigurationParameter> params = configStore.getConfigParams().collect(Collectors.toList());

 if (request.getParameter("submit") != null && !request.getParameter("submit").isEmpty()){

 	for (ConfigurationParameter param : params) {
 		String paramName = param.getConfigParamName();
 		if(paramName.startsWith("fault.")
 		|| paramName.startsWith("precision.")) {
 	param.setConfigParamValue(request.getParameter(paramName));
 	configStore.setConfigParam(param);
 		}
 	}
 	
 	out.println("<br><br>Parameters Saved Successfully, <a href='faults.jsp'>click here to make more changes</a>.");
 } else {
 %>
<div class="bodyContent">

<h1>Geocoder Precision Points</h1>
<p> Original points awarded for match precision, before faults are subtracted.</p>
<form name="fault_update" method="POST">
<table class="alternating">

<% 
	Collections.sort(params);
	for(ConfigurationParameter param : params) { 
		String name = param.getConfigParamName();
		String value = param.getConfigParamValue();
		if(name.startsWith("precision.")) {
			out.println("<tr>");
			out.println("<td>" + name + ":</td>");
			out.println("<td><input name='" + name + "' type=text size=3 value=" + value + "></td>");
			out.println("</tr>");
		}
	}
%>

</table>

<h1>Geocoder Match Fault Values</h1>
<p>points to be subtracted for each fault found in the address string.</p>
<table class="alternating">

<% 
	for(ConfigurationParameter param : params) { 
		String name = param.getConfigParamName();
		String value = param.getConfigParamValue();
		if(name.startsWith("fault.")) {
			out.println("<tr>");
			out.println("<td>" + name + ":</td>");
			out.println("<td><input name='" + name + "' type=text size=3 value=" + value + "></td>");
			out.println("</tr>");
		}
	}
%>

</table>
<br><br>
<p>
<input name="submit" type="submit" value="Save All Values">
</p>
<br>
<br>
</form>
</div>

<%
}
%>
<%@ include file="footer.jsp" %>
