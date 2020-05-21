<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.UnitDesignator" %>
<%@ page language="java" import="ca.bc.gov.ols.admin.AdminApplication" %>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore" %>
<%@ page language="java" import="java.util.stream.Stream" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Iterator" %>

<%@ include file="header.jsp"%> 

<%
 	AdminApplication adminApp = AdminApplication.adminApplication();
 GeocoderConfigurationStore configStore = adminApp.getConfigStore();

 String action = request.getParameter("action");
 if(action != null && !action.isEmpty()) {
 	String can = request.getParameter("can");
 	String query = null;
 	if(can != null && !can.isEmpty() && action != null && !action.isEmpty()) {
 		UnitDesignator ud = new UnitDesignator(can);
 		if(action.equals("create")) {
 	configStore.addUnitDesignator(ud);
 		} else if(action.equals("delete")) {
 	configStore.removeUnitDesignator(ud);
 		} 
 		out.println("Changes saved successfully.");
 	} else { 
 		out.println("<font color='red'>Missing or invalid parameters, cannot save changes.</font>");
 	}
 }
 %>
<div class="bodyContent">

<h1>Add Unit Designator</h1>

<form name="ud_create" method="POST">
<input type="hidden" name="action" value="create">
<table>
<tr><td>Unit Designator:</td><td><input type="text" name="can" size="15"></td></tr>
<tr><td><input type="submit" name="submit" value="Add"></td></tr>
</table>
</form>
<hr>
<h1>Edit Existing Unit Designator Mappings</h1>

<form name="ud_delete"  method="POST">
<input type="hidden" name="action" value="delete">
<table class="alternating">
<% 
Stream<UnitDesignator> unitDesignators = configStore.getUnitDesignators();
unitDesignators = unitDesignators.sorted();
for(Iterator<UnitDesignator> it = unitDesignators.iterator(); it.hasNext();) {
	UnitDesignator ud = it.next();
	String can = ud.getCanonicalForm();
	out.println("<tr>");
	out.println("<td>" + can + "</td>");
	out.println("<td><button type=\"submit\" name=\"can\" value=\"" + can + "\">Delete</button></td>");
	out.println("</tr>");
}
%>
</table>
</form>
</div>

<%@ include file="footer.jsp" %>