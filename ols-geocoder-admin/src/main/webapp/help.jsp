<%--

    Copyright 2008-2015, Province of British Columbia
    All rights reserved.

--%>
<%@ include file="header.jsp" %>
<div class="bodyContent">
<h1>Location Services Administration Help</h1>
<hr><h2>General Information</h2>
<p>For all of the editable values you can change through this Admin Interface, they will be change in the database configuration tables. This will not update any existing, running services. Restarting Tomcat and reloading the CPF plug-in will be required before the parameters are used in the live services. This is because the services have all the required configuration in memory and do not interact with the configuration database on a live basis for performance reasons.</p>	 
<hr><h2>Parameter Defaults Page</h2>

<p>There are a number of general parameters that can be edited using this page. To edit them simply change the field values and press 'Save all Values'.

<h3>apiUrl</h3>
<p>The URL to the API documentation.</p>

<h3>copyrightLicense</h3>
<p>The URL link to the copyright license information.</p>

<h3>copyrightNotice</h3>
<p>The text of the copyright notice.</p>

<h3>defaultLookAtRange</h3>
<p>The default lookAtRange for KML output</p>

<h3>defaultSetBack</h3>
<p>This parameter is used once a geocoded point has been found. The setback is the perpendicular distance from the road centerline the final returned co-ordinate will be, in meters.</p> 

<h3>disclaimer</h3>
<p>The URL link to the disclaimer information.</p>

<h3>glossaryBaseURL</h3>
<p>.The Base URL of the documentation glossary file.</p>

<h3>kmlStylesUrl</h3>
<p>The url where the kml styles are located.</p>

<h3>maxReverseGeocodeDistance</h3>
<p>This is the maximum height and/or width of a bounding box query in meters, and is also the maximum distance for the "nearest site or intersection.</p>

<h3>maxWithinResults</h3>
<p>This is the maximum number of results (sites or intersections) returned from a within bounding box query.</p>

<h3>moreInfoURL</h3>
<p>The URL link to the documentation.</p>

<h3>privacyStatement</h3>
<p>The URL link to the privacy statement</p>

<h3>provinceX</h3>
<p>The X value for the center of the province.</p>

<h3>provinceY</h3>
<p>The YX value for the center of the province.</p>

<h3>resultsLimit</h3>	
<p>This is the limit of how many matches one can request from the system.</p>

<hr><h2>Match Faults Values Page</h2>
<h3>Geocoder Precision Points</h3>
<p>These editable values are the scores awarded for the match based on the level of detail that was able to be matched. They should not be changed drastically, only slightly to tweak the results.</p>
<p>To edit them simply change the field values and press 'Save all Values'. This will save all fields on the page back to the database</p>
  
<h3>Geocoder Match Fault Values</h3>
<p>These are the values used when subtracting points for each problem found while matching the input address string.</p> 
<p>To edit them simply change the field values and press 'Save all Values'. This will save all fields on the page back to the database</p>

<hr><h2>Abbreviation Mappings Page</h2>
<p>Abbreviation mappings allow administrators to create a mapping from an abbreviation to the official Abbreviation used in street names such as route -&gt; Rte. It can also be used to simple convert text to other text to allow for more possible matches such as tenth -&gt; 10</p>
<h3>Add New</h3>
<p>To create a new mapping simply fill out the two text fields of what you would like to map and press the 'Save' button</p>
<h3>Edit/Delete Mappings</h3>
<p>To edit or delete a mapping, press the edit button next to the mapping you wish to edit. Then simply change one or both of the fields in the mapping and press 'Save Changes' to edit the mapping, or press the 'Delete this Mapping' button to remove the mapping.</p>
      
<hr><h2>Locality Mappings Page</h2>
<p>Locality mappings allow administrators to create a mapping from a locality description to the official localities used in the Geocoder.</p>
<h3>Add New</h3>
<p>To create a new mapping simply fill out the three fields listed, selecting the input text you wish to map to the locality from the dropdown as well as the confidence. The confidence is how confident one would be that the mapping given is the intent of the user, for something like gabriola -&gt; Gabriola Island, you may be highly confident(100), for something like Nanaimo -&gt; Gabriola Island you may be less confident, but it's possible(80). Then press the 'Save' button to create the new mapping</p>
<h3>Edit/Delete Mappings</h3>
<p>To edit or delete a mapping, press the edit button next to the mapping you wish to edit. Then simply change any or all of the 3 fields in the mapping and press 'Save Changes' to edit the mapping, or press the 'Delete this Mapping' button to remove the mapping.</p>

<hr><h2>Unit Designators Page</h2>
<p>Unit Designators are strings which identify either a unit, or type of unit, in which case they are followed by a number identifying the exact unit. When defining a unit designator, the administrator creates a mapping from an acceptable unit designator string to the canonical form of the unit designator, and also specifies whether a unit number is required to follow the unit designator.</p>
<h3>Add New</h3>
<p>To create a new Unit Designator simply fill out the three fields listed, entering the input string you wish to accept as a unit designator, the canonical form of the unit designator, as you wish it to be display to the user in query results, and the String "Yes" if a unit number is required to go along with the designator.  Then enter the admin password and press the 'Save' button to create the new mapping</p>
<h3>Edit/Delete Mappings</h3>
<p>To edit or delete a Unit Designator, press the edit button next to the Unit Designator you wish to edit. Then simply change any or all of the 3 fields and press 'Save Changes' to edit the Unit Designator, or press the 'Delete this Mapping' button to remove the mapping.</p>

</div>
<%@ include file="footer.jsp" %>