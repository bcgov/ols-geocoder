---
title: BC Physical Address Exchange Schema
description: Defines a schema for the exchange of reference addresses between address authorities and geocoding service providers.
---

The Physical Address eXchange (PAX) Schema defines a standard format for exchanging physical addresses between address authorities, emergency response agencies, and geocoder service providers.

## Overview

The PAX Schema supports the exchange of:

- Addresses of buildings without units
- Addresses of buildings assigned single or multiple civic numbers
- Addresses that have no civic number (landmark or non-civic addresses)
- Addresses of buildings with multiple units
- Addresses of buildings with special entrances (e.g., entrance pavilion, emergency access)
- Addresses of complexes that contain multiple buildings
- Addresses of buildings that contain sub-buildings (e.g., floors, wards, wings)

An address can have site and vehicle access locations. Units within buildings and buildings within complexes can have their own rooftop and vehicle access locations (e.g., townhouse units within a complex, buildings within a campus).

Buildings, complexes, outdoor areas, and any other site that can be assigned an address may also have its own footprint.

## Change History

| Version | Date | Changes |
|---------|------|---------|
| v0.17 | Nov 2, 2021 | Added locationDescriptor at the request of GeoBC |
| v0.16 | July 9, 2021 | Added street schema; still need to add examples describing its use |
| v0.15 | July 3, 2021 | Generalized provinceCode to subCountryCode to comply with ISO 3166-2 standard |
| v0.14 | June 21, 2021 | Added changeDate; moved yourId to middle of list of schema fields |
| v0.13 | June 19, 2021 | Added streetNamePhonetic and localityNamePhonetic |
| v0.12 | June 18, 2021 | Added dataOwner, notes, accessPointLatLon, latitude, and longitude fields |
| v0.11 | June 17, 2021 | Changed fields to camel-case; replaced isOfficialAddress with isAlias |
| v0.10 | June 16, 2021 | Made special entrance/exit unitDesignators more readable |
| v0.9 | May 3, 2021 | Updated support for entrances and exits as units within a building |
| v0.8 | Feb 25, 2021 | Added support for entrances and exits as units within a building |
| v0.7 | Feb 20, 2021 | Removed SITE_POINT_DESCRIPTOR as it is rarely provided |
| v0.6 | Jul 28, 2020 | Improved Example 6 (Special entrances) |
| v0.5 | Jun 15, 2020 | Eliminated option to use ExtraPoints; added uses cases to handle emergency entrances |
| v0.4 | Jun, 2019 | Added SUPER_SITE_YOURID to schema; improved examples |

## Examples

### Example 1 - A house with a single civic number and no units

**35 Olympia Ave, Victoria, BC**

| Field | Value |
|-------|-------|
| siteLatLon | 48.40995,-123.37032 |
| civicNumber | 35 |
| streetName | Olympia |
| streetType | Ave |
| localityName | Victoria |
| accessPointLatLon | 48.40987,-123.37043 |
| footprintDescriptor | building |
| footprint | (aPolygon) |

### Example 1a - A house on a named highway

**47209 Hart Hwy, McLeod Lake, BC**

| Field | Value |
|-------|-------|
| siteLatLon | 54.98534,-123.03376 |
| civicNumber | 47209 |
| streetName | Hart |
| streetType | Hwy |
| localityName | Saanich |
| subCountryCode | BC |
| accessPointLatLon | 54.98535,-123.03323 |
| footprintDescriptor | building |
| footprint | (aPolygon) |

### Example 1b - A house on a numbered highway

**6678 Hwy 97 N, Baldonnel, BC**

| Field | Value |
|-------|-------|
| siteLatLon | 56.20911,-120.71146 |
| civicNumber | 6678 |
| streetName | 97 |
| streetType | Hwy |
| isStreetTypePrefix | true |
| streetDirection | N |
| localityName | Baldonnel |
| subCountryCode | BC |
| accessPointLatLon | 56.20947,-120.71045 |
| footprintDescriptor | building |
| footprint | (aPolygon) |

### Example 1c - A house on a street with a streetDirection following streetName

**149 Maddock Ave W, Saanich, BC**

| Field | Value |
|-------|-------|
| siteLatLon | 48.44930,-123.39098 |
| civicNumber | 149 |
| streetName | Maddock |
| streetType | Ave |
| streetDirection | W |
| localityName | Saanich |
| subCountryCode | BC |
| accessPointLatLon | 48.44940,-123.39094 |
| footprintDescriptor | building |
| footprint | (aPolygon) |

### Example 1d - A house on a street with a streetDirection preceding streetName

**3290 SW Marine Dr, Vancouver, BC**

