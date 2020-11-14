# Open Location Services (OLS)

## Product Vision

### Strategic Objectives
|||
|:---:|---|
**NO LOSSES**|No losses of any kind due to government use of inaccurate digital road services
**NEVER LOST, NEVER LATE**|No lost or late vehicles on the road
**NEVER FAR**|No public facilities or services that are hard to find or reach by road

### Slogan
Find a better way with Open Location Services

## Product Line

### OLS Address Geocoder

The OLS Address Geocoder provides address cleaning, correction, completion, geocoding, and reverse geocoding for government and the public at large. Geocoding turns a physical address into a form of spatial data called a point that can be used to display the location of the address on a map or answer spatial queries such as:

- how close is the nearest school, firehall, hospital, or swimming pool?
- are there any medical lab test facilities within three kilometres of a given medical drop-in clinic?

Reverse geocoding finds the nearest address to a given point. Address cleaning and standardization allow you to deduplicate your data and link data records that have no key fields in common.

#### History at the Province of British Columbia

Established in 2013 and released under the Apache 2.0 open source license in 2019, the OLS Geocoder is deployed in the Province of British Columbia as the BC Address Geocoder. The BC Address Geocoder, is used by over two dozen government ministries and agencies in dozens of government digital services. As of June, 2020, the BC Address Geocoder has processed over one billion addresses.

Major clients include Elections BC, MOTI, MoH Health Ideas Warehouse, SDPR, LTSA, AVED Business Intelligence Warehouse, BC Centre for Disease Control, BC Oil and Gas Commission, MCFD, FLNROD, most BC Health Authorities, City of Vancouver, BC Wildfire Service, BC Emergency Health Services, WildSafe BC.

The BC Address Geocoder is based on authoritative address data provided by BC municipalities and BC Assessment. It also uses the BC Integrated Road Network which is the authoritative source for road network data in BC. In two independent studies, the BC Geocoder was found to be more accurate than commercial products.

The BC Address Geocoder is free and open to end-users. Geocoder addresses are covered under an open government license and their locations can be used for any legal purpose. For example, the Geocoder has become quite popular in the real-estate sector. 

The OLS Address Geocoder was released under the Apache 2.0 Open Source License with the intent of building community to share the enhancement and maintenance burden, ideas, documentation, and advocacy. In its current form, the OLS Address Geocoder is most suitable to local, provincial, and federal governments of Canada since it is consistent with Canada Post mailing address standards, deviating only where necessary such as the use of civic authority instead of postal community (e.g., civic authority of North Saanich instead of postal community of Victoria for a civic address in North Saanich).

The OLS Address Geocoder is designed to run behind an API Gateway such Kong, which is a plugin for NGINX. Both Kong and NGNIX are open source projects. The API is a REST API defined using the industry standard OpenAPI 3.0 which makes it easy to integrate into your application or API.


### OLS Route Planner

Road and ferry travel are essential to the business of government. Route planning can be used to find the best routes, determine proximity of clients to service delivery locations, and optimize goods and service delivery. Route planning is useful across a wide range of government business activities; from field operations to resource and emergency planning, to policy and law making.

Established as an open source (Apache 2.0) project in 2017, the OLS Route Planner is deployed at the Province of British Columbia as the BC Route Planner. Recognized as a common component suitable for use across government, The BC Route Planner, is currently in use by several government clients including the Ministry of Transportation and Infrastructure, Ministry of Health, Ministry of Citizen Services, BC Emergency Health Services, and TransLink.

The BC Route Planner is registered with the BC Data Catalogue, uses the DataBC API Gateway, and supports industry standards in its design and specification including REST and OpenAPI 3.0. This makes it easy for developers to use our services in their digital services.

The OLS Route Planner was released under the Apache 2.0 Open Source License with the intent of building community to share the enhancement and maintenance burden, ideas, documentation, and advocacy. The OLS Route Planner leverages other open source projects such as JSprit and Apache Spring.
