<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.AbbreviationMapping" %>
<%@ page language="java" import="ca.bc.gov.ols.admin.AdminApplication" %>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.stream.Stream" %>
<%@ page language="java" import="java.util.Iterator" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Comparator" %>

<%@ include file="header.jsp"%> 

<%
 	AdminApplication adminApp = AdminApplication.adminApplication();
 GeocoderConfigurationStore configStore = adminApp.getConfigStore();

 String action = request.getParameter("action");
 if(action != null && !action.isEmpty()) {
 	String abbrForm = request.getParameter("abbrForm");
 	String longForm = request.getParameter("longForm");
 	String row = request.getParameter("row");
 	if(row != null && !row.isEmpty()) {
 		String[] parts = row.split(":");
 		if(parts.length == 2) {
 	abbrForm = parts[0];
 	longForm = parts[1];
 		}
 	}
 	String query = null;
 	if(action != null && !action.isEmpty()
 	&& abbrForm != null && !abbrForm.isEmpty()
 	&& longForm != null && !longForm.isEmpty()) {
 		AbbreviationMapping abbrMap = new AbbreviationMapping(abbrForm,longForm);
 		if(action.equals("create")) {
 	configStore.setAbbrevMapping(abbrMap);
 		} else if(action.equals("delete")) {
 	configStore.removeAbbrevMapping(abbrMap);
 		}
 		out.println("Changes saved successfully.");
 	} else {
 		out.println("<font color='red'>Missing or invalid parameters, cannot save changes.</font>");
 	}
 }
 %>
<div class="bodyContent">

<h1>Add Abbreviation Mapping</h1>

<form name="am_create" method="POST">
<input type="hidden" name="action" value="create">
Abbreviated Form: <input type="text" name="abbrForm" size="10">
Maps to Long Form: <input type="text" name="longForm" size="10">
<br><br><input type="submit" name="submit" value="Add"><br>
</form>
<hr>
<h1>Edit Existing Abbreviation Mappings</h1>
<form name="am_delete" method="POST">
<input type="hidden" name="action" value="delete">
<table class="alternating">
<% 
Stream<AbbreviationMapping> abbrMaps = configStore.getAbbrevMappings();
abbrMaps = abbrMaps.sorted();
for(Iterator<AbbreviationMapping> it = abbrMaps.iterator(); it.hasNext();) {
	AbbreviationMapping abbrMap = it.next();
	String abbrForm = abbrMap.getAbbreviatedForm();
	String longForm = abbrMap.getLongForm();
			
	out.println("<tr>");
	out.println("<td><b>" + abbrForm + "</b></td>");
	out.println("<td>Maps to: <b>");
	out.println(longForm);
	out.println("<td><button type=\"submit\" name=\"row\" value=\"" + abbrForm + ":" + longForm + "\">Delete</a></td>");
	out.println("</tr>");
}
		
%>
</table>
</form>
</div>
<%@ include file="footer.jsp" %>