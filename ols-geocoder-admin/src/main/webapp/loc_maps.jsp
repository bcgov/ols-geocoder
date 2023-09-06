<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.LocalityMapping" %>
<%@ page language="java" import="ca.bc.gov.ols.admin.AdminApplication" %>
<%@ page language="java" import="ca.bc.gov.ols.admin.LocalityMappingComparator" %>
<%@ page language="java" import="ca.bc.gov.ols.geocoder.config.GeocoderConfigurationStore" %>
<%@ page language="java" import="java.util.stream.Stream" %>
<%@ page language="java" import="java.util.List" %>
<%@ page language="java" import="java.util.Collections" %>
<%@ page language="java" import="java.util.Iterator" %>
<%@ page language="java" import="java.util.Comparator" %>
<%@ page language="java" import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page language="java" import="gnu.trove.map.hash.TIntObjectHashMap" %>
<%@ page language="java" import="gnu.trove.iterator.TIntObjectIterator" %>

<%@ include file="header.jsp"%> 

<%
AdminApplication adminApp = AdminApplication.adminApplication();
GeocoderConfigurationStore configStore = adminApp.getConfigStore();
TIntObjectHashMap<String> localityIdMap = adminApp.getLocalityIdMap();
%>
<div class="bodyContent">

<h1>Add Locality Mapping</h1>

<form name="lm_create" action="loc_maps_edit.jsp" method="POST">
<input type="hidden" name="action" value="create">
<table>
<tr>
<td>Input String: <br><input type="text" name="input"></td>
<td>Maps to: <br><b><select name="id">
<%
for(int id : adminApp.getSortedLocalityIds()) {
	String name = localityIdMap.get(id);
	out.println("<option value=\"" + id + "\">" + name + " (" + id + ")</option>");
}
%>
</select></b></td>
<td colspan=2>with confidence:<br><input type="text" name="conf" size="3"></td>
</tr>
<tr><td colspan="4"><input type="submit" name="submit" value="Save"><br><br><hr></td></tr>
</table>
</form>
<h1>Edit Existing Locality Mappings</h1>
<form name="lm_edit" action="loc_maps_edit.jsp" method="POST">
<input type="hidden" name="action" value="edit">
<table class="alternating">
<tr>
	<td><a href="loc_maps.jsp?orderby=input_string">Sort by Input String</a></td>
	<td><a href="loc_maps.jsp?orderby=locality">Sort by Locality</a></td>
	<td><a href="loc_maps.jsp?orderby=confidence">Sort by Confidence</a></td>
</tr>
<% 
	
Stream<LocalityMapping> locMaps = configStore.getLocalityMappings();
	
String orderCol = request.getParameter("orderby");

locMaps = locMaps.sorted(new LocalityMappingComparator(orderCol, localityIdMap));

for(Iterator<LocalityMapping> it = locMaps.iterator(); it.hasNext();) { 
	LocalityMapping locMap = it.next();
	int id = locMap.getLocalityId();
	String input = locMap.getInputString();
	int conf = locMap.getConfidence();
	
	out.println("<tr><td><b>" + input + "</b></td><td>Maps to: <b>" + localityIdMap.get(id) + " (" + id + ")"
			+ "</b> with confidence: </td><td><b>" + conf + "</b></td>"
			+ "<td><button name=\"row\" value=\"" + id + ":" + StringEscapeUtils.escapeEcmaScript(input) + ":" + conf
			+ "\">Edit</button></td></tr>");
}
		
%>
</table>
</form>
</div>

<%@ include file="footer.jsp" %>