| Field | Value |
|-------|-------|
| siteLatLon | 49.22985,-123.17838 |
| civicNumber | 3290 |
| streetName | Marine |
| streetType | Dr |
| streetDirection | SW |
| isStreetDirectionPrefix | true |
| localityName | Vancouver |
| subCountryCode | BC |
| accessPointLatLon | 49.22985,-123.17813 |
| footprintDescriptor | building |
| footprint | (aPolygon) |

### Example 2 - A single apartment building with multiple floors and units

**200 Gorge Rd W, Saanich, BC** has three floors with 4 units each numbered, 101-104, 101A, 201-204, 301-304. Site and access points of building, not units, are known.

The following exchange data records will represent the above addresses:

| Field | Value |
|-------|-------|
| siteLatLon | 48.44741,-123.39670 |
| civicNumber | 200 |
| streetName | Gorge |
| streetType | Rd |
| streetDirection | W |
| localityName | Saanich |
| subCountryCode | BC |
| accessPointLatLon | 48.44727,-123.39594 |
| footprintDescriptor | building |
| footprint | (aMultiPolygon) |

| Field | Value |
|-------|-------|
| unitDesignator | APT |
| unitNumber | 101-104,201-204,301-304,101A |
| civicNumber | 200 |
| streetName | Gorge |
| streetType | Rd |
| streetDirection | W |
| localityName | Saanich |
| subCountryCode | BC |

If this example was provided as reference data to the BC Address Geocoder, the Geocoder would derive full addresses such as:

- Unit 101, 200 Gorge Rd W, Saanich, BC
- Unit 101A, 200 Gorge Rd W, Saanich, BC
- Unit 102, 200 Gorge Rd W, Saanich, BC
- Unit 103, 200 Gorge Rd W, Saanich, BC
- Unit 104, 200 Gorge Rd W, Saanich, BC
- Unit 201, 200 Gorge Rd W, Saanich, BC
- Unit 202, 200 Gorge Rd W, Saanich, BC
- Unit 203, 200 Gorge Rd W, Saanich, BC
- Unit 204, 200 Gorge Rd W, Saanich, BC
- Unit 301, 200 Gorge Rd W, Saanich, BC
- Unit 302, 200 Gorge Rd W, Saanich, BC
- Unit 303, 200 Gorge Rd W, Saanich, BC
- Unit 304, 200 Gorge Rd W, Saanich, BC

### Example 3 - An apartment complex with two buildings named A and B

**810 Esquimalt Rd, Esquimalt, BC** has two buildings: A and B. Each building has four floors with 10 units each numbered 100-110, 200-210, 300-310, and 400-410. Each building has its own site location and footprint but share a single access point.

| Field | Value | Comment |
|-------|-------|---------|
| siteLatLon | (aReal),(aReal) | |
| civicNumber | 810 | |
| streetName | Esquimalt | |
| streetType | Rd | |
| localityName | Esquimalt | |
| subCountryCode | BC | |
| accessPointLatLon | (aReal),(aReal) | |
| footprintDescriptor | building | |
| footprint | (aMultiPolygon) | shape of complex |

| Field | Value | Comment |
|-------|-------|---------|
| unitDesignator | APT | |
| unitNumber | A100-110,A200-210,A300-310,A400-410 | The letter A at the beginning of each unitNumber range represents Building A |
| civicNumber | 810 | |
| streetName | Esquimalt | |
| streetType | Rd | |
| localityName | Esquimalt | |
| subCountryCode | BC | |

| Field | Value | Comment |
|-------|-------|---------|
| unitDesignator | APT | |
| unitNumber | B100-110,B200-210,B300-310,B400-410 | The letter B at the beginning of each unitNumber range represents Building B |
| civicNumber | 810 | |
| streetName | Esquimalt | |
| streetType | Rd | |
| localityName | Esquimalt | |
| subCountryCode | BC | |

### Example 4 - A complex with multiple levels of units

**Vancouver International Airport, 3211 Grant McConachie Way, Richmond, BC** has the following terminals and gates:

- Terminal A: Gate 1-35
- Terminal B: Gate 1-40
- Terminal C: Gate 1-20

### Example 5 - A complex of buildings

Given the following addresses for UVIC:

- Rooms 100-110 in the Clearihue Building, University of Victoria – 3800 Finnerty Rd, Saanich BC
- Michele Pujol Room, Student Union Building, University of Victoria – 38800 Finnerty Rd, Saanich, BC

### Example 6 - A building with special entrances and exits

Buildings may have special entrances for emergency access or service staff, and special exits for emergency egress. These are represented as units (subsites) within the building.

For example, assume the HR MacMillan Space Centre in Vancouver has two numbered emergency exits and an emergency responder entrance with an adjacent emergency access lane that runs to Chestnut St:

