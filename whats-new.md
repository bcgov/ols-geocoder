# What's New with the BC Address Geocoder

Check out the Geocoder release [roadmap](https://github.com/bcgov/ols-geocoder/blob/gh-pages/roadmap.md)
For detailed API release notes, see the [BC Geocoder Developer Guide](https://github.com/bcgov/api-specs/blob/master/geocoder/geocoder-developer-guide.md)

## February 2, 2021
- BC Address Geocoder Version 4.1.0 released (both online and batch geocoders)
- Improved address match accuracy
   - The address list match accuracy of a government list of 13 million addresses went from 58% in version 4.0 to 75% in version 4.1, a 17% improvement
   - 2% improvement is due to additional address ranges (from GeoBC Integrated Transportation Network), additional locality aliases (e.g., all neighbouring localities, abbreviations such as Poco for Port Coquitlam), and additional non-standard abbreviations in common usage (e.g., Bu for Boulevard, Cre for Crescent, Dri for Drive).
    - 15% improvement is due to smarter address parsing
    - [Here](https://github.com/bcgov/ols-geocoder/blob/gh-pages/rejected-addresses.md) is a list of typical bad addresses and how v4.1 handles them compared to v4.0    
    - [Here](https://github.com/bcgov/ols-geocoder/issues/102) is the complete list of added abbreviations.
   
- There is a new [batch address list metrics calculator](https://github.com/bcgov/ols-devkit/tree/gh-pages/alm) that takes your batch geocoder results file and computes address list match accuracy, address counts by score interval, and other metrics that let you see exactly how much better your results are in version 4.1

- Added a way for houses on Indian Reserves that are assigned unique civic numbers but no street names to be modelled in the geocoder. Address authorities may now supply such addresses to DataBC in the form: ***House N -- IRName -- localityName, BC*** as in the following example: ***HOUSE 900 Malachan 11 -- Ditidaht, BC***

- To reduce user confusion during address autocompletion, the geocoder no longer returns duplicate localities (e.g., ***Mill Bay*** on Vancouver Island and ***Mill Bay*** in Northern BC) in autocomplete mode.
   - We took all official populated placenames that aren't defined as localities in the Integrated Transportation Network and made them sites. 
   - Some of these sites, such as Brentwood Bay, are situated in an ITN locality and look like this: ***Brentwood Bay in Central Saanich, BC***
   - Other sites are situated outside but near ITN localities and look like this: ***Boston Bar 1 near Boston Bar, BC***
   - In applications that use the geocoder for address completion, when entering just a locality, the geocoder will search these new sites as well. For example, entering ***Brentwood Bay *** will return ***Brentwood Bay in Central Saanich, BC***. 
   
- Online and Batch Geocoder APIs and the [batch address list submitter](https://github.com/bcgov/ols-devkit/tree/gh-pages/als) are unchanged from version 4.0.2


## July 28, 2020
- BC Address Geocoder version 4.0.2 released.
- Batch Geocoder version 4.0.2 released.
- Improved handling of new road classes
- Address data updated to reflect sources available as of July 2020.

## June 24, 2020
- BC Batch Address Geocoder version 4.0.1 released
- API is backward compatible with version 3.4.2. with one exception:
    - geojson input and output no longer support coordinates in Spatial Reference Systems other than EPSG 4326 (lon/lat) as per [latest specification](https://tools.ietf.org/pdf/rfc7946.pdf) of the geojson standard.

## March 31, 2020
- BC Online Address Geocoder version 4.0.1\* released.
- API is backward compatible with version 3.4.2. with one exception:
    - geojson input and output no longer support coordinates in Spatial Reference Systems other than EPSG 4326 (lon/lat) as per [latest specification](https://tools.ietf.org/pdf/rfc7946.pdf) of the geojson standard.
- Improved error handling for GeoJSON output.
- Improved security and performance.
- Road network and address data updated to reflect sources available as of March 2020.
<br><br>\* Excluding Batch Geocoder software, which remains at version 3.4.2 pending future update

## September 1, 2018
- The following URLs are deprecated, no longer supported and may be shut down in the future:

```
http://apps.gov.bc.ca/pub/geocoder

https://apps.gov.bc.ca/pub/geocoder

```
- The following URL should be used, with or without an API key:

```
https://geocoder.api.gov.bc.ca

```

- A rate limit of 1,000 requests per minute now applies to use without an API key.

## June 19, 2018
BC Address Geocoder version 3.4.1 released.
- The BC Digital Road Atlas for May, 2018 has been loaded.
- A bug fixed in the address range generator has resulted in 10% more blocks with addresses.
- In the occupants address resource (e.g., /occupants/addresses),the fullAddress response parameter, occupant name is now separated from address by two consecutive asterisks as in the following:

```
Sir James Douglas Elementary ** 401 Moss St, Victoria, BC 
```
If addressString doesn’t contain the occupant separator “\*\*”, addressString will be treated as an occupant name. In previous releases, addressString was treated as an address.

## April 24, 2018
BC Address Geocoder version 3.4 released.
- Electoral Area is a new attribute that is now returned in address results for addresses outside of incorporated areas. For example, 11237 West Coast Rd, Shirley, BC is in the CAPRD Juan de Fuca Electoral Area.
- Township of Langley is now distinguished from City of Langley by the appropriate value in localityDescriptor  (e.g., Township, City).
- District Municipality of North Vancouver is now distinguished from City of North Vancouver by the appropriate value in localityDescriptor (e.g., District Municipality, City).
- A single PID response from parcels/pids is now a string instead of a number.
- Minor bug fixes.

## April 20, 2018
- Bug fixed in Address List Editor Export Results feature.

## March 13, 2018
BC Address Geocoder version 3.3 released.
- Updated with January 2018 data.
Various data improvements.

## January 14, 2018
BC Address Geocoder version 3.2.2 released.
- Improved address auto-complete.
- Added new brief parameter that specifies only a subset of address attributes be returned.
- Improved reverse geocoding to the nearest address access point.
- Road network and address data updated to reflect sources available as of September 2017.

## May 11, 2017
BC Address Geocoder version 3.1.2 released.
- Increased number of physical addresses, including highway addresses.
- New developer tools added, including a location services demo application.

## November 8, 2016
BC Address Geocoder version 3.0.0 released.
- Improved performance: the Online Geocoder is 20% faster and the Batch Geocoder is five times faster.
- Higher availability: load balanced and geo-redundant across two data centres.
- excludeUnits parameter was added.
- Road network and address data updated to reflect sources available as of October 2016.  This update includes 50,000 more address points.
- Improved address range generation.
- Improved handling of highway addresses.
- General improvements and bug fixes.

## March 13, 2016
BC Address Geocoder version 2.0.1 released.
- 1.4 million more addressed sites loaded, for a total of 1.8 million.
- Improved geocoding positional accuracy.
- Improved address ranges.
- Added new site occupants: hospitals, schools, and post-secondary education institutions.
- General improvements and bug fixes.
- Documentation updated.

## December 10, 2015
BC Address Geocoder version 2.0 released.
- Support for occupant searches - you can now search for a limited number of public facilities and related businesses.
- Content Types for JSON and XML requests have been fixed.
- Documentation updated.

## September 6, 2015
BC Address Geocoder version 1.7 released.
- Support for street, locality and province in non-civic addresses.
- Intersection degree added to Intersection Address and Site and Intersection Address records in Geocoder responses.
- Intersection URL removed from Intersection Address.
- Documentation updated.

## May 10, 2015
BC Address Geocoder version 1.6 released.
- Minor improvements and bug fixes.

## January 25, 2015
BC Address Geocoder version 1.5 released.
- In online and batch geocoders, you can now filter result geographically by providing a bounding box or a list of locality names.
- The Address List Editor is improved with support for copy/paste to/from spreadsheets, an export function that preserves your source columns, a show in map function, a cleaner look and feel and much faster geocoding.
- There is a new batch address list submitter script in python that lets you add batch geocoding functionality into your server-side applications.
- In online and batch geocoders, floor numbers are now matched and standardized (e.g., 5th floor becomes FLR 5).
- You can now perform a local search for addresses. The sites/near resource returns site addresses near a given origin point. - - You can specify how many results to return and they will be sorted in increasing distance from the origin. There is also a new local search resource called intersections/near that returns intersection addresses near a given origin point.
- sites/nearest and intersections/nearest will now find the nearest site or intersection no matter how far away it is. 
- In sites/nearest, intersections/nearest, and sites/within, you can now add a setback to the accessPoint position.
- You can now specify the desired level of address match through a new matchPrecision parameter. For example, if you set matchPrecision=locality, the address 525 Superior St, Victoria, BC will return a match of Victoria, BC but not Superior St, BC or 525 Superior St, BC.
- Documentation has also been updated.

## May 12, 2014
BC Address Geocoder version 1.4 released.
- There is a new autocompleting address finder widget that you can incorporate into your own web apps and web pages.
- 500,000 strata units from BC Assessment have been loaded.
- The February 2014 release of the Digital Road Atlas has been loaded.
- The Geocoder’s parser has been overhauled for much improved performance and smarter matching.
- French input is now recognized and translated.
- Nested sites are now supported and recognized on input with the help of a frontGate delimiter ("--") as in "RM 605, West Wing, Royal Jubilee Hospital --1952 Bay St, Victoria BC.
- Non-civic addresses (e.g., sitename, locality, province) are now supported.
- Partial street and locality names are now matched. For example, 4450 happy rd metchosin bc will be matched to 4450 Happy Valley Rd Metchosin BC.
- Prefix street directionals and street types are now recognized on input and formatted appropriately in fullAddress.
- Accents and punctuation are now allowed in reference addresses.
- The Geocoder now recognizes and matches streetQualifiers (e.g., the Bridge in Johnson St Bridge).
- The Geocoder now matches units (e.g., the 104 in 104-525 Superior St Victoria BC). In v1.3, units were recognized but not matched.
- Cross Origin Scripting (CORS) is now supported.
- A new document entitled Understanding Geocoder Output has been published and all existing documents have been updated.

## July 31, 2013
BC Address Geocoder version 1.2 released.
- Geocoder URL and API have been changed to support improved long term use.
- New Address List Editor tool available.
- Significant improvements to Geocoder address matching and locating accuracy.
- Reverse geocoding functions are now available.
- Unit designators are now recognized for more complete address validation.
- Address locations in KML output are styled to reflect location type and positional accuracy.
- New documentation, including: Guide to Preparing Address Lists for Geocoding, REST API Guide for Developers, Javascript API Guide for Developers, expanded Glossary.
- General improvements and bug fixes

## January 25, 2013
BC Address Geocoder v1.0 released (under name of Physical Address Geocoder)
