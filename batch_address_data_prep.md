---
title: Preparing your Address File for Batch Geocoding
---

# September 6, 2015

> **Document Version 2.0**
>
> The *Address List Editor* can handle a list of up to one thousand
> addresses. For larger lists, use the *Batch Geocoder*.

# Preparing You Address File for the Address List Editor

> The Address List Editor has a text box that you paste addresses into.
> Each pasted line should contain only one address, with or without
> double quotes. For best matching performance, each address should be
> in single-line address format as described in the Single-Line Address
> Format section below. The first line must be a list of comma-separated
> column names. Here is a screenshot of a list of addresses copied from
> a csv file and pasted into the address list editor:

![](./image1.png){width="6.28740157480315in"
height="3.826771653543307in"}

> All input columns are written out when you export results to a file or
> the clipboard. If your data includes a Notes column, it will be
> editable in the address list editor so you can annotate addresses as
> needed.
>
> If you are a data provider using the Address List Editor to validate
> and standardize your addresses for use as reference data by the
> geocoder, consider providing three columns: addressString,
> parcelPoint, and Notes. Here is an example file:
>
> addressString,parcelPoint,Notes
>
> \"BIG BARN \-- HWY 3, Rock Creek,BC\",SRID=4326;POINT(-123.3715424
> 48.4180423),\"Added 2015/03/11\" \"CENTENNIAL CANDLE \-- Victoria,
> BC\",SRID=4326;POINT(-123.336306 48.423109),\"Added 2015/05/17\"
>
> parcelPoint is given in Extended Well Known Text format which is
> defined as follows: SRID=\<EPSG code\>; POINT(x y)
>
> where
>
> \<EPSG code\> is the projection code (e.g., 4326 is geographics, 3005
> is BC Albers, etc.)
>
> x is longitude in decimal degrees for SRID=4326, Easting otherwise
>
> y is latitude in decimal degrees for SRID=4326, Northing otherwise

# Preparing You Address File for the Batch Geocoder

> The batch geocoder accepts a csv file containing one or more addresses
> in one of two schemas: unstructured address and structured address.
>
> Put your address data into an unstructured address file if you
> haven\'t batch geocoded your address data with the Physical Address
> Geocoder before. It gives the geocoder more latitude in exploring
> alternate interpretations of an address to find the best match.
>
> Use the structured address file only if you want to know if your
> address data strictly conforms to the geocoder\'s structured address
> schema. If your source data is managed in a different schema, you may
> not get very good matches. For example, if you have an address element
> called *street* that includes street name, street type, and street
> directional; and you assign it to streetName in the structured address
> file, the geocoder won\'t find any good matches.

# 

# Unstructured Address File

