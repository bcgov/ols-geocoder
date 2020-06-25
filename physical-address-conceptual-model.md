# Conceptual Model of Physical Address

Topics: fullAddress as business unique identifier, what is a physical address, difference from mailing address, uses, address fabric designs, addresses in the real world, adding a sense of history, how is address location defined, 

### What is a physical address?
A physical address is a compound name assigned to a geographic feature connected to a road network. We call such a geographic feature a site. Examples of site include house, building, unit within a building, special gate or entrance to a building, building complex such as a university campus or hospital, mobile home or RV park, campground, water tower, industrial plant, and place of worship. 

A physical address can take the form of a civic address, a non-civic address, or an intersection address.

Addresses of buildings without units
Addresses of buildings assigned single or multiple civic numbers
Addresses that have no civic number (landmark or non-civic addresses)
Addresses of buildings with multiple units
Addresses of buildings with special entrances (e.g., entrance pavillion, emergency access)
Addresses of complexes that contain multiple buildings
Addresses of buildings that contain sub-buildings (e.g., floors, wards, wings)


Nature of Site | Address
---: | ---
Single House assigned a civic number|1211 16 St NE, Salmon Arm, BC
Unit within a building|Unit 401 -- 1225 Douglas St, Victoria, BC
Special entrance of a industrial complex|Entrance, Delta Port Terminal -- 2 Roberts Bank Rd, Delta, BC
Sub-building within a building|Emergency Unit, Royal Jubilee Hospital -- 1952 Bay St, Victoria, BC
Building within a complex|Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC
Room within a building within a complex|Rooms 100, Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC

A civic address is a civic number assigned to a site assigned to a street in a locality in a subCountry. For example,

     1175 Douglas St, Victoria, BC

A civic address may also contain the name of a subsite of a site assigned a civic number a site as in the following:

     Unit 101 -- 740 Gorge Rd W, Saanich, BC

Unit 101 is the name of a unit within a building assigned a civic address of 740 Gorge Rd W, Saanich, BC

     Unit A301 -- 810 Esquimalt Rd, Esquimalt, BC
     
Unit A301 is name of a unit within a building called "A" with an apartment complex assigned a civic address of 810 Esquimalt Rd, Esquimalt, BC




A non-civic address is a site-name assigned to a site assigned to a street in a locality in a subCountry.

      Centennial Candle -- Laurel Lane, Victoria, BC


An intersection address is a sequence of the names of all roads that meet at a single intersection in a locality in a subCountry. For example:

       Dallas Rd and Government St, Victoria, BC
       Douglas St and Gorge Rd and Hillside Ave and Government St, Victoria, BC



## Site Address Data Type


Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
fullAddress|String|Full address in Single-Line Address Format|Y|Y
unitDesignator|String|Official unit designator abbreviation (e.g., APT)|No|No
unitNumberPrefix|String|A single letter attached to the front of a Unit number (e.g., the A in A100)|No|No
unitNumber|String|Unit number of a unit(e.g., the 100 in A100)|No|No
unitNumberSuffix|String|A single letter appended to the UNIT_NUMBER (e.g.,the A in 102A)|No|No
siteName|String|name of building (e.g., University of Victoria), part of a building (e.g., Emergency) or landmark name (e.g., Centennial Candle)|no|yes
fullSiteDescriptor|String|Full site descriptor starting with unit and SITE_NAME followed by all units and SITE_NAMEs in parent site hierarchy separated by commas (e.g., RM 104, Student Union Building, University of Victoria)|No|No
fullAddress|String|Full address in Single-Line Address Format|Y|Y
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
isOfficial|Boolean|True if address is designated as official by the appropriate address authority; False if unofficial (e.g., former address)|Yes|Yes
location|Point|Location of the site; the point must lie within the site or within the parcel containing the site (e.g., a point on the roof of a house just above the front door)|Yes|Yes
accessLocation|Point|The point at which the site's driveway, walkway, or access road meets the street named in the site's address|Yes|Yes
centrelineLocation|Point|The nearest point on the road centreline to the site's LOCATION. The road centreline is the centreline of the street named in the site's address. If the named street is divided, the road centreline on the same side as the site should be used|Yes|Yes
isNonCivic|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes
relativeLocation|String|Relative geographic location of a non-civic address (e.g., Lonely Cabins - 43 km west of Stui on N side of Hwy 20)|No|Yes	
footprintDescriptor|String| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
footprint|OGC WKT|Spatial extent of the site|No|No
