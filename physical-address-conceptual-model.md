# Conceptual Model of Physical Addressing

Topics: fullAddress as business unique identifier,  address fabric designs (Hwy 1 is the spine for Shuswap St N/S; together they form the quadrant boundaries in Salmon Arm), addresses in the real world, adding a sense of history, how is site location defined, how are sites and parcels related, how are sites related spatially to the earth,

## What is a physical address?
A physical address is the compound name of a geographic feature connected to a road network. We call such a geographic feature a site. Examples of site include house, office or apartment building, university campus, mobile home park, campground, place of worship, and industrial plant. A physical address is not a geographic feature; it is the name of one.

The primary purpose of a physical address is to make it is easy for people and digital devices to find the location of a particular site on the earth or on a map of the earth. To achieve this purpose, physical addresses depend on accurately spelled and visible road-name signs on roads and civic-number plaques on sites. For example, its hard to find a particular building if there is no street sign. Another example is a house on a corner lot that has a plaque of the correct civic number mounted on an exterior wall facing the wrong street. This has caused more than one emergency response vehicle needless delay.  Other examples include [mispelled names on street signs or wrong site names on site signs](https://www.summerlandreview.com/news/spellings-inconsistent-on-summerland-street-signs/).


Efficient physical addressing also depends on a well-designed address fabric. An address fabric is the set of patterns applied to the layout and naming of civic streets. There are three common patterns for street layout: grid, star, and tree. In the grid layout, streets follow evenly-spaced, straight lines and intersect at right-angles. In the star layout, streets form a spider web of radiating lines intersected by evenly spaced cross-streets. In the tree layout, many smaller streets converge to a single trunk the connects to a main artery. This is the dominant pattern of urban suburbs. A single urban area typically has multiple layout patterns reflecting different phases of its growth.

In a grid layout, streets may be numbered instead of named. Numbered streets allow you to estimate the number of blocks you might be from a given civic address. Parts of Vancouver are laid out in a numbered grid like [here](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=49.26301,-123.10500). This area also contains a pair street layout with Main St acting as the spine that cuts all cross-streets into E and W pairs (e.g., E 7th Ave, W 7th Ave).

A grid may also be broken up into quadrants with each quadrant assigned a street directions of NE, NW, SE,or SW. The grid may also be broken up into pairs by a spine street that divides all cross-streets into either E/W or N/S street directions.[Salmon Arm](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=50.69959,-119.28523) is a good example of quadrant street layout. Shuswap St and the Trans-Canada Hwy divide Salmon Arm into four quadrants. In each quadrant, streets are generally numbered, not named. This means there are up to four street with the same name, one in each quadrant. In Salmon Arm, there are four 10th Ave's and three 1st Ave's (there is no 1st Ave NW). You might notice that Shuswap St is divided into N and S by the TCH but that doesn't make the TCH a pair spine because it doesn't divide any other streets in two.

While quadrant and pair patterns have a certain appeal to urban planners, they can cause great confusion because a single street direction error can lead you to a site miles from the correct location. For example, 

     5860 10 Ave NW, Salmon Arm, BC
     5860 10 Ave NE, Salmon Arm, BC
     
have only a single character difference between them but they are over twelve kilometers away from each other.


A physical address can take the form of a site address or an intersection address. A site address can be a civic address or a non-civic address.

## Civic Address

A civic address includes a civic number assigned by a local government to a site assigned to a street in a locality in a subCountry in a Country. A civic address may also include the name of a site and the names of its subsites. Here are some examples of civic address:


Nature of Site | Civic Address | Notes
---: | --- | ---
Single House assigned a civic number|1211 16 St NE, Salmon Arm, BC, Canada| 1211 is the civic number assigned to a particular house on a street called 16 St NE in the City of Salmon Arm in the Province of British Columbia.
Unit within a building|Unit 401 -- 1225 Douglas St, Victoria, BC, Canada| Unit 401 is a subsite of an office building assigned a civic number of 1225 on a street called Douglas St.
Special entrance of a industrial complex|Entrance, Delta Port Terminal -- 2 Roberts Bank Rd, Delta, BC, Canada|Entrance is a subsite of Delta Port Terminal
Sub-building within a building|Emergency Ward, Royal Jubilee Hospital -- 1952 Bay St, Victoria, BC, Canada|Emergency Ward is a subsite of Royal Jubilee Hospital
Building within a complex|Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, Canada|Clearihue Building is a subsite of University of Victoria
Room within a building within a complex|Room 100, Clearihue Building, University of Victoria -- 3800 Finnerty Rd, Saanich BC, Canada| Room 100 is a subsite of Clearihue Building which is a subsite of University of Victoria
Unit within a building within a complex|Gate 23, Terminal A, Vancouver International Airport -- 3211 Grant McConnachie Way, Richmond,BC, Canada|Gate 23 is a subsite of Terminal A which is a subsite of Vancouver International Airport
Unit within a building within a complex|Unit A301 -- 810 Esquimalt Rd, Esquimalt, BC, Canada| Unit A301 is a subsite of Building A which is a subsite of the complex at 810 Esquimalt Rd

## Non-civic Address

A non-civic address is the address of a site that hasn't been assigned a civic number and includes a site-name assigned to a site assigned to a street in a locality in a subCountry in a country. Non-civic addresses usually designate landmarks, city infrastructure, or buildings in rural areas. Some non-civic addresses are simply streets within a locality. Here are some examples of non-civic address:

Non-civic Address | Description
--- | ---
Centennial Candle -- Laurel Lane, Victoria, BC, Canada|named water tower on a street in a city
Hwy 97, Chasm, BC, Canada|numbered hwy as street name
Cariboo Hwy, Buckhorn, BC|named hwy as street name
Johnson St Bridge, Victoria, BC, Canada|Named bridge is named after street; Johnson St is street name, Bridge is street qualifier
Massey Dr Overpass, Prince George, BC|named overpass is named after street
Great Bear Snowshed, Coquihalla, BC|named snowshed is street name

All named bridges, overpasses, snowsheds, and tunnels are street names or street name + street qualifier as in the case of Johnson St Bridge.

## Intersection Address

An intersection address is a sequence of the names of all roads that meet at a single intersection in a locality in a subCountry. For example:

       Dallas Rd and Government St, Victoria, BC, Canada
       Douglas St and Gorge Rd and Hillside Ave and Government St, Victoria, BC, Canada

A special type of intersection address is a highway exit which is the intersection of a highway and an offramp as in the following example:

       Hwy 1 at Exit 366, Kamloops, BC, Canada


From here on in, all examples in this document will assume a country of Canada without showing it.


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
isOfficial|Boolean|True if address is designated as official by the appropriate address authority; False if unofficial (e.g., former address)|Yes|Yes
isNonCivic|Boolean|True if address has no assigned civic number; a non-civic address must have a SITE_NAME to be referenced (e.g., Lonely Cabins -- Hwy 20, Stui, BC)|Yes|Yes	


## Differences between Physical Address and Mailing Address

There are several differences between a physical address and a postal address that reflect their different purposes. First and foremost, the locality of a physical address is an incorporated municipality or unincorporated populated place. The locality of a mailing address is the postal community assigned by the mailing authority, which in BC, is Canada Post. For example, the locality of 4440 Happy Valley Rd is the postal community of Victoria in a mailing address and the Municipality of Metchosin in a physical address. Knowing that 440 Happy Valley Rd is in the postal community of Victoria helps Canada Post collect, sort, and deliver the mail but it's not very helpful when you are trying to find the Metchosin Fire Hall in your car or on a map since Victoria is over 23km away from Metchosin.

Mailing service addresses such as PO Boxes, Rural Routes, General Delivery are equally unhelpful when looking for a particular house, business, etc so are not recognized as valid elements of a physical address. For example, Main Branch, Victoria Public Library , PO BOX 320, Victoria, BC will, at best be understood as the physical address, Victoria, BC since Victoria is a known municipality in BC. However, this won't help you find the location of the main branch of the library.

Another difference is that Canada Post doesn't care if a street type or street direction is a predirectional or postdirectional in a mailing address; just that there is a streetDirection, a streetType or both. Again this is very important when trying to find a physical address so there are isStreetTypePrefix and isStreetDirectionPrefix flags included in physical address.
