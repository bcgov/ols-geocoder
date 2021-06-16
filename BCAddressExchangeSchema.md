# Physical Address Exchange Standard Draft v0.10
### Change history
v0.10 June 16, 2021 - made special entrance/exit unitDesignators more readable<br>
v0.9 May 3, 2021 - updated support for entrances and exits as units within a building; added concept of special access points as units<br>

v0.8 Feb 25, 2021 - added support for entrances and exits as units within a building; sites no longer have to be named to have special entrances<br>

v0.7 Feb 20, 2021 - removed SITE_POINT_DESCRIPTOR as it is rarely provided by address authorities, added TOC<br>

v0.6 Jul 28, 2020 - improved Example 6 (Special entrances)<br> 

v0.5 Jun 15, 2020 - eliminated option to use ExtraPoints to define an unlimited number of additional coordinate locations for a given site. Extra site and access points can be added to a given site using subsites; added uses cases to handle emergency entrances, etc. as subsites, each with its own sitePoint and accessPoint.<br>

v0.4 Jun, 2019 - added SUPER_SITE_YOURID to schema, changed schema to allow any number of extra points, improved examples, added example of single apartment building, improved readability, fixed formatting of schema definition table.

[Introduction](#intro)<br>
[Example 1 - A house with a single civic number and no units](#ex1)<br>
[Example 2 - A single apartment building with multiple floors and units](#ex2)<br>
[Example 3 - An apartment complex with buildings distinguished by unit number prefix](#ex3)<br>
[Example 4 - A complex with multiple levels of units](#ex4)<br>
[Example 5 - A complex of buildings](#ex5)<br>
[Example 6 - A building with special entrances and exits](#ex6)<br>
[Unit Designators of entrances, exits, and other building parts](#specialUnitDesignators)<br>
[Schema definition](#schema)<br>
[Unit designators](#unitDesignators)<br><br>


<a name=intro></a>
## Introduction
The Physical Address Exchange Schema can be used to exchange reference physical addresses between government agencies such as address authorities, emergency response agencies, geocoder service providers, etc. The schema supports the exchange of the following types of address situations:

- Addresses of buildings without units
- Addresses of buildings assigned single or multiple civic numbers
- Addresses that have no civic number (landmark or non-civic addresses)
- Addresses of buildings with multiple units
- Addresses of buildings with special entrances (e.g., entrance pavillion, emergency access)
- Addresses of complexes that contain multiple buildings
- Addresses of buildings that contain sub-buildings (e.g., floors, wards, wings)

An address can have site and vehicle access locations. Units within buildings and buildings within complexes can have their own rooftop and vehicle access locations (e.g., townhouse units within a complex, buildings within a campus).

Buildings, complexes, outdoor areas, and any other site that can be assigned an address may also have its own footprint.

In all examples, attributes that are null are not shown. The full address exchange schema is the last section in this document.
References such as (aReal), (aMultiPolygon), and (aPolygon) represent an arbitrary value that conforms to the named type (e.g., Real, Multipolygon, Polygon)

<a name=ex1> </a>
## Example 1 - A house with a single civic number and no units
37 Olympia Ave, Victoria, BC

Field | Value
-----: | ------
CIVIC_NUMBER|37
STREET_NAME|Olympia
STREET_TYPE|Ave
LOCALITY|Victoria
PROVINCE_CODE|BC
SITE_LAT| (aReal)
SITE_LON| (aReal)
ACCESS_POINT_LAT| (aReal)
ACCESS_POINT_LON| (aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT| (aPolygon)

<a name=ex2> </a>
## Example 2 - A single apartment building with multiple floors and units
740 Gorge Rd W, Saanich, BC has three floors with 4 units each numbered, 101-104, 201-204, and 301-304. Site and access points of building, not units, are known.

The following exchange data records will represent the above addresses:

Field | Value
-----: | ------
CIVIC_NUMBER|740
STREET_NAME|Gorge
STREET_TYPE|Rd
STREET_DIRECTION|W
LOCALITY|Saanich
PROVINCE_CODE|BC
SITE_LAT| (aReal)
SITE_LON| (aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(aMultiPolygon)

Field | Value
-----: | ------
UNIT_DESIGNATOR|APT
UNIT_NUMBER|101-104,201-204,301-304
CIVIC_NUMBER|740
STREET_NAME|Gorge
STREET_TYPE|Rd
STREET_DIRECTION|W
LOCALITY|Saanich
PROVINCE_CODE|BC

If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

Unit 101, 740 Gorge Rd W, Saanich, BC

Unit 102, 740 Gorge Rd W, Saanich, BC

Unit 103, 740 Gorge Rd W, Saanich, BC

Unit 104, 740 Gorge Rd W, Saanich, BC

Unit 201, 740 Gorge Rd W, Saanich, BC

Unit 202, 740 Gorge Rd W, Saanich, BC

Unit 203, 740 Gorge Rd W, Saanich, BC

Unit 204, 740 Gorge Rd W, Saanich, BC

Unit 301, 740 Gorge Rd W, Saanich, BC

Unit 302, 740 Gorge Rd W, Saanich, BC

Unit 303, 740 Gorge Rd W, Saanich, BC

Unit 304, 740 Gorge Rd W, Saanich, BC

All addresses will be assigned the site and accessPoint locations assigned to 740 Gorge Rd W, Saanich, BC

<a name=ex3></a>
## Example 3 - An apartment complex with buildings distinguished by unit number prefix

Here is a common situation where a complex has building names that are single letters embedded in a unit number as in APT A105.

810 Esquimalt Rd, Esquimalt, BC has two buildings: A and B. Each building has four floors with 10 units each numbered 100-110, 200-210, 300-310, and 400-410. Each building has its own site location and footprint but share a single access point. Unit locations are assigned the site and accessPoints of their respective buildings. 

The following exchange data records will represent the above addresses:

Field | Value | Comment
-----: | ------ | -----
CIVIC_NUMBER|810
STREET_NAME|Esquimalt
STREET_TYPE|Rd
LOCALITY|Esquimalt
PROVINCE_CODE|BC
SITE_LAT| (aReal)
SITE_LON| (aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(aMultiPolygon) | shape of complex


Field | Value | Comment
-----: | ------ | -----
UNIT_DESIGNATOR| APT
UNIT_NUMBER_PREFIX|A|represents Building A
UNIT_NUMBER|100-110,200-210,300-310,400-410
CIVIC_NUMBER|810
STREET_NAME|Esquimalt
STREET_TYPE|Rd
LOCALITY|Esquimalt
PROVINCE_CODE|BC
SITE_LAT|(aReal)| lat of building A parcel point
SITE_LON|(aReal)| lon of building A parcel point
ACCESS_POINT_LAT|(aReal)|lat of building A access point
ACCESS_POINT_LON|(aReal)|lon of building A access point 

Field | Value | Comment
-----: | ------ | -----
UNIT_DESIGNATOR| APT
UNIT_NUMBER_PREFIX|B|represents Building B
UNIT_NUMBER|100-110,200-210,300-310,400-410
CIVIC_NUMBER|810
STREET_NAME|Esquimalt
STREET_TYPE|Rd
LOCALITY|Esquimalt
PROVINCE_CODE|BC
SITE_LAT|(aReal)| lat of building B parcel point 
SITE_LON|(aReal)| lon of building B parcel point
ACCESS_POINT_LAT|(aReal)|lat of building B access point
ACCESS_POINT_LON|(aReal)|lon of building B access point 


If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

APT A100, 810 Esquimalt Rd,Esquimalt,BC

APT B407, 810 Esquimalt Rd,Esquimalt,BC

APT B210, 810 Esquimalt Rd,Esquimalt,BC
<a name=ex4></a>
## Example 4 - A complex with multiple levels of units

Vancouver International Airport, 3211 Grant McConachie Way, Richmond, BC has the following terminals and gates:
- Terminal A
  - Gate 1-35
- Terminal B
  - Gate 1-40
- Terminal C
  - Gate 1-20

The following data exchange records will represent the above addresses:

Field | Value
----:|----
SITE_NAME|Vancouver International Airport
CIVIC_NUMBER|3211
STREET_NAME|Grant McConnachie
STREET_TYPE|Way
LOCALITY|Richmond
PROVINCE_CODE|BC
SITE_LAT|(aReal)
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|complex
FOOTPRINT|(aMultiPolygon)

Field | Value
----:|----
UNIT_DESIGNATOR|Terminal
UNIT_NUMBER|A
SUPER_FULL_SITE_DESCRIPTOR|Vancouver International Airport
CIVIC_NUMBER|3211
STREET_NAME|Grant McConnachie
STREET_TYPE|Way
LOCALITY|Richmond
PROVINCE_CODE|BC
SITE_LAT|(aReal)
SITE_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(polygon)

Field | Value
----:|----
UNIT_DESIGNATOR|Terminal
UNIT_NUMBER|B
SUPER_FULL_SITE_DESCRIPTOR|Vancouver International Airport
CIVIC_NUMBER|3211
STREET_NAME|Grant McConnachie
STREET_TYPE|Way
LOCALITY|Richmond
PROVINCE_CODE|BC
SITE_LAT|(aReal)
SITE_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(polygon)

Field | Value
----:|----
UNIT_DESIGNATOR|Terminal
UNIT_NUMBER|C
SUPER_FULL_SITE_DESCRIPTOR|Vancouver International Airport
CIVIC_NUMBER|3211
STREET_NAME|Grant McConnachie
STREET_TYPE|Way
LOCALITY|Richmond
PROVINCE_CODE|BC
SITE_LAT|(aReal)
SITE_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(polygon)


Field | Value
----:|----
UNIT_DESIGNATOR|Gate
UNIT_NUMBER|1-35
SUPER_FULL_SITE_DESCRIPTOR|Terminal A -- Vancouver International Airport
CIVIC_NUMBER|3211
STREET_NAME|Grant McConnachie
STREET_TYPE|Way
LOCALITY|Richmond
PROVINCE_CODE|BC
SITE_LAT|(aReal) 
SITE_LON|(aReal)

The gates of terminals B and C are represented similarly.

If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC

Terminal C, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC

Gate 23, Terminal A, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC 

Gate 7, Terminal B, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC 

Each Terminal and Gate can have its own site and access locations

<a name=ex5></a>
## Example 5 - A complex of buildings

Given the following addresses for UVIC:

Rooms 100-110 in the Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC

Michele Pujol Room, Student Union Building, University of Victoria -- 38800 Finnerty Rd, Saanich, BC

The following exchange data records will represent the above addresses:


Field | Value
----:|----
SITE_NAME|University of Victoria
CIVIC_NUMBER|3800
STREET_NAME|Finnerty
STREET_TYPE|Rd
LOCALITY|Victoria
PROVINCE_CODE|BC
SITE_LAT|(aReal) 
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|complex
FOOTPRINT|(aMultiPolygon)

Field | Value
----:|----
SITE_NAME|Student Union Building
SUPER_FULL_SITE_DESCRIPTOR|University of Victoria
CIVIC_NUMBER|3800
STREET_NAME|Finnerty
STREET_TYPE|Rd
LOCALITY|Saanich
PROVINCE_CODE|BC
SITE_LAT|(aReal) 
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(polygon)

Field | Value
----:|----
SITE_NAME|Clearihue Building
SUPER_FULL_SITE_DESCRIPTOR|University of Victoria
CIVIC_NUMBER|3800
STREET_NAME|Finnerty
STREET_TYPE|Rd
LOCALITY|Saanich
PROVINCE_CODE|BC
SITE_LAT|(aReal) 
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|building
FOOTPRINT|(polygon)

Field | Value
----:|----
SITE_NAME|Michele Pujol Room
SUPER_FULL_SITE_DESCRIPTOR|Student Union Building -- University of Victoria
CIVIC_NUMBER|3800
STREET_NAME|Finnerty
STREET_TYPE|Rd
LOCALITY|Saanich
PROVINCE_CODE|BC
SITE_LAT|(aReal)
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal)
ACCESS_POINT_LON|(aReal)
FOOTPRINT_DESCRIPTOR|indoorArea
FOOTPRINT|(polygon)

Field | Value
----:|----
UNIT_DESIGNATOR|Room
UNIT_NUMBER|100-110
SUPER_FULL_SITE_DESCRIPTOR|Clearhue Building -- University of Victoria
CIVIC_NUMBER|3800
STREET_NAME|Finnerty
STREET_TYPE|Rd
LOCALITY|Saanich
PROVINCE_CODE|BC

<a name=ex6></a>
## Example 6 - A building with special entrances and exits

Buildings may have special entrances for emergency access or service staff, and special exits for emergency egress. These are represented as units (subsites) within the building. For example, assume the HR MacMillan Space Centre in Vancouver has two numbered emergency exits and an emergency responder entrance with an adjacent emergency access lane that runs to Chestnut St. Here are the site and subsite addresses to be exchanged:

1100 Chestnut St, Vancouver, BC

EMERGENCY-ENTRANCE -- 1100 Chestnut St, Vancouver, BC

EMERGENCY-EXIT 1 -- 1100 Chestnut St, Vancouver BC

EMERGENCY-EXIT 2 -- 1100 Chestnut St, Vancouver BC


The following exchange data records will represent the above addresses:

Field | Value
----:|----
CIVIC_NUMBER|1100
STREET_NAME|Chestnut
STREET_TYPE|St
LOCALITY|Vancouver
PROVINCE_CODE|BC


Field | Value
----:|----
UNIT_DESIGNATOR|EMERGENCY-ENTRANCE
CIVIC_NUMBER|1100
STREET_NAME|Chestnut
STREET_TYPE|St
LOCALITY|Vancouver
PROVINCE_CODE|BC
SITE_LAT|(aReal); location of emergency entrance on building
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal) ;location of intersection of emergency access lane and Chestnut St
ACCESS_POINT_LON|(aReal)


Field | Value
----:|----
UNIT_DESIGNATOR|EMERGENCY-EXIT
UNIT_NUMBER|1
CIVIC_NUMBER|1100
STREET_NAME|Chestnut
STREET_TYPE|St
LOCALITY|Vancouver
PROVINCE_CODE|BC
SITE_LAT|(aReal); location of emergency exit 1 on building
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal) ;location of intersection of access road and Chestnut St if different from primary site access point
ACCESS_POINT_LON|(aReal)

Field | Value
----:|----
UNIT_DESIGNATOR|EMERGENCY-EXIT
UNIT_NUMBER|2
CIVIC_NUMBER|1100
STREET_NAME|Chestnut
STREET_TYPE|St
LOCALITY|Vancouver
PROVINCE_CODE|BC
SITE_LAT|(aReal); location of emergency exit 2 on building
SITE_LON|(aReal)
ACCESS_POINT_LAT|(aReal) ;location of intersection of access road and Chestnut St if different from primary site access point
ACCESS_POINT_LON|(aReal)
<br>

<a name=specialUnitDesignators></a>
## Unit Designators of entrances, exits, and other building parts
The following entrance unit designators are proposed to handle the special types of entrances and exits in a standard way:

|unitDesignator|Description|
|--|--|
BUS-ENTRANCE|BUS Entrance
BUS-EXIT|BUS Exit only
BUS-PARKING-ENTRANCE|Bus parking entrance
BUS-PARKING-EXIT|Bus parking exit only
DELIVERY-ENTRANCE| Delivery entrance
DELIVERY-EXIT| Delivery exit only
EMERGENCY-ENTRANCE|Emergency responder entrance
EMERGENCY-EXIT|Emergency exit only (sounds alarm)
ENTRANCE|Entrance
EXIT|Exit only
PARKING-ENTRANCE|Parking entrance
PARKING-EXIT|Parking exit only
SERVICE-ENTRANCE|Service entrance
SERVICE-EXIT|Service exit only
TRUCK-ENTRANCE|TRUCK entrance
TRUCK-EXIT|TRUCK exit only
TRUCK-PARKING-ENTRANCE|Truck parking entrance
TRUCK-PARKING-EXIT|Truck parking exit only

Like any unitDesignator, entrances may be numbered (eg., ENTRANCE 1, EXIT 4)

The following Canada Post unit designators define other building parts and are also supported:

|unitDesignator|Description|
|--|--|
LOBBY| Lobby
MEZZ|Mezzanine
UPPR|Upper floor
LWR|Lower floor
REAR|Rear of building
BSMNT|Basement
FLR| Floor


<a name=schema></a>
## Schema Definition
This schema can be used in any common text format that supports named properties including CSV,TSV,JSON, and XML

Field Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
yourId|String|Unique identifier in your local address management system (e.g., X0233212)|No|No
unitDesignator|String|unit designator (e.g., APT, UNIT); includes Canada Post unit designators plus standard entrance designators (e.g., ENTRANCE A, EMERGENCY-ENTRANCE, EMERGENCY-EXIT, EXIT, SERVICE-ENTRANCE)|No|No
unitNumberPrefix|String|a single letter or sequence of letter ranges separated by commas (e.g., A-D,J,M-P)|No|No
unitNumber|String|unit number or letter or sequence of unit number/letter ranges separated by commas (e.g., 100-119, 200-219)|No|No
unitNumberSuffix|String|Canada Post unit number suffix (e.g., C)|No|No
siteName|String|building or landmark name (e.g., Centennial Candle)|no|yes
superFullsiteDescriptor|String|names of all units and sites in parent site hierarchy separated by double-dash (e.g., Student Union Building -- University of Victoria)|No|No
superSiteYourId|Sting|Unique identifier of super site|No|No
civicNumber|Number| civic number, usually a positive integer (e.g., 1321)|Yes|No
civicNumberSuffix|String|Canada Post civic number suffix (e.g., A)|No|No
streetName|String|Street name|Yes|No
streetType|String|Street type|No|No
isStreetTypePrefix|Boolean| True if street type appears before street name as in HWY 17|No|No
streetDirection|String|Canada Post street direction (e.g., NW); Note Canada Post does not allow prefix and suffix street direction in same address as in: 103 N 52 St SW|No|No
isStreetDirectionPrefix|Boolean|true if street direction appears before street name as in SW Marine Dr|No|No
locality|String|Locality (e.g., Victoria)|Yes|Yes
localityDescriptor|String|type of locality|(e.g., Municipality)|Yes|Yes
provinceCode|String|Canada Post two-character province code|Yes|Yes
isNonCivicAddress|Boolean|True if address has no assigned civic number|Yes|Yes
isOfficialAddress|Boolean|True if address is official; False if unofficial (e.g., former address)|Yes|Yes
relativeLocation|String|Relative geographic location of a non-civic address (e.g., Lonely Cabins - 43 km west of Stui on N side of Hwy 20)|No|Yes	
siteLat|Number|site latitude|Yes|Yes
siteLon|Number)|site longitude|Yes|Yes
siteTags|String| Comma-separated list of descriptive tags (e.g. stadium)|No|No
accessPointLat|Number|Only needed if access point is different than site point or super site point|No|Yes
accessPointLon|Number|Only needed if access point is different than site point or super site point|No|Yes
footprintDescriptor|String| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
footprint|OGC WKT|geometry of site footprint in OGC Well-Known Text format. Can use other geometry standards in other formats (e.g., GML GeoJson)|No|No

<a name=unitDesignators></a>
## Unit Designators
Here is the list of standard unit designators. Any designator can also have an associated unit number.

|Name|Description|Canada Post Standard|
|--|--|--|
APT|Apartment|Yes|Yes
BERTH|Berth on a dock|No|Yes
BSMT|Basement|No
BLDG|Building|No
BUS-ENTRANCE|BUS Entrance|No
BUS-EXIT|BUS Exit only|No
BUS-PARKING-ENTRANCE|Bus parking entrance|No
BUS-PARKING-EXIT|Bus parking exit|No
CONDO|Condominium unit within a building|No
DELIVERY-ENTRANCE| Delivery entrance|No
DELIVERY-EXIT| Delivery exit only|No
EMERGENCY-ENTRANCE|Emergency responder entrance|No
EMERGENCY-EXIT|Emergency exit (sounds alarm)|No
ENTRANCE|Entrance|No
EXIT|Exit only|No
FLR|Floor|No
GATE|Gate|No
HOUSE|House within a complex|No
LOBBY|Lobby|No
LWR |Lower floor of building|No
MEZZ|Mezzanine|No
PAD|RV or mobile home Pad|No
PARKING-ENTRANCE|Parking entrance|No
PARKING-EXIT|Parking exit only|No
PH|Penthouse|Yes
PLATFORM|Platform|No
REAR|Rear of building|No
RM|Room|No
SERVICE-ENTRANCE|Service entrance|No
SERVICE-EXIT|Service exit only|No
SIDE|Side of building|No
SITE|Site|No
SUITE|Suite|Yes
TERMINAL|Terminal|No
TH|Townhouse|No
TRUCK-ENTRANCE|TRUCK entrance|No
TRUCK-EXIT|TRUCK exit only|No
TRUCK-PARKING-ENTRANCE|Truck parking entrance|No
TRUCK-PARKING-EXIT|Truck parking exit only|No
UNIT|Unit|Yes
UPPR|Upper floor of building|No
