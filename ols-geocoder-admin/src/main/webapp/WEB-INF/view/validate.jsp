<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="../../header.jsp" %>
<div class="bodyContent">
<h1>Validate Configuration</h1>
<p><b>File Name:</b> ${exportConfig.fileName}</p>
<p><b>Export Date (from file contents):</b> ${exportConfig.exportDate}</p> 
<c:choose>
  <c:when test="${!exportConfig.errors.isEmpty()}">
    <h2 class="red">Validation Failed</h2>
    <p><b>Errors</b></p>
    <ul>
    <c:forEach var="error" items="${exportConfig.errors}">
      <li>${error}</li>
    </c:forEach>  
    </ul>
  </c:when>
  <c:otherwise>
    <h2>Validation Successful</h2>
  </c:otherwise>
</c:choose>
<c:if test="${!exportConfig.messages.isEmpty()}">
	<p><b>Messages</b></p>
	<ul>
	<c:forEach var="msg" items="${exportConfig.messages}">
  		<li>${msg}</li>
	</c:forEach>
	</ul>
</c:if>
<h2>Record Counts</h2>
<table class="diffTable">
<tr><th>Table</th><th>Live Config Records</th><th>File Records</th><th>File Check Count</th></tr>
<tr><td>Configuration Parameters</td><td>${comparison.curConfigParamCount}</td><td>${comparison.otherConfigParamCount}</td><td>${exportConfig.configParamCount}</td></tr>
<tr><td>Abbreviation Mappings</td><td>${comparison.curAbbrevMappingCount}</td><td>${comparison.otherAbbrevMappingCount}</td><td>${exportConfig.abbrevMappingCount}</td></tr>
<tr><td>Unit Designators</td><td>${comparison.curUnitDesignatorCount}</td><td>${comparison.otherUnitDesignatorCount}</td><td>${exportConfig.unitDesignatorCount}</td></tr>
<tr><td>Locality Mappings</td><td>${comparison.curLocalityMappingCount}</td><td>${comparison.otherLocalityMappingCount}</td><td>${exportConfig.localityMappingCount}</td></tr>
</table>
<h2>Comparison with Live Config</h2>
<h3>Configuration Parameters Differences</h3>
<c:choose>
  <c:when test="${comparison.configParamDiffs == null || comparison.configParamDiffs.isEmpty()}">
    <p><b>No Differences</b></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th colspan="3">Live Config</th><th colspan="3">File</th></tr>
    <tr><th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th>
      <th>APP_ID</th><th>CONFIG_PARAM_NAME</th><th>CONFIG_PARAM_VALUE</th></tr>
    <c:forEach var="diff" items="${comparison.configParamDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.current == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.current.appId}</td><td>${diff.current.configParamName}</td><td>${diff.current.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.other == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.other.appId}</td><td>${diff.other.configParamName}</td><td>${diff.other.configParamValue}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

<h3>Abbreviation Mappings Differences</h3>
<c:choose>
  <c:when test="${comparison.abbrevMappingDiffs == null || comparison.abbrevMappingDiffs.isEmpty()}">
    <p><b>No Differences</b></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th colspan="2">Live Config</th><th colspan="2">File</th></tr>
    <tr><th>ABBREVIATED_FORM</th><th>LONG_FORM</th>
      <th>ABBREVIATED_FORM</th><th>LONG_FORM</th></tr>
    <c:forEach var="diff" items="${comparison.abbrevMappingDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.current == null}">
      	  <td colspan="2">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.current.abbreviatedForm}</td><td>${diff.current.longForm}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.other == null}">
      	  <td colspan="2">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.other.abbreviatedForm}</td><td>${diff.other.longForm}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

<h3>Unit Designators Differences</h3>
<c:choose>
  <c:when test="${comparison.unitDesignatorDiffs == null || comparison.unitDesignatorDiffs.isEmpty()}">
    <p><b>No Differences</b></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th>Live Config</th><th>File</th></tr>
    <tr><th>CANONICAL_FORM</th><th>CANONICAL_FORM</th></tr>
    <c:forEach var="diff" items="${comparison.unitDesignatorDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.current == null}">
      	  <td>Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.current.canonicalForm}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.other == null}">
      	  <td>Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.other.canonicalForm}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

<h3>Locality Mappings Differences</h3>
<c:choose>
  <c:when test="${comparison.localityMappingDiffs == null || comparison.localityMappingDiffs.isEmpty()}">
    <p><b>No Differences</b></p>
  </c:when>
  <c:otherwise>
    <table class="diffTable">
    <tr><th colspan="3">Live Config</th><th colspan="3">File</th></tr>
    <tr><th>LOCALITY_ID</th><th>INPUT_STRING</th><th>CONFIDENCE</th>
      <th>LOCALITY_ID</th><th>INPUT_STRING</th><th>CONFIDENCE</th></tr>
    <c:forEach var="diff" items="${comparison.localityMappingDiffs}">
      <tr>
      <c:choose>
        <c:when test="${diff.current == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.current.localityId}</td><td>${diff.current.inputString}</td><td>${diff.current.confidence}</td>
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${diff.other == null}">
      	  <td colspan="3">Not Present</td>
      	</c:when>
      	<c:otherwise>
          <td>${diff.other.localityId}</td><td>${diff.other.inputString}</td><td>${diff.other.confidence}</td>
        </c:otherwise>
      </c:choose>
      </tr>
	</c:forEach>
	</table>
  </c:otherwise>
</c:choose>  

</div>

<%@ include file="../../footer.jsp" %>