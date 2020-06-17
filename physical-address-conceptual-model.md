
## Schema Definition


Property Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
UNIT_DESIGNATOR |String|Canada Post unit designator (e.g., APT)|No|No
UNIT_NUMBER_PREFIX|String|a single letter that should appear at the beginning of a unit number (e.g., the A in A100)|No|No
UNIT_NUMBER|String|unit number or letter of a unit with a site (the 100 in A100)|No|No
UNIT_NUMBER_SUFFIX|String|A single letter that should appear at the end of a unit number(e.g.,the A in 102)|No|No
SITE_NAME |String|building or landmark name (e.g., Centennial Candle)|yes|yes
FULL_SITE_DESCRIPTOR|String|full site descriptor starting with unit and SITE_NAME followed by all units and SITE_NAMEs in parent site hierarchy separated by commas (e.g., RM 104, Student Union Building, University of Victoria)|No|No
CIVIC_NUMBER|Number| civic number, usually a positive integer (e.g., 1321)|Yes|No
CIVIC_NUMBER_SUFFIX|String|Canada Post civic number suffix (e.g., A)|No|No
STREET_NAME|String|Street name|Yes|No
STREET_TYPE|String|Street type|No|No
IS_STREET_TYPE_PREFIX|Boolean| True if street type appears before street name as in HWY 17|No|No
STREET_DIRECTION|String|Canada Post street direction (e.g., NW); Note Canada Post does not allow prefix and suffix street direction in same address as in: 103 N 52 St SW|No|No
IS_STREET_DIRECTION_PREFIX|Boolean|true if street direction appears before street name as in SW Marine Dr|No|No
LOCALITY|String|Locality (e.g., Victoria)|Yes|Yes
LOCALITY_DESCRIPTOR|String|type of locality|(e.g., Municipality)|Yes|Yes
PROVINCE_CODE|String|Canada Post two-character province code|Yes|Yes
IS_NON_CIVIC_ADDRESS|Boolean|True if address has no assigned civic number|Yes|Yes
IS_OFFICIAL_ADDRESS|Boolean|True if address is official; False if unofficial (e.g., former address)|Yes|Yes
RELATIVE_LOCATION|String|Relative geographic location of a non-civic address (e.g., Lonely Cabins - 43 km west of Stui on N side of Hwy 20)|No|Yes	
SITE_POINT_DESCRIPTOR|String|one of site (somewhere on the site or the parcel containing the site), rooftop, frontDoor, internalDoor, entrance, frontGate|No|No
SITE_LAT|Number|site latitude|Yes|Yes
SITE_LON|Number)|site longitude|Yes|Yes
SITE_TAGS|String| Comma-separated list of descriptive tags (e.g. stadium)|No|No
ACCESS_POINT_LAT|Number|Only needed if access point is different than site point or super site point|No|Yes
ACCESS_POINT_LON|Number|Only needed if access point is different than site point or super site point|No|Yes
FOOTPRINT_DESCRIPTOR|String| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
FOOTPRINT|OGC WKT|geometry of site footprint in OGC Well-Known Text format. Can use other geometry standards in other formats (e.g., GML GeoJson)|No|No
