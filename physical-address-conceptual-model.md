# Conceptual Model of Physical Address

Topics: fullAddress as business unique identifier, what is a physical address, difference from mailing address, uses, address fabric designs, addresses in the real world, adding a sense of history, how is site location defined, 

### What is a physical address?
A physical address is a compound name assigned to a geographic feature connected to a road network. We call such a geographic feature a site. Examples of site include house, office or apartment building, university campus, mobile home park, campground, place of worship, and industrial plant.

A physical address can take the form of a civic address, a non-civic address, or an intersection address.

A civic address includes a civic number assigned to a site assigned to a street in a locality in a subCountry in a Country. A civic address may also include the name of a site and the names of its subsites. Here are some examples of civic address:


Nature of Site | Civic Address | Notes
---: | --- | ---
Single House assigned a civic number|1211 16 St NE, Salmon Arm, BC, Canada| 1211 is the civic number assigned to a particular house on a street called 16 St NE in the City of Salmon Arm in the Province of British Columbia.
Unit within a building|Unit 401 -- 1225 Douglas St, Victoria, BC, Canada| Unit 401 is a subsite of an office building assigned a civic number of 1225 on a street called Douglas St.
Special entrance of a industrial complex|Entrance, Delta Port Terminal -- 2 Roberts Bank Rd, Delta, BC, Canada|Entrance is a subsite Delta Port Terminal
Sub-building within a building|Emergency Ward, Royal Jubilee Hospital -- 1952 Bay St, Victoria, BC, Canada|Emergency Ward is a subsite of Royal Jubilee Hospital
Building within a complex|Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, Canada|Clearihue Building is a subsite of University of Victoria
Room within a building within a complex|Room 100, Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, Canada| Room 100 is a subsite of Clearihue Building which is a subsite of University of Victoria
Unit within a building within a complex|Gate 23, Terminal A, Vancouver International Airport -- 3211 Grant McConnachie Way,Richmond,BC, Canada|Gate 23 is a subsite of Terminal A which is a subsite of Vancouver International Airport
Unit within a building within a complex|Unit A301 -- 810 Esquimalt Rd, Esquimalt, BC, Canada| Unit A301 is a subsite of building "A" which is a subsite of the complex at 810 Esquimalt Rd

A non-civic address is the address of a site that hasn't been assigned a civic number and includes a site-name assigned to a site assigned to a street in a locality in a subCountry in a country. Here's an example:

      Centennial Candle -- Laurel Lane, Victoria, BC, Canada
      
Non-civic addresses usually designate landmarks, city infrastructure, or buildings in rural areas

An intersection address is a sequence of the names of all roads that meet at a single intersection in a locality in a subCountry. For example:

       Dallas Rd and Government St, Victoria, BC, Canada
       Douglas St and Gorge Rd and Hillside Ave and Government St, Victoria, BC, Canada

From here on in, all examples in this document will assume a country of Canada without showing it.

## Site Address Elements
The following table defines each element of a site address.

Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
fullAddress|String|Full address in [Single-Line Address Format](https://github.com/bcgov/ols-geocoder/edit/gh-pages/physical-address-conceptual-model.md)|Y|Y
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
subCountryCode|String|ISO 3166-2-CA sub-country code (e.g., BC, YT)|Yes|Yes
isOfficial|Boolean|True if address is designated as official by the appropriate address authority; False if unofficial (e.g., former address)|Yes|Yes
isNonCivic|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes	