- 1100 Chestnut St, Vancouver, BC
- EMERGENCY-ENTRANCE – 1100 Chestnut St, Vancouver, BC
- EMERGENCY-EXIT 1 – 1100 Chestnut St, Vancouver BC
- EMERGENCY-EXIT 2 – 1100 Chestnut St, Vancouver BC

## Unit Designators of entrances, exits, and other building parts

The following entrance unit designators are proposed to handle the special types of entrances and exits in a standard way. Like any unitDesignator, entrances may be numbered (e.g., ENTRANCE 1, EXIT 4).

| Unit Designator | Description |
|-----------------|-------------|
| BUS-ENTRANCE | BUS Entrance/exit |
| BUS-EXIT | BUS Exit only |
| BUS-PARKING-ENTRANCE | Bus parking entrance/exit |
| BUS-PARKING-EXIT | Bus parking exit only |
| DELIVERY-ENTRANCE | Delivery entrance/exit |
| DELIVERY-EXIT | Delivery exit only |
| EMERGENCY-ENTRANCE | Emergency responder entrance/exit |
| EMERGENCY-EXIT | Emergency exit only (sounds alarm) |
| ENTRANCE | Entrance/exit |
| EXIT | Exit only |
| PARKING-ENTRANCE | Parking entrance/exit |
| PARKING-EXIT | Parking exit only |
| SERVICE-ENTRANCE | Service entrance/exit |
| SERVICE-EXIT | Service exit only |
| TRUCK-ENTRANCE | TRUCK entrance/exit |
| TRUCK-EXIT | TRUCK exit only |
| TRUCK-PARKING-ENTRANCE | Truck parking entrance/exit |
| TRUCK-PARKING-EXIT | Truck parking exit only |

The following Canada Post Unit Designators define other building parts:

| Unit Designator | Description |
|-----------------|-------------|
| LOBBY | Lobby |
| MEZZ | Mezzanine |
| UPPR | Upper floor |
| LWR | Lower floor |
| REAR | Rear of building |
| BSMNT | Basement |
| FLR | Floor |

## Data Dictionary

### Address Schema Definition

This schema can be used in any common text format that supports named properties including CSV, TSV, JSON, and XML.

| Field Name | Data Type | Default | Description | Required (Civic) | Required (Non-Civic) |
|------------|-----------|---------|-------------|------------------|----------------------|
| siteLatLon | Number | | Site latitude and longitude separated by a comma | No | No |
| unitDesignator | String | UNIT | unit designator (e.g., APT, UNIT) | No | No |
| unitNumber | String | | unit number or letter or sequence of unit number/letter ranges | No | No |
| unitNumberSuffix | String | | Canada Post unit number suffix (e.g., C) | No | No |
| civicNumber | Number | | civic number, usually a positive integer | Yes | No |
| civicNumberSuffix | String | | Canada Post civic number suffix (e.g., A) | No | No |
| streetName | String | | Street name (e.g., Dallas) | Yes | No |
| streetType | String | | Street type suffix (e.g., the Rd in Herd Rd) | No | No |
| isStreetTypePrefix | Boolean | false | true if street type appears before street name | No | No |
| streetDirection | String | | Canada Post street direction (e.g., NW) | No | No |
| isStreetDirectionPrefix | Boolean | false | true if street direction appears before street name | No | No |
| localityName | String | | Locality name (e.g., Victoria) | Yes | Yes |
| subCountryCode | String | | ISO 3166-2 two-character subCountry code (e.g., BC) | Yes | Yes |
| dataOwner | String | | Name of address authority (e.g., MLIB) | Yes | Yes |
| changeDate | String | | Date this address data was last changed (YYYYDDMM) | Yes | Yes |
| notes | String | | Additional info about address | No | No |
| latitude | Number | | Site latitude | No | No |
| longitude | Number | | Site longitude | No | No |
| siteName | String | | building or landmark name | No | Yes |
| isNonCivic | Boolean | false | true if address has a sitename and no assigned civic number | Yes | Yes |
| siteTags | String | | Comma-separated list of descriptive tags | No | No |
| superFullSiteDescriptor | String | | names of all units and sites in parent site hierarchy | No | No |
| yourId | String | | Unique identifier in your local address management system | No | No |
| superYourId | String | | Unique identifier of super site | No | No |
| accessPointLatLon | String | | Lat and lon of accessPoint separated by a comma | No | Yes |
| accessPointLat | Number | | Only needed if access point is different than site point | No | No |
| accessPointLon | Number | | Only needed if access point is different than site point | No | No |
| streetQualifier | String | | The qualifier of a street (e.g., the Bridge in Johnson St Bridge) | No | No |
| footprintDescriptor | String | | one of building, complex, parcel, outdoorArea, indoorArea, secureOutdoorArea | No | No |
| footprint | String | | Geometry in OGC Well Known Text format | No | No |
| locationDescriptor | String | parcelPoint | Describes the nature of the siteLatLon location | No | No |