> A file of unstructured addresses looks like this:
>
> addressString
>
> \"2317 MOODY AVE Kamloops, BC\"
>
> \"APT 1 1207 Douglas St, Victoria, BC\" \"525 Superior St,
> Victoria,BC\"
>
> \"4251A ROCKBANK PL, WEST VANCOUVER,BC\" \"4450 HAPPY VALLEY RD,
> METCHOSIN,BC\" \"2050 SW MARINE DR,VANCOUVER,BC\"
>
> \"420 GORGE RD E,VICTORIA,BC\" \"130A HALL ST,NELSON,BC\"
>
> \"UNIT 1, 7467 ASPEN BLVD,PEMBERTON,BC\"
>
> \"PAD 2, 2097 WILDFLOWER RD,SHAWNIGAN LAKE,BC\"
>
> \"PORT ALICE HEALTH CENTRE \-- 1090 MARINE DRIVE,PORT ALICE,BC\"
> \"ROYAL JUBILEE HOSPITAL \-- 1952 BAY ST,VICTORIA,BC\" \"CENTENNIAL
> CANDLE \-- VICTORIA, BC\"
>
> The first line tells the geocoder that each record contains a single
> column called addressString. Note that column names in CSV format are
> case-sensitive. Each line after the first contains an address as a
> single string in double quotes. Be sure each record has no trailing
> blanks or you will get a mysterious *Network or Server Error*. For
> best matching performance, the address should be in single-line format
> as described in the Single-Line Address Format section below.
>
> An addressString for a non-civic address must contain values for site
> name, locality name, and province code (see last example above). If
> you include a sitename in an address, it must be followed by the front
> gate delimiter (e.g., \"\--\") as in the following examples:
>
> Royal Jubilee Hospital \-- 1952 Bay St, Victoria, BC Kilometre Post
> 620 \-- HWY 3, Rock Creek, BC
>
> If you include a unit in an address with a site name, the unit must
> appear before the site name as in:
>
> RM 102, Port Alice Health Centre \-- 1090 Marine Dr,Port Alice,BC
>
> You can optionally include a column called yourId which should contain
> the identifier you use to uniquely identify each row. For example, you
> may have a list of ten clients and their addresses where three clients
> live at the same address. By providing a unique client id for each
> row, every client gets their own geocoded address. The yourId column
> will be output with each geocoded address so you can match each
> address up with a source client record.
>
> yourId,addressString
>
> A23E4,\"2317 MOODY AVE Kamloops, BC\"
>
> BXe33,\"APT 1 1207 Douglas St, Victoria, BC\" AA2w3,\"525 Superior St,
> Victoria,BC\" Q2S3E,\"4251A ROCKBANK PL, WEST VANCOUVER,BC\"
> E22Q3,\"4450 HAPPY VALLEY RD, METCHOSIN,BC\" E2f2e,\"2050 SW MARINE
> DR,VANCOUVER,BC\" E8f4k,\"420 GORGE RD E,VICTORIA,BC\" J4fkk,\"130A
> HALL ST,NELSON,BC\"
>
> U6t4m,\"UNIT 1, 7467 ASPEN BLVD,PEMBERTON,BC\"
>
> D4f76,\"PAD 2, 2097 WILDFLOWER RD,SHAWNIGAN LAKE,BC\"
>
> V8hj5,\"PORT ALICE HEALTH CENTRE \-- 1090 MARINE DRIVE,PORT ALICE,BC\"
> P3u4j,\"ROYAL ATHLETIC PARK \-- 1014 CALEDONIA AVE,VICTORIA,BC\"
>
> addressString is not included in the geocoded results file, only
> fullAddress which is the standardized and corrected address. To avoid
> having to use yourId to match up each address with the original
> address in a source client record, you can copy the value of
> addressString into yourId as well. Here is an example:
>
> yourId,addressString
>
> \"2317 MOODY AVE Kamloops, BC\",\"2317 MOODY AVE Kamloops, BC\"
>
> \"APT 1 1207 Douglas St, Victoria, BC\",\"APT 1 1207 Douglas St,
> Victoria, BC\" \"525 Superior St, Victoria,BC\",\"525 Superior St,
> Victoria,BC\"
>
> \"4251A ROCKBANK PL, WEST VANCOUVER,BC\",\"4251A ROCKBANK PL, WEST
> VANCOUVER,BC\" \"4450 HAPPY VALLEY RD, METCHOSIN,BC\",\"4450 HAPPY
> VALLEY RD, METCHOSIN,BC\" \"2050 SW MARINE DR,VANCOUVER,BC\",\"2050 SW
> MARINE DR,VANCOUVER,BC\"
>
> \"420 GORGE RD E,VICTORIA,BC\",\"420 GORGE RD E,VICTORIA,BC\" \"130A
> HALL ST,NELSON,BC\",\"130A HALL ST,NELSON,BC\"
>
> \"UNIT 1, 7467 ASPEN BLVD,PEMBERTON,BC\",\"UNIT 1, 7467 ASPEN
> BLVD,PEMBERTON,BC\"

# 

# Structured Address File

> A file of structured addresses looks like this:
>
yourId,siteName,unitDesignator,unitNumber,unitNumberSuffix,civicNumber,civicNumberSuffix,streetName,streetType,isStreetTypePrefix,streetDirection,isStreetDirectionPrefix,streetQualifier,localityName,provinceCode
myId1,,,,,2317,,MOODY,AVE,,,,Kamloops,BC
myId2,,APT,1,,1207,,Douglas,St,,,,Victoria,BC
myId3,,,,,525,,Superior,St,,,,Victoria,BC
myId4,,,,,4251,A,ROCKBANK,PL,,,,WEST VANCOUVER,BC
myId5,,,,,4450,,HAPPY VALLEY,RD,,,,METCHOSIN,BC
myId6,,,,,2050,,MARINE,DR,SW,true,,,VANCOUVER,BC
myId7,,,,,420,,GORGE,RD,E,,,,VICTORIA,BC
myId8,,,,,130,A,HALL,ST,,,,NELSON,BC
myId9,,UNIT,1,,7467,,ASPEN,BLVD,,,,PEMBERTON,BC
myId10,,PAD,2,,2097,,WILDFLOWER,RD,,,,SHAWNIGAN LAKE,BC
myId11,PORT ALICE HEALTH CENTRE,,,,1090,,MARINE,DRIVE,,,,PORT ALICE,BC
myId12,ROYAL ATHLETIC PARK,,,,1014,,CALEDONIA,AVE,,,,VICTORIA,BC
>
> The first line tells the geocoder what the name and order of the
> columns that define a structured address are. Column names are
> case-sensitive and explained in the table below. Only streetName or
> localityName column names are required but the more column names, the
> better the address match. Column names may appear in any order and all
> columns are of type String.
>
> A non-civic address must contain values for siteName, localityName,
> and provinceCode. A non-civic address may also contain values for
> street elements (e.g., streetName, streetType)
>
> yourId should contain an identifier you use to uniquely identify each
> row. For example, you may have a list of ten clients and their
> addresses where three clients live at the same address. By providing a
> unique client id for each row, every client gets their own geocoded
> address. The yourId column will be output with each geocoded address
> so you can match each address up with a source client record.
>
> Each line after the first contains a single structured address. Here
> are a few tips about the syntax of these lines:

1.  There must be one address per line.

2.  Each address must contain all columns specified in the header record
    and in the same order.

3.  If a field has no value, a single comma must be present to represent
    that column.

4.  A field value that contains a comma should be delimited with double
    quotes.

5.  Extra spaces between commas are included in the value of that column
    (e.g. , ***WILDFLOWER,*** is not the same as***,WILDFLOWER,*** )

6.  The character encoding of the file must be ANSI, not Unicode as per
    Microsoft-generated csv files.

7.  You may add extra columns by adding column names to the end of the
    first line in the file. If you do, every address record must contain
    values or commas for each extra column. The extra columns will not
    be copied to the address results file. Do not add columns containing
    private or sensitive data.

8.  Be sure each record has no trailing blanks or you will get a
    mysterious *Network or Server Error*.

The following table defines the meaning of each address element:

| Element           | Type     | Required       | Description                                    |
|-------------------|----------|----------------|------------------------------------------------|
| unitDesignator    | String   | No             | The type of unit within a house or building. Valid values are APT, BLDG, BSMT, FLR, LOBBY, LWR, PAD, PH, REAR, RM, SIDE, SITE, SUITE, TH, UNIT, and UPPR. The geocoder will try to match variations of these values on input (e.g., UPR) and output the standardized value (e.g., UPPR). |
| unitNumber        | String   | No             | The number of the unit within a house or building. |
| unitNumberSuffix  | String   | No             | A letter that follows the unit number, as in Unit 1A or Suite 302B. |
| civicNumber       | String   | No             | The official number assigned to a site on a street by an address authority. |
| civicNumberSuffix | String   | No             | A letter or fraction that follows the civic number. There should be no space between a civic number and a letter (e.g., Unit 1A) and one space between a civic number and a fraction (e.g., Suite 3 ½). |
| siteName          | String   | No for civic addresses, Yes for non-civic addresses | A string containing the name of the building, facility, or institution (e.g., Duck Building, Casa Del Mar, Crystal Garden, Bluebird House). A business name should only be used if it is permanently affixed to the site and the site has no other, more generic name. If a site is a unit within a complex, it may have a siteName in addition to a unitNumber and unitSuffix. |
| streetName        | String   | No             | The official name of the street recognized by a municipality (e.g., Douglas in 1175 Douglas Street). A streetName that starts with a directional is not abbreviated (e.g., North Park, not N Park). |
| streetType        | String   | No             | The type of street as assigned by a municipality (e.g., the ST in 1175 DOUGLAS ST) and is abbreviated if such an abbreviation exists. The set of all street types is defined by the provincial Integrated Transportation Network Program. |
| streetDirection   | String   | No             | The abbreviated compass direction as defined by Canada Post and B.C. civic addressing authorities. The complete list is C, E, N, NE, NW, S, SE, SW, and W. All street directions except C are defined by Canada Post. |
| streetQualifier   | String   | No             | The qualifier of a street name (e.g., the Bridge in Johnson St Bridge). |
| localityName      | String   | No             | The name of the municipality, community, Indian reservation, subdivision, regional district, aboriginal lands, or natural feature the site is located in. Since this is a physical address geocoder, not a mailing address geocoder, the locality of a civic address is that defined by the civic address authority, not Canada Post. A locality name that starts with a directional is not abbreviated (e.g., North Vancouver, not N Vancouver). Spelling of localities that are place names or natural feature names MUST match that published by the BC Geographical Names Information System. |
| provinceCode      | String   | No             | The ISO 3166-2 Sub-Country Code for British Columbia, which is BC. |

> For address element examples, see the next section.

# Single-Line Address Format

> An address may be represented by a single line (string) in one of the
> formats listed below.
>
> In each format, a term in square brackets is optional, a term in
> square brackets followed by an asterisk means the term may appear zero
> or more times, and a term in square brackets followed by a plus sign
> means the term may appear one or more times. A term in brace brackets
> (e.g., {streetDirection}) may appear in none or one of the multiple
> places indicated (e.g., Central St, N Central St, or Central St N, but
> not N Central St NE)
>
> Format 1 -- Civic address
>
> \[\[unitDesignator unitNumber\[unitNumberSuffix\]\] \[siteName\],\]\*
> frontGate civicNumber\[civicNumberSuffix\] {streetDirection}
> {streetType} streetName
>
> {streetType} {streetDirection} \[streetQualifier\], localityName,
> provinceCode
>
> Format 2 -- Non-civic address
>
> \[\[unitDesignator unitNumber\[unitNumberSuffix\]\] \[siteName\],\]\*
> frontGate \[{streetDirection} {streetType} streetName {streetType}
> {streetDirection} \[streetQualifier\],\] localityName, provinceCode
>
> Format 3 -- Intersection address
>
> {streetDirection} {streetType} streetName {streetType}
> {streetDirection} \[streetQualifier\] \[ and {streetDirection}
> {streetType} streetName {streetType}
>
> {streetDirection} \[streetQualifier\] \]+ , localityName, provinceCode

frontGate is the double dash delimiter (e.g., "\--"). Here is an example
of a civic address:

> 420A GORGE RD E, VICTORIA, BC
>
> which contains the following address elements:

  -----------------------------------------------------------------------
  **Address Element**              **Value**
  -------------------------------- --------------------------------------
  civicNumber                      420

  civicNumberSuffix                A

  streetName                       GORGE

  streetType                       RD

  streetDirection                  E

  localityName                     VICTORIA

  provinceCode                     BC
  -----------------------------------------------------------------------

> Here is an example of a civic address with a unit:
>
> UNIT 1A \-- 433 CEDAR RAPIDS BLVD, PEMBERTON, BC
>
> which contains the following address elements:

  -----------------------------------------------------------------------
  **Address Element**              **Value**
  -------------------------------- --------------------------------------
  unitDesignator                   UNIT

  unitNumber                       1

  unitNumberSuffix                 A

  civicNumber                      433

  streetName                       CEDAR RAPIDS

  streetType                       BLVD

  localityName                     PEMBERTON

  provinceCode                     BC
  -----------------------------------------------------------------------

> Here is an example of a non-civic address with a street qualifier:
>
> JOHNSON ST BRIDGE, VICTORIA, BC
>
> which contains the following address elements:

  -----------------------------------------------------------------------
  **Address Element**              **Value**
  -------------------------------- --------------------------------------
  streetName                       JOHNSON

  streetType                       ST

  streetQualifier                  BRIDGE

  localityName                     VICTORIA

  provinceCode                     BC
  -----------------------------------------------------------------------

> Here are some more examples:

1.  Civic addresses without a unit:

> 1025 HAPPY VALLEY RD, METCHOSIN, BC
>
> 130A HILL ST, NELSON, BC

2.  A civic address with a unit:

> PAD 2 \-- 1200 NORTH PARK RD, SHAWNIGAN LAKE, BC

3.  Civic addresses with a simple site name:

> PORT ALICE HEALTH CENTRE \-- 1090 MARINE DRIVE, PORT ALICE, BC ROYAL
> ATHLETIC PARK \-- 1014 CALEDONIA AVE, VICTORIA, BC

4.  Civic addresses with a unit within a named complex:

> PAD 2, HAPPY MOBILE HOME PARK \-- 1200 NORTH PARK RD, SHAWNIGAN LAKE,
> BC
>
> ROOM 103A, CLEARIHUE BUILDING, UNIVERSITY OF VICTORIA \-- 3800
> FINNERTY RD, VICTORIA, BC ROOM 230, WEST BLOCK, ROYAL JUBILEE HOSPITAL
> \-- 1952 BAY ST, VICTORIA, BC

5.  Non-civic addresses with a unit within a named complex:

> PAD 2, HAPPY MOBILE HOME PARK \-- NIMPO LAKE, BC
>
> PAD 2, HAPPY MOBILE HOME PARK \-- REMOTE RD, NIMPO LAKE, BC

6.  Non-civic address containing a street, locality, and province:

> KILOPOST 330 \-- WILLOW DRIVE, 70 MILE HOUSE, BC BIKE STAND 134 \--
> JOHNSON ST BRIDGE, VICTORIA, BC

7.  Non-civic address without a street:

> CENTENNIAL CANDLE \-- VICTORIA, BC

8.  Intersection addresses:

> Douglas St and Johnson St, Victoria, BC
>
> Douglas St and Gorge Rd E and Hillside Ave, Victoria, BC

# Alternative Address Formats

> On input, the geocoder can also handle the following alternatives to
> Single-Line Address Format:

1.  Unit without a frontGate:

> PAD 2, 1200 NORTH PARK RD, SHAWNIGAN LAKE, BC

2.  Unit number without a frontGate and unitDesignator (as per Canada
    Post):

> 2-1200 NORTH PARK RD, SHAWNIGAN LAKE, BC

3.  Unit following street (as per Canada Post):

> 1200 NORTH PARK RD PAD 2, SHAWNIGAN LAKE, BC
