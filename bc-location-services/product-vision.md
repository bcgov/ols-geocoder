# BC Location Services

## Product Vision

### Strategic Objectives
|||
|:---:|---|
**NO LOSSES**|No losses of any kind due to government use of inaccurate digital road services
**NEVER LOST, NEVER LATE**|No lost or late vehicles on the roads of British Columbia
**NEVER FAR**|No public facilities or services that are hard to find or reach by road

### Slogan
Find a better way with BC Location Services

## Product Line

### BC Address Geocoder

The [BC Address Geocoder](https://www2.gov.bc.ca/gov/content?id=118DD57CD9674D57BDBD511C2E78DC0D) provides address cleaning, correction, completion, geocoding, and reverse geocoding for government and the public at large. Geocoding turns a physical address into a form of spatial data called a point that can be used to display the location of the address on a map or answer spatial queries such as:

- how close is the nearest school, firehall, hospital, or swimming pool?
- are there any medical lab test facilities within three kilometres of a given medical drop-in clinic?

Reverse geocoding finds the nearest address to a given point. Address cleaning and standardization allow you to deduplicate your data and link data records that have no key fields in common.

Established in 2013, the BC Address Geocoder is used by over two dozen government ministries and agencies in dozens of government digital services. As of June, 2020, the geocoder has processed over one billion addresses.

Major clients include Elections BC, MOTI, MoH Health Ideas Warehouse, SDPR, LTSA, AVED Business Intelligence Warehouse, BC Centre for Disease Control, BC Oil and Gas Commission, MCFD, FLNROD, BC Health Authorities, City of Vancouver, BC Wildfire Service, BC Emergency Health Services, WildSafe BC.

The BC Address Geocoder is registered with the BC Data Catalogue, uses the DataBC API Gateway, and uses industry standards for its design and specification (e.g., REST, OpenAPI 3.0) which makes it easy to integrate into your application or API.

The BC Address Geocoder is based on authoritative address data provided by BC municipalities and BC Assessment. It also uses the BC Integrated Transportation Network which is the authoritative source for road network data in BC. In two independent studies, the BC Geocoder was found to be more accurate than commercial products.

The BC Address Geocoder is free and open to end-users. Geocoder addresses are covered under an open government license and their locations can be used for any legal purpose. For example, the Geocoder has become quite popular in the real-estate sector. Source code and documentation for the geocoder was released on GitHub under an Apache 2.0 license ([bcgov/ols-geocoder](https://github.com/bcgov/ols-geocoder)).

### BC Route Planner

Road and ferry travel are essential to the business of government. Route planning can be used to find the best routes, determine proximity of clients to service delivery locations, and optimize goods and service delivery.  Route planning is useful across a wide range of government business activities; from field operations to resource and emergency planning, to policy and law making.

Established in 2017, the [BC Route Planner](https://www2.gov.bc.ca/gov/content?id=9D99E684CCD042CD88FADC51E079B4B5) is recognized as a common component suitable for use across government. It is currently used by several government clients including the Ministry of Transportation and Infrastructure, Ministry of Health, Ministry of Citizen Services, BC Emergency Health Services, and TransLink.

The BC Route Planner is registered with the BC Data Catalogue, uses the DataBC API Gateway, and supports industry standards in its design and specification including REST and OpenAPI 3.0.

All source code for the BC Route Planner is available on GitHub and released under the Apache 2.0 open source license ([bcgov/ols-router](https://github.com/bcgov/ols-router)). The BC Route Planner leverages other open source projects such as JSprit and Apache Spring.

### Geomark Web Service

The [Geomark Web Service](https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084) allows you to create and share geographic areas of interest over the web in a variety of formats and coordinate systems. This service is especially helpful when you need to share an area of interest with people who require that the data be in a different format, or they use different mapping software.

Established in 2010, the Geomark Web Service continues to support a variety of clients both within and external to government. Although it is a mature product, the web service and web application continue to be updated as required as seen in the [What's New](https://www2.gov.bc.ca/gov/content?id=C88C27C98E9B4BACBC0978017CAA5F84) page.

The Geomark Web Service is registered with the BC Data Catalogue, and supports industry standards in its design and specification including REST and OpenAPI 3.0.

Although the source code for the Geomark Web Service has not been released as open source we have included scripts to make use of the API under an Apache 2.0 license on GitHub ([bcgov/ols-devkit](https://github.com/bcgov/ols-devkit/tree/gh-pages/geomark/scripts))
