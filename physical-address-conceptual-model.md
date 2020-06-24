# Conceptual Model of Physical Address


A civic address is a civic number assigned to a site assigned to a street in a locality in a subCountry. For example:

     1175 Douglas St, Victoria, BC


A non-civic address is a site-name assigned to a site assigned to a street in a locality in a subCountry.

      Centennial Candle -- Laurel Lane, Victoria, BC


An intersection address is a sequence of the names of all roads that meet at a single intersection in a locality in a subCountry. For example:

       Dallas Rd and Government St, Victoria, BC
       Douglas St and Gorge Rd and Hillside Ave and Government St, Victoria, BC

A physical address is the compound name of a site and its coordinate location and can take the form of a civic address, an non-civic address, or an intersection address. Even though it has a coordinate location, a physical address is a data type, not a geographic feature.

A site is any geographic feature that is connected to a road. Examples include house, cabin, building, building complex, empty lot, RV park, manufactured home park, race-track, stadium, golf course, industrial plant, bandshell, hospital, university, recreation centre, place of worship, campground, motel, park entrance or washroom, water tower, municipal pumping station, hydro sub-station, unit within a building, and building within a complex.

## Civic Address Data Type


Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
unitDesignator|String|Official unit designator abbreviation (e.g., APT)|No|No
unitNumberPrefix|String|A single letter attached to the front of a Unit number (e.g., the A in A100)|No|No
unitNumber|String|Unit number of a unit(e.g., the 100 in A100)|No|No
unitNumberSuffix|String|A single letter appended to the UNIT_NUMBER (e.g.,the A in 102A)|No|No
siteName|String|name of building (e.g., University of Victoria), part of a building (e.g., Emergency) or landmark name (e.g., Centennial Candle)|no|yes
fullSiteDescriptor|String|Full site descriptor starting with unit and SITE_NAME followed by all units and SITE_NAMEs in parent site hierarchy separated by commas (e.g., RM 104, Student Union Building, University of Victoria)|No|No
civicNumber|Number| civic number, usually a positive integer (e.g., 1321)|Yes|No
civicNumberSuffix|String|Civic number suffix (e.g., A)|No|No
streetName|String|Street name|Yes|No
streetType|String|Official abbreviation of street type (e.g., Ave, Blvd, Hwy)|No|No
isStreetTypePrefix|Boolean| True if street type appears before street name. For example, the street type HWY appears before the streetName 17 in Hwy 17|No|No
streetDirection|String|Official street direction abbreviation (e.g., N,S,E,W,NE,SE,NW,SW); Prefix and suffix street directions in the same address (e.g., 103 N 52nd St SW) are not allowed|No|No
isStreetDirectionPrefix|Boolean|true if street direction appears before street name as in SW Marine Dr|No|No
locality|String|Locality name (e.g., Victoria)|Yes|Yes
localityDescriptor|String|type of locality(e.g.,Municipality,Unincorporated)|Yes|Yes
subCountryCode|String|ISO 3166-2-CA sub-country code (e.g., BC, YT)|Yes|Yes
isOfficial|Boolean|True if address is official; False if unofficial (e.g., former address)|Yes|Yes
location|Point|Location of the site; the point must lie within the site or parcel containing the site (e.g., a point on the roof of a house just above the front door)|Yes|Yes
accessLocation|Point|The point at which the site's driveway, walkway, or access road meets the street named in the site's address|Yes|Yes
centrelineLocation|Point|The nearest point on the road centreline to the site's LOCATION. The road centreline is the centreline of the street named in the site's address. If the named street is divided, the road centreline on the same side as the site should be used|Yes|Yes
isNonCivic|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes
relativeLocation|String|Relative geographic location of a non-civic address (e.g., Lonely Cabins - 43 km west of Stui on N side of Hwy 20)|No|Yes	
footprintDescriptor|String| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
footprint|OGC WKT|Spatial extent of the site|No|No
