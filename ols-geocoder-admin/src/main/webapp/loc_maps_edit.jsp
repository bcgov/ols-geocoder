<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.admin.AdminApplication" %>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.LocalityMapping"%>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Comparator" %>
<%@ page language="java" import="org.apache.commons.lang3.StringEscapeUtils" %>
<%@ page language="java" import="gnu.trove.map.hash.TIntObjectHashMap" %>

<%@ include file="header.jsp"%> 

<%
 	AdminApplication adminApp = AdminApplication.adminApplication();
 GeocoderConfigurationStore configStore = adminApp.getConfigStore();
 TIntObjectHashMap<String> localityIdMap = adminApp.getLocalityIdMap();

 String action = request.getParameter("action");
 int id = Integer.MIN_VALUE;
 int conf = Integer.MIN_VALUE;
 try {
 	id = Integer.parseInt(request.getParameter("id"));
 	conf = Integer.parseInt(request.getParameter("conf"));
 } catch(NumberFormatException nfe) {
 	// no worries, already set to Integer.MIN_VALUE
 }
 String input = request.getParameter("input");
 String row = request.getParameter("row");
 if(row != null && !row.isEmpty()) {
 	String[] parts = row.split(":");
 	if(parts.length == 3) {
 		try {
 	id = Integer.parseInt(parts[0]);
 		} catch(NumberFormatException nfe) {
 	id = Integer.MIN_VALUE;
 		}
 		input = parts[1];
 		try {
 	conf = Integer.parseInt(parts[2]);
 		} catch(NumberFormatException nfe) {
 	conf = Integer.MIN_VALUE;
 		}
 	}
 }
 if(action != null && !action.isEmpty() && !action.equals("edit")) {
 	String query = null;
 	if(action != null && !action.isEmpty()
 			&& input != null && !input.isEmpty()
 			&& id != Integer.MIN_VALUE && conf != Integer.MIN_VALUE) {
 		LocalityMapping locMap = new LocalityMapping(id, input, conf);
 		if(action.equals("create") || action.equals("update")) {
 			configStore.setLocalityMapping(locMap);
 		} else if(action.equals("delete")) {
 			configStore.removeLocalityMapping(locMap);
 		} 
 		out.println("Changes saved successfully.");
 	} else {
 		out.println("<font color='red'>Missing or invalid parameters, cannot save changes.</font>");
 	}
 } else if(action != null && action.equals("edit") 
 		&& input != null && !input.isEmpty()
 		&& id != Integer.MIN_VALUE
 		&& conf != Integer.MIN_VALUE) {
 %>
<div class="bodyContent">

<h1>Edit Locality Mapping</h1>

<form name="lm_update" method="POST">
<% 
	out.println("<input type=\"hidden\" name=\"id\" value=\"" + id + "\">");
	out.println("<input type=\"hidden\" name=\"input\" value=\"" + input + "\">");
	out.println("<table>");
	out.println("<tr>");
	out.println("<td>" + input + "</td>");
	out.println("<td>Maps to: <b>" + localityIdMap.get(id) + " (" + id + ")</b>");
	out.println("with confidence: </td>");
	out.println("<td><input type=\"text\" name=\"conf\" size=\"5\" value='" + conf+ "'></td>");
	out.println("</tr>");
	out.println("<tr>");
	out.println("<tr><td><button type=\"submit\" name=\"action\" value=\"update\">Save Changes</button> or </td>");
	out.println("</tr><tr><td><br><br><button type=\"submit\" name=\"action\" style=\"font: bold 10px Arial\" value=\"delete\">Delete This Mapping</button></td>");
	out.println("</tr>");
%>
</table>
</form>
</div>

<%
} else {
	out.println("<br><br><font color='red'>Missing Parameters to Edit Locality Mapping, please try again.</font><br>");
}
%>

<%@ include file="footer.jsp" %>