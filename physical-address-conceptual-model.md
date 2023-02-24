# Conceptual Model of Physical Addressing (Draft v2.0)


## What is a physical address?
A physical address is the compound name of a geographic feature connected to a road network. We call such a geographic feature a Site. Examples of Sites include house, office or apartment building, university campus, mobile home park, campground, place of worship, and industrial plant. A physical address is not a geographic feature; it is the name of one. That name includes a site's name or civic number, the street block it is connected to, the locality that contains the block, the province or territory that contains the locality, and the country that contains the province or territory. Here is a schematic (or data model diagram) of the geographic features that contribute their names to a physical address:


SITE ----- STREET ----- LOCALITY ----- SUBCOUNTRY ----- COUNTRY
  

The above diagram says that a site is assigned to a street, a street belongs to a locality, a locality belongs to a subcountry (e.g., a province or territory), and a subcountry belongs to a country. A site assigned to a street may optionally be assigned a civic number.

A physical address makes it easy for people and digital devices to find the location of a particular site on the earth or on a map of the earth. However, it only works if the real sites and road network on the Earth have complete and accurate signage. For example, it's hard to drive to a particular building if it has no civic number plaque on it or if there is no street sign. Another example is a house on a corner lot that has a plaque of the correct civic number mounted on an exterior wall facing the wrong street. This has caused more than one emergency response vehicle needless delay.  Other examples include [mispelled names on street signs or wrong site names on site signs](https://www.summerlandreview.com/news/spellings-inconsistent-on-summerland-street-signs/).

Efficient physical addressing also depends on a well-designed address fabric. An address fabric is the set of patterns applied to the layout and naming of civic streets. There are three common patterns for street layout: grid, star, and tree. In the grid layout, streets follow evenly-spaced, straight lines and intersect at right-angles. In the star layout, streets form a spider web of radiating lines intersected by evenly spaced cross-streets. In the tree layout, many smaller streets converge to a single trunk that connects to a main artery. The grid layout is commonly found in the oldest part of urban areas. The star layout is found in urban areas that were dominated by trains in their early days. The tree layout is common in suburbs because it minimized road surface and maximizes property area; it also causes long line-ups in commuter traffic.  A single urban area typically has multiple layout patterns reflecting different phases of its growth.

In a grid layout, streets may be numbered instead of named. Numbered streets allow you to estimate the number of blocks you might be from a given civic address. Parts of Vancouver are laid out in a numbered grid like [this example](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=49.26301,-123.10500). This area also contains a pair street layout with Main St acting as the spine that cuts cross-streets 1st through 69th into E and W pairs (e.g., E 7th Ave, W 7th Ave).

A grid may also be broken up into quadrants with each quadrant assigned a street directions of NE, NW, SE,or SW.[Salmon Arm](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=50.69959,-119.28523) is a good example of quadrant street layout. Shuswap St and the Trans-Canada Hwy divide Salmon Arm into four quadrants. In each quadrant, streets are generally numbered, not named. This means there are up to four street with the same name, one in each quadrant. In Salmon Arm, there are four 10th Ave's and three 1st Ave's (there is no 1st Ave NW). You might notice that Shuswap St is divided into N and S by the TCH but that doesn't make the TCH a pair spine because it doesn't divide any other streets in two.

While quadrant and pair patterns have a certain appeal to urban planners, they can cause great confusion because a single street direction error can lead you or your delivery person to a site far from the correct location. For example, the two addresses below only have a single character difference between them but they are over twelve kilometers away from each other.

     5860 10 Ave NW, Salmon Arm, BC
     5860 10 Ave NE, Salmon Arm, BC

## Types of physical address
A physical address can take the form of a site address or an intersection address. A site address can be a civic address or a non-civic address.

### Civic Address
A civic address includes a civic number assigned by a local government to a site assigned to a street in a locality in a subCountry in a Country. A civic address may also include the name of a site and the names of its subsites. Here are some examples of civic address:


Nature of Site | Civic Address | Notes
---: | --- | ---
Single House assigned a civic number|1211 16 St NE, Salmon Arm, BC, CA| 1211 is the civic number assigned to a particular house on a street called 16 St NE in the City of Salmon Arm in the Province of British Columbia in the country of Canada.
Unit within a building|Unit 401 -- 1225 Douglas St, Victoria, BC, CA| Unit 401 is a subsite of an office building assigned a civic number of 1225 on a street called Douglas St.
Special entrance of a industrial complex|Entrance, Delta Port Terminal -- 2 Roberts Bank Rd, Delta, BC, CA|Entrance is a subsite of Delta Port Terminal -- 2 Roberts Bank Rd, Delta, BC, CA
Sub-building within a building|Emergency Ward, Royal Jubilee Hospital -- 1952 Bay St, Victoria, BC, CA|Emergency Ward is a subsite of Royal Jubilee Hospital
Building within a complex|Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, CA|Clearihue Building is a subsite of University of Victoria
Room within a building within a complex|Room 100, Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, CA| Room 100 is a subsite of Clearihue Building which is a subsite of University of Victoria
Unit within a building within a complex|Gate 23, Terminal A, Vancouver International Airport -- 3211 Grant McConnachie Way, Richmond, BC, CA|Gate 23 is a subsite of Terminal A which is a subsite of Vancouver International Airport
Unit within a building within a complex|Unit A301 -- 810 Esquimalt Rd, Esquimalt, BC, CA| Unit A301 is a subsite of Building A which is a subsite of the complex at 810 Esquimalt Rd

### Non-civic Address
A non-civic address is the address of a site that hasn't been assigned a civic number and includes a site-name assigned to a site assigned to a street in a locality in a subCountry in a country. Non-civic addresses usually designate landmarks, city infrastructure, or buildings in rural areas. Some non-civic addresses are simply streets within a locality. Here are some examples of non-civic address:

Non-civic Address | Description
--- | ---
Centennial Candle -- Laurel Lane, Victoria, BC, CA|named water tower on a street in a city
Hwy 97, Chasm, BC, CA|numbered hwy as street name
Cariboo Hwy, Buckhorn, BC, CA|named hwy as street name
Johnson St Bridge, Victoria, BC, CA|Named bridge is named after street; Johnson St is street name, Bridge is street qualifier
Massey Dr Overpass, Prince George, BC, CA|named overpass is named after street
Great Bear Snowshed, Coquihalla, BC, CA|named snowshed is street name

All named bridges, overpasses, snowsheds, and tunnels are street names or street name + street qualifier as in the case of Johnson St Bridge.

### Intersection Address
An intersection address is a sequence of the names of all roads that meet at a single intersection in a locality in a subCountry. For example:

       Dallas Rd and Government St, Victoria, BC, CA
       Douglas St and Gorge Rd and Hillside Ave and Government St, Victoria, BC, CA

A special type of intersection address is a highway exit which is the intersection of a highway and an offramp as in the following example:

       Hwy 1 at Exit 366, Kamloops, BC, CA


## Anatomy of a site address

The following table defines the elements of a site address:

Element Name | Data Type |	Description | Required for Civic Address|Required for Non-civic address
---: | --- | --- | ---| ---
fullAddress|String|Full address in [Single-Line Address Format](https://github.com/bcgov/ols-geocoder/blob/gh-pages/singleLineAddressFormat.md)|Y|Y
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
StreetQualifier|String|One of Frontage, Bridge, Tunnel, or Snowshed|No|no
locality|String|Locality name (e.g., Victoria)|Yes|Yes
subCountryCode|String|ISO 3166-2-CA sub-country code (e.g., BC, YT)|Yes|Yes
countryCode|String|ISO 3166-1-alpha-2 country code (e.g., CA)|Yes|Yes
isOfficial|Boolean|True if address is designated as official by the appropriate address authority; False if unofficial (e.g., former address)|Yes|Yes
isNonCivic|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes	


### Differences between Physical Address and Mailing Address

There are several differences between a physical address and a mailing address that reflect their different purposes. First and foremost, the locality of a physical address is an incorporated municipality or unincorporated populated place. The locality of a mailing address is the postal community assigned by Canada Post, the mailing authority for Canada. For example, the locality of 4440 Happy Valley Rd is the postal community of Victoria in a mailing address and the Municipality of Metchosin in a physical address. Knowing that 440 Happy Valley Rd is in the postal community of Victoria helps Canada Post collect, sort, and deliver the mail but it's not very helpful when you are trying to find the Metchosin Fire Hall in your car or on a map since Victoria is over 23km away from Metchosin.

Mailing service addresses such as PO Boxes, Rural Routes, General Delivery are equally unhelpful when looking for a particular house, business, etc so are not recognized as valid elements of a physical address. For example, Main Branch, Victoria Public Library , PO BOX 320, STN A, Victoria, BC will, at best, be understood as the physical address, Victoria, BC since Victoria is a known municipality in BC. However, this won't help you find the location of the main branch of the library.

Another difference is that Canada Post doesn't care if a street type or street direction is a predirectional or postdirectional in a mailing address; just that there is a streetDirection, a streetType or both. Again this is very important when trying to find a physical address so there are isStreetTypePrefix and isStreetDirectionPrefix flags included in physical address.

### How site location is defined

The two major uses of the geography of a physical address are:
- to determine the location of a site for display on a map and 
- to determine the location of a road access point to the site for use in finding a route to or from the site. 

Address authorities should choose a site location that best guides people in and out of the site and that lies within the correct parcel. For a site that is a house or building, a point on its rooftop just above an entrance or exit would be ideal. If there are no structures on the site's parcel, a location known to be within parcel (e.g., a parcel centroid) will do. An access point is the point at which the site's driveway or walkway meets the road.

A subSite like a unit within a building or a townhouse within a complex can have its own site location and access point. If not specified, a subsite inherits its locations from its superSite. For example, if the location of individual units within a building are not known, it is sufficient to define the site location and access point of the building and all units within the building will inherit these locations. In the case of service or emergency entrances to a building, these can be represented as subsites of the building with each subSite having its own name, site location, and access point. For example, an emergency entrance can be assigned the siteName "Emergency Entrance" with its site location set to the location of the entrance door. Its access point should be set to where the associated emergency lane meets the road.


#### Anatomy of Site Location
A site's location is defined by its site point and access point. If sites are nested, site point and access point values may be inherited from their superSites.

|Element Name|Data Type|Description
|--|--|--|
siteLocation|Point|(x,y) coordinate location of a site; by default, coordinates are defined in lon,lat format; other Spatial Reference Systems may be used
accessPoint|Point| (x,y) coordinate location of a site's access point; 

Coordinates are assumed to be in lon,lat format (e.g., EPGS code 4326)

### Topics to do
fullAddress as business unique identifier, addresses in the real world, adding a sense of history, how are sites and parcels related, how are sites related spatially to the earth.
