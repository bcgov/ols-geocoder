# Physical Address eXchange (PAX) Standard Draft v0.14
### Change history

v0.14 June 21, 2021 - added changeDate; moved yourId to middle of list of schema fields since it is never populated manually<br>

v0.13 June 19, 2021 - added streetNamePhonetic and localityNamePhonetic; updated all examples to reflect recent schema changes; added examples 1a-1d<br>

v0.12 June 18, 2021 - added dataOwner, notes, accessPointLatLon, latitude, and longitude fields; removed isAlias because unofficial addresses are out of scope of this standard<br>

v0.11 June 17, 2021 - changed fields in all examples to camel-case; deleted localityDescriptor; replaced isOfficialAddress with isAlias so that default could be false which simplifies input; renamed isNonCivicAddress isNonCivic; renamed superSiteYourId superYourId; deleted unitNumberPrefix since a prefix is allowed by Canada Post in a unitNumber<br>

v0.10 June 16, 2021 - made special entrance/exit unitDesignators more readable<br>

v0.9 May 3, 2021 - updated support for entrances and exits as units within a building; added concept of special access points as units<br>

v0.8 Feb 25, 2021 - added support for entrances and exits as units within a building; sites no longer have to be named to have special entrances<br>

v0.7 Feb 20, 2021 - removed SITE_POINT_DESCRIPTOR as it is rarely provided by address authorities, added TOC<br>

v0.6 Jul 28, 2020 - improved Example 6 (Special entrances)<br> 

v0.5 Jun 15, 2020 - eliminated option to use ExtraPoints to define an unlimited number of additional coordinate locations for a given site. Extra site and access points can be added to a given site using subsites; added uses cases to handle emergency entrances, etc. as subsites, each with its own sitePoint and accessPoint.<br>

v0.4 Jun, 2019 - added SUPER_SITE_YOURID to schema, changed schema to allow any number of extra points, improved examples, added example of single apartment building, improved readability, fixed formatting of schema definition table.