### Unit Designators

| Name | Description | Canada Post Standard |
|------|-------------|---------------------|
| APT | Apartment | Yes |
| BERTH | Berth on a dock | No |
| BSMT | Basement | No |
| BLDG | Building | No |
| BUS-ENTRANCE | BUS entrance/exit | No |
| BUS-EXIT | BUS Exit only | No |
| BUS-PARKING-ENTRANCE | Bus parking entrance/exit | No |
| BUS-PARKING-EXIT | Bus parking exit | No |
| CONDO | Condominium unit within a building | No |
| DELIVERY-ENTRANCE | Delivery entrance/exit | No |
| DELIVERY-EXIT | Delivery exit only | No |
| EMERGENCY-ENTRANCE | Emergency responder entrance/exit | No |
| EMERGENCY-EXIT | Emergency exit (sounds alarm) | No |
| ENTRANCE | Entrance/exit | No |
| EXIT | Exit only | No |
| FLR | Floor | No |
| GATE | Gate | No |
| HOUSE | House within a complex | No |
| LOBBY | Lobby | No |
| LWR | Lower floor of building | No |
| MEZZ | Mezzanine | No |
| PAD | RV or mobile home Pad | No |
| PARKING-ENTRANCE | Parking entrance/exit | No |
| PARKING-EXIT | Parking exit only | No |
| PH | Penthouse | Yes |
| PLATFORM | Platform | No |
| REAR | Rear of building | No |
| RM | Room | No |
| SERVICE-ENTRANCE | Service entrance/exit | No |
| SERVICE-EXIT | Service exit only | No |
| SIDE | Side of building | No |
| SITE | Site | No |
| SUITE | Suite | Yes |
| TERMINAL | Terminal | No |
| TH | Townhouse | No |
| TRUCK-ENTRANCE | TRUCK entrance/exit | No |
| TRUCK-EXIT | TRUCK exit only | No |
| TRUCK-PARKING-ENTRANCE | Truck parking entrance/exit | No |
| TRUCK-PARKING-EXIT | Truck parking exit only | No |
| UNIT | Unit | Yes |
| UPPR | Upper floor of building | No |

### Street Qualifiers

| Name | Description |
|------|-------------|
| Airport | |
| Airstrip | |
| Arm | |
| Bay | |
| Beach | |
| Boardwalk | |
| Boatlaunch | |
| Brakecheck | |
| Bridge | |
| Campground | |
| Causeway | |
| Chainoff | |
| Chainup | |
| Channel | |
| Conn | |
| Connector Creek | |
| Dam | |
| Day Use Area | |
| Diversion | |
| Extension | |
| Falls | |
| Ferry | |
| Flyover | |
| Frtg | |
| Frontage | |
| Hill | |
| Hospital | |
| Island | |
| Lake | |
| Marina | |
| MHP | |
| Offramp | |
| Onramp | |
| Overhead | |
| Overpass | |
| Parkinglot | |
| Passage | |
| Picnicarea | |
| Pk | |
| Park | |
| Point | |
| Pullout | |
| Ramp | |
| Recsite | |
| Restarea | |
| River | |
| School | |
| Seabus | |
| Shoreline | |
| Snowshed | |
| Station | |
| Stub | |
| Terminal | |
| TrailerCrt | |
| Trailhead | |
| Trestle | |
| Tunnel | |
| Underpass | |
| Viaduct | |
| WeighScale | |

## Street Schema Definition

The Street Schema defines the fields of a single street within a locality. It is used to QA the street fields in both civic and non-civic addresses. The collection of all street definitions forms a Street Index.

| Field Name | Data Type | Required | Default | Description |
|------------|-----------|----------|---------|-------------|
| streetName | String | Yes | | Official spelling of street name |
| streetNamePhonetic | String | No | | Phonetic spelling of streetName |
| streetNameSpoken | URL | No | | A link to a .WAV file containing the streetName spoken |
| streetType | String | No | | Street type suffix |
| isStreetTypePrefix | Boolean | No | false | true if street type appears before street name |
| streetDirection | String | No | | Canada Post street direction |
| isStreetDirectionPrefix | Boolean | No | false | true if street direction appears before street name |
| streetQualifier | String | No | | The qualifier of a street |
| localityName | String | Yes | | Locality name |
| subCountryCode | String | Yes | | ISO 3166-2 two-character subCountry code |
| dataOwner | String | Yes | | Name of address authority |
| changeDate | String | Yes | | Date this address data was last changed (YYYYDDMM) |
| notes | String | No | | Additional info about the street |
