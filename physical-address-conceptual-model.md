
## Physical Address Elements


Element Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
UNIT_DESIGNATOR |String|Official unit designator abbreviation (e.g., APT)|No|No
UNIT_NUMBER_PREFIX|String|A single letter attached to the front of a Unit number (e.g., the A in A100)|No|No
UNIT_NUMBER|String|Unit number of a unit(the 100 in A100)|No|No
UNIT_NUMBER_SUFFIX|String|A single letter appended to the UNIT_NUMBER (e.g.,the A in 102)|No|No
SITE_NAME |String|building or landmark name (e.g., Centennial Candle)|yes|yes
FULL_SITE_DESCRIPTOR|String|Full site descriptor starting with unit and SITE_NAME followed by all units and SITE_NAMEs in parent site hierarchy separated by commas (e.g., RM 104, Student Union Building, University of Victoria)|No|No
CIVIC_NUMBER|Number| civic number, usually a positive integer (e.g., 1321)|Yes|No
CIVIC_NUMBER_SUFFIX|String|Civic number suffix (e.g., A)|No|No
STREET_NAME|String|Street name|Yes|No
STREET_TYPE|String|Street type|No|No
IS_STREET_TYPE_PREFIX|Boolean| True if street type appears before street name as in HWY 17|No|No
STREET_DIRECTION|String|Official street direction abbreviation (e.g., N,S,E,W,NE,SE,NW,SW); Prefix and suffix street directions in the same address (e.g., 103 N 52nd St SW) are not allowed|No|No
IS_STREET_DIRECTION_PREFIX|Boolean|true if street direction appears before street name as in SW Marine Dr|No|No
LOCALITY|String|Locality name (e.g., Victoria)|Yes|Yes
LOCALITY_DESCRIPTOR|String|type of locality(e.g.,Municipality,Unincorporated)|Yes|Yes
SUB_COUNTRY_CODE|String|ISO 3166-2-CA sub-country code (e.g., BC, YT)|Yes|Yes
IS_OFFICIAL_ADDRESS|Boolean|True if address is official; False if unofficial (e.g., former address)|Yes|Yes
LOCATION|Point|Location of the site; the point must lie within the site or parcel containing the site (e.g., a point on the roof of a house just above the front door)
ACCESS_LOCATION|Point|The point at which the site's driveway, walkway, or access road meets the street named in the site's address
CENTRELINE_LOCATION|Point|The nearest point on the road centreline to the site's LOCATION. The road centreline is the centreline of the street named in the site's address.
IS_NON_CIVIC_ADDRESS|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes
RELATIVE_LOCATION|String|Relative geographic location of a non-civic address (e.g., Lonely Cabins - 43 km west of Stui on N side of Hwy 20)|No|Yes	
TAGS|String| Comma-separated list of descriptive tags (e.g. stadium)|No|No
FOOTPRINT_DESCRIPTOR|String| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
FOOTPRINT|OGC WKT|Spatial extent of the site|No|No