[Introduction](#intro)<br>
[Example 1 - A house with a single civic number and no units](#ex1)<br>
[Example 1a - A house on a named highway](#ex1a)<br>
[Example 1b - A house on a numbered highway](#ex1b)<br>
[Example 1c - A house on a street with a streetDirection following streetName](#ex1c)<br>
[Example 1d - A house on a street with a streetDirection preceding streetName](#ex1d)<br>
[Example 2 - A single apartment building with multiple floors and units](#ex2)<br>
[Example 3 - An apartment complex with buildings distinguished by unit number prefix](#ex3)<br>
[Example 4 - A complex with multiple levels of units](#ex4)<br>
[Example 5 - A complex of buildings](#ex5)<br>
[Example 6 - A building with special entrances and exits](#ex6)<br>
[Unit Designators of entrances, exits, and other building parts](#specialUnitDesignators)<br>
[Schema definition (aka Data dictionary)](#schema)<br>
[Unit designators](#unitDesignators)<br>
[Street qualifiers](#streetQualifiers)<br><br>


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
35 Olympia Ave, Victoria, BC

Field | Value
-----: | ------
siteLatLon|48.40995,-123.37032
civicNumber|35
streetName|Olympia
streetType|Ave
localityName|Victoria
provinceCode|BC
accessPointLatLon|48.40987,-123.37043
footprintDescriptor|building
footprint| (aPolygon)

<a name=ex1a> </a>
## Example 1a - A house on a named highway
47209 Hart Hwy, McLeod Lake, BC

Field | Value
-----: | ------
siteLatLon|54.98534,-123.03376
civicNumber|47209
streetName|Hart
streetType|Hwy
localityName|Saanich
provinceCode|BC
accessPointLatLon|54.98535,-123.03323
footprintDescriptor|building
footprint| (aPolygon)

<a name=ex1b> </a>
## Example 1b - A house on a numbered highway
6678 Hwy 97 N, Baldonnel, BC

Field | Value
-----: | ------
siteLatLon|56.20911,-120.71146
civicNumber|6678
streetName|97
streetType|Hwy
isStreetTypePrefix|true
streetDirection|N
localityName|Baldonnel
provinceCode|BC
accessPointLatLon|56.20947,-120.71045
footprintDescriptor|building
footprint| (aPolygon)

<a name=ex1c> </a>
## Example 1c - A house on a street with a streetDirection following streetName
149 Maddock Ave W, Saanich, BC

Field | Value
-----: | ------
siteLatLon|48.44930,-123.39098
civicNumber|149
streetName|Maddock
streetType|Ave
streetDirection|W
localityName|Saanich
provinceCode|BC
accessPointLatLon|48.44940,-123.39094
footprintDescriptor|building
footprint| (aPolygon)

<a name=ex1d> </a>
## Example 1d - A house on a street with a streetDirection preceding streetName
3290 SW Marine Dr, Vancouver, BC

Field | Value
-----: | ------
siteLatLon|49.22985,-123.17838
civicNumber|3290
streetName|Marine
streetType|Hwy
streetDirection|SW
isStreetDirectionPrefix|true
localityName|Vancouver
provinceCode|BC
accessPointLatLon|49.22985,-123.17813
footprintDescriptor|building
footprint| (aPolygon)

<a name=ex2> </a>
## Example 2 - A single apartment building with multiple floors and units
200 Gorge Rd W, Saanich, BC has three floors with 4 units each numbered, 101-104, 201-204, and 301-304. Site and access points of building, not units, are known.

The following exchange data records will represent the above addresses:

Field | Value
-----: | ------
siteLatLon|48.44741,-123.39670
civicNumber|740
streetName|Gorge
streetType|Rd
streetDirection|W
localityName|Saanich
provinceCode|BC
accessPointLatLon|48.44727,-123.39594
footprintDescriptor|building
footprint|(aMultiPolygon)

Field | Value
-----: | ------
unitDesignator|APT
unitNumber|101-104,201-204,301-304
civicNumber|740
streetName|Gorge
streetType|Rd
streetDirection|W
localityName|Saanich
provinceCode|BC

If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

Unit 101, 200 Gorge Rd W, Saanich, BC

Unit 102, 200 Gorge Rd W, Saanich, BC

Unit 103, 200 Gorge Rd W, Saanich, BC

Unit 104, 200 Gorge Rd W, Saanich, BC

Unit 201, 200 Gorge Rd W, Saanich, BC

Unit 202, 200 Gorge Rd W, Saanich, BC

Unit 203, 200 Gorge Rd W, Saanich, BC

Unit 204, 200 Gorge Rd W, Saanich, BC

Unit 301, 200 Gorge Rd W, Saanich, BC

Unit 302, 200 Gorge Rd W, Saanich, BC

Unit 303, 200 Gorge Rd W, Saanich, BC

Unit 304, 200 Gorge Rd W, Saanich, BC

All addresses will be assigned the site and accessPoint locations assigned to 200 Gorge Rd W, Saanich, BC

<a name=ex3></a>
## Example 3 - An apartment complex with buildings distinguished by unit number prefix

Here is a common situation where a complex has building names that are single letters embedded in a unit number as in APT A105.

810 Esquimalt Rd, Esquimalt, BC has two buildings: A and B. Each building has four floors with 10 units each numbered 100-110, 200-210, 300-310, and 400-410. Each building has its own site location and footprint but share a single access point. Unit locations are assigned the site and accessPoints of their respective buildings. 

The following exchange data records will represent the above addresses:

Field | Value | Comment
-----: | ------ | -----
siteLatLon|(aReal),(aReal)
civicNumber|810
streetName|Esquimalt
streetType|Rd
localityName|Esquimalt
provinceCode|BC
accessPointLatLon|(aReal),(aReal)
footprintDescriptor|building
footprint|(aMultiPolygon) | shape of complex


Field | Value | Comment
-----: | ------ | -----
unitDesignator| APT
unitNumber|A100-110,200-210,300-310,400-410|The letter A at the beginning of the unitNumber expression represents Building A
civicNumber|810
streetName|Esquimalt
streetType|Rd
localityName|Esquimalt
provinceCode|BC

Field | Value | Comment
-----: | ------ | -----
unitDesignator| APT
unitNumber|B100-110,200-210,300-310,400-410|The letter B at the beginning of the unitNumber expression represents Building B
civicNumber|810
streetName|Esquimalt
streetType|Rd
localityName|Esquimalt
provinceCode|BC 


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
siteLatLon|(aReal),(aReal)
civicNumber|3211
streetName|Grant McConnachie
streetType|Way
localityName|Richmond
provinceCode|BC
siteName|Vancouver International Airport
accessPointLatLon|(aReal),(aReal)
footprintDescriptor|complex
footprint|(aMultiPolygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
unitDesignator|Terminal
unitNumber|A
civicNumber|3211
streetName|Grant McConnachie
streetType|Way
localityName|Richmond
provinceCode|BC
superFullSiteDescriptor|Vancouver International Airport
footprintDescriptor|building
footprint|(polygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
unitDesignator|Terminal
unitNumber|B
civicNumber|3211
streetName|Grant McConnachie
streetType|Way
localityName|Richmond
provinceCode|BC
superFullSiteDescriptor|Vancouver International Airport
footprintDescriptor|building
footprint|(polygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
unitDesignator|Terminal
unitNumber|C
civicNumber|3211
streetName|Grant McConnachie
streetType|Way
localityName|Richmond
provinceCode|BC
superFullSiteDescriptor|Vancouver International Airport
footprintDescriptor|building
footprint|(polygon)


Field | Value
----:|----
siteLatLon|(aReal),(aReal)
unitDesignator|Gate
unitNumber|1-35
civicNumber|3211
streetName|Grant McConnachie
streetType|Way
localityName|Richmond
provinceCode|BC
superFullSiteDescriptor|Terminal A -- Vancouver International Airport

The gates of terminals B and C are represented similarly.

If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC

Terminal C, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC

Gate 23, Terminal A, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC 

Gate 7, Terminal B, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC 

In this example, each Terminal and Gate has its own site location and inherits access location from Vancouver International Airport. You could give each terminal and gate its own accessPoint location by populating the accessPointLatLon field for each terminal and gate.

<a name=ex5></a>
## Example 5 - A complex of buildings

Given the following addresses for UVIC:

Rooms 100-110 in the Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC

Michele Pujol Room, Student Union Building, University of Victoria -- 38800 Finnerty Rd, Saanich, BC

The following exchange data records will represent the above addresses:


Field | Value
----:|----
siteLatLon|48.44727,-123.39594
civicNumber|3800
streetName|Finnerty
streetType|Rd
localityName|Victoria
provinceCode|BC
siteName|University of Victoria
footprintDescriptor|complex
footprint|(aMultiPolygon)

Field | Value
----:|----
siteLatLon|48.46529,-123.30848 
civicNumber|3800
streetName|Finnerty
streetType|Rd
localityName|Saanich
provinceCode|BC
siteName|Student Union Building
superFullSiteDescriptor|University of Victoria
accessPointLatLon|48.46494,-123.30895
footprintDescriptor|building
footprint|(polygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
civicNumber|3800
streetName|Finnerty
streetType|Rd
localityName|Saanich
provinceCode|BC
siteName|Clearihue Building
superFullSiteDescriptor|University of Victoria
accessPointLatLon|(aReal),(aReal)
footprintDescriptor|building
footprint|(polygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
civicNumber|3800
streetName|Finnerty
streetType|Rd
localityName|Saanich
provinceCode|BC
siteName|Michele Pujol Room
superFullSiteDescriptor|Student Union Building -- University of Victoria
accessPointLatLon|(aReal),(aReal)
footprintDescriptor|indoorArea
footprint|(polygon)

Field | Value
----:|----
siteLatLon|(aReal),(aReal)
unitDesignator|Room
unitNumber|100-110
civicNumber|3800
streetName|Finnerty
streetType|Rd
localityName|Saanich
provinceCode|BC
superFullSiteDescriptor|Clearhue Building -- University of Victoria
accessPointLatLon|(aReal),(aReal)

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
siteLatLon|(aReal),(aReal)
civicNumber|1100
streetName|Chestnut
streetType|St
localityName|Vancouver
provinceCode|BC


Field | Value
----:|----
siteLatLon|(aReal),(aReal); location of emergency entrance on building
unitDesignator|EMERGENCY-ENTRANCE
civicNumber|1100
streetName|Chestnut
streetType|St
localityName|Vancouver
provinceCode|BC
accessPointLatLon|(aReal),(aReal) ;location of intersection of emergency access lane and Chestnut St


Field | Value
----:|----
siteLatLon|(aReal,(aReal); location of emergency exit 1 on building
unitDesignator|EMERGENCY-EXIT
unitNumber|1
civicNumber|1100
streetName|Chestnut
streetType|St
localityName|Vancouver
provinceCode|BC
latitude|(aReal); location of emergency exit 1 on building
longitude|(aReal)
accessPointLatLon|(aReal) ;location of intersection of access road and Chestnut St if different from primary site access point

Field | Value
----:|----
siteLatLon|(aReal,(aReal); location of emergency exit 2 on building
unitDesignator|EMERGENCY-EXIT
unitNumber|2
civicNumber|1100
streetName|Chestnut
streetType|St
localityName|Vancouver
provinceCode|BC
accessPointLatLon|(aReal) ;location of intersection of access road and Chestnut St if different from primary site access point
<br>

<a name=specialUnitDesignators></a>
## Unit Designators of entrances, exits, and other building parts
The following entrance unit designators are proposed to handle the special types of entrances and exits in a standard way. Like any unitDesignator, entrances may be numbered (eg., ENTRANCE 1, EXIT 4)

|unitDesignator|Description|
|--|--|
BUS-ENTRANCE|BUS Entrance/exit
BUS-EXIT|BUS Exit only
BUS-PARKING-ENTRANCE|Bus parking entrance/exit
BUS-PARKING-EXIT|Bus parking exit only
DELIVERY-ENTRANCE| Delivery entrance/exit
DELIVERY-EXIT| Delivery exit only
EMERGENCY-ENTRANCE|Emergency responder entrance/exit
EMERGENCY-EXIT|Emergency exit only (sounds alarm)
ENTRANCE|Entrance/exit
EXIT|Exit only
PARKING-ENTRANCE|Parking entrance/exit
PARKING-EXIT|Parking exit only
SERVICE-ENTRANCE|Service entrance/exit
SERVICE-EXIT|Service exit only
TRUCK-ENTRANCE|TRUCK entrance/exit
TRUCK-EXIT|TRUCK exit only
TRUCK-PARKING-ENTRANCE|Truck parking entrance/exit
TRUCK-PARKING-EXIT|Truck parking exit only


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
## Schema definition (aka Data Dictionary)
This schema can be used in any common text format that supports named properties including CSV, TSV, JSON, and XML. The most commonly populated fields appear at the front of the list. The Default column specifies the value that will be assigned to the field if none is supplied or the field is missing from the data file.

Field Name|Data Type|Default|Description|Required for Civic Address|Required for Non Civic Address
|---:|--|--|--|--|--|
siteLatLon|Number||Site latitude and longitude separated by a comma (e.g., 54.98457,-123.04504); ideally building coordinates represent a point on the roof directly above the front door; not required if siteLat and siteLon populated|Yes|Yes
unitDesignator|String|UNIT|unit designator (e.g., APT, UNIT); includes Canada Post unit designators plus standard entrance designators (e.g., ENTRANCE 1, EXIT); [here](#unitDesignators) is a complete list|No|No
unitNumber|String||unit number or letter or sequence of unit number/letter ranges separated by commas (e.g., 100-119, 200-219); a unit number may contain a leading alphabetic character as in A100 which can participate in unit number ranges such as A100-A119|No|No
unitNumberSuffix|String||Canada Post unit number suffix (e.g., C)|No|No
civicNumber|Number|| civic number, usually a positive integer (e.g., 1321)|Yes|No
civicNumberSuffix|String||Canada Post civic number suffix (e.g., A)|No|No
streetName|String||Street name (e.g., Dallas)|Yes|No
streetNamePhonetic|String||Phonetic spelling of streetName|No|No
streetType|String||Street type suffix( e.g., the Rd in Herd Rd) |No|No
isStreetTypePrefix|Boolean|false|true if street type appears before street name as in HWY 17; false otherwise|No|No
streetDirection|String||Canada Post street direction (the W in Burnside Ave W); one of C, E, N, NE, NW, SE, SW, or W (e.g., NW); Canada Post does not allow both prefix and suffix street directions in the same address as in: 103 N 52 St SW|No|No
isStreetDirectionPrefix|Boolean|false|true if street direction appears before street name as in SW Marine Dr; false otherwise|No|No
localityName|String||Locality name (e.g., Victoria)|Yes|Yes
localityNamePhonetic|String||Phonetic spelling of streetName|No|No
provinceCode|String||Canada Post two-character province code|Yes|Yes
dataOwner|String||Name of address authority (e.g., MLIB)|Yes|Yes
changeDate|String|| Date this address data was last changed by the dataOwner; in format YYYYDDMM|Yes|Yes
notes|String||Additional info about address (e.g., at top of hill, narrow driveway, enter in back of house, grey house, siteLatLon is rooftop above front door)|No|no|
latitude|Number||Site latitude (e.g., 54.98457); not required if siteLatLon populated|Yes|Yes
longitude|Number||Site longitude (e.g.,-123.04504); not required if siteLatLon populated|Yes|Yes
siteName|String||building or landmark name (e.g., Centennial Candle)|no|yes
isNonCivic|Boolean|false|true if address has a sitename and no assigned civic number; false otherwise|Yes|Yes
siteTags|String|| Comma-separated list of descriptive tags (e.g. stadium)|No|No
superFullSiteDescriptor|String||names of all units and sites in parent site hierarchy separated by double-dash (e.g., Student Union Building -- University of Victoria)|No|No
yourId|String||Unique identifier in your local address management system (e.g., X0233212)|No|No
superYourId|String||Unique identifier of super site|No|No
accessPointLatLon|String||Lat and lon of accessPoint separated by a comma; Ideally point represents a point on a driveway or walkway just before the sidewalk or roadway; Only needed if access point is different than site point or super site point; also not needed if accessPointLat and accessPointLon populated|No|Yes
accessPointLat|Number||Only needed if access point is different than site point or super site point; also not needed if accessPointLatLon populated|No|Yes
accessPointLon|Number||Only needed if access point is different than site point or super site point; also not needed if accessPointLatLon populated|No|Yes
streetQualifier|String||The qualifier of a street as assigned by a municipality (e.g., the Bridge in Johnson St Bridge)|No|No
footprintDescriptor|String|| one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea (e.g., inner courtyard, football field associated with a stadium)|No|No
footprint|OGC Well Known Text||geometry of site footprint in OGC Well-Known Text format for CSV files, other geometry standards for other formats (e.g., GML GeoJson)|No|No

<a name=unitDesignators></a>
## Unit Designators
Here is the list of standard unit designators. Any designator can also have an associated unit number.

|Name|Description|Canada Post Standard|
|--|--|--|
APT|Apartment|Yes|Yes
BERTH|Berth on a dock|No|Yes
BSMT|Basement|No
BLDG|Building|No
BUS-ENTRANCE|BUS entrance/exit|No
BUS-EXIT|BUS Exit only|No
BUS-PARKING-ENTRANCE|Bus parking entrance/exit|No
BUS-PARKING-EXIT|Bus parking exit|No
CONDO|Condominium unit within a building|No
DELIVERY-ENTRANCE| Delivery entrance/exit|No
DELIVERY-EXIT| Delivery exit only|No
EMERGENCY-ENTRANCE|Emergency responder entrance/exit|No
EMERGENCY-EXIT|Emergency exit (sounds alarm)|No
ENTRANCE|Entrance/exit|No
EXIT|Exit only|No
FLR|Floor|No
GATE|Gate|No
HOUSE|House within a complex|No
LOBBY|Lobby|No
LWR |Lower floor of building|No
MEZZ|Mezzanine|No
PAD|RV or mobile home Pad|No
PARKING-ENTRANCE|Parking entrance/exit|No
PARKING-EXIT|Parking exit only|No
PH|Penthouse|Yes
PLATFORM|Platform|No
REAR|Rear of building|No
RM|Room|No
SERVICE-ENTRANCE|Service entrance/exit|No
SERVICE-EXIT|Service exit only|No
SIDE|Side of building|No
SITE|Site|No
SUITE|Suite|Yes
TERMINAL|Terminal|No
TH|Townhouse|No
TRUCK-ENTRANCE|TRUCK entrance/exit|No
TRUCK-EXIT|TRUCK exit only|No
TRUCK-PARKING-ENTRANCE|Truck parking entrance/exit|No
TRUCK-PARKING-EXIT|Truck parking exit only|No
UNIT|Unit|Yes
UPPR|Upper floor of building|No
<br><br>
<a name=streetQualifiers></a>
## Street Qualifiers
Here is the list of standard street qualifiers

|Name|Description|
|--|--|
Airport|
Airstrip|
Arm|
Bay|
Beach|
Boardwalk|
Boatlaunch|
Brakecheck|
Bridge|
Campground|
Causeway|
Chainoff|
Chainup|
Channel|
Conn|Connector
Creek|
Dam|
Day Use Area|
Diversion|
Extension|
Falls|
Ferry|
Flyover|
Frtg|Frontage
Hill|
Hospital|
Island|
Lake|
Marina|
MHP|
Offramp|
Onramp|
Overhead|
Overpass|
Parkinglot|
Passage|
Picnicarea|
Pk|Park
Point|
Pullout|
Ramp|
Recsite|
Restarea|
River|
School|
Seabus|
Shoreline|
Snowshed|
Station|
Stub|
Terminal|
TrailerCrt|
Trailhead|
Trestle|
Tunnel|
Underpass|
Viaduct|
WeighScale
