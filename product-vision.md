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

Reverse geocoding finds the nearest address to a given point. Address cleaning and standardization allow you to improve address quality, remove duplicates, and link client records that have no direct linkage (e.g., client number).

#### History at the Province of British Columbia

Established in 2013 by the Province of British Columbia and released under the Apache 2.0 open source license in 2019, the BC Address Geocoder is the reference deployment of the OLS Address Geocoder. The BC Address Geocoder, is used by over two dozen government ministries and agencies in dozens of government digital services. As of June, 2021, the BC Address Geocoder has processed over 1.5 billion addresses.

Major clients include:
- Elections BC
- BC Ministry of Advanced Education Business Intelligence Warehouse
- BC Ministry of Children and Family Development
- BC Ministry of Education
- BC Ministry of Forests, Land, and Natural Resource Operations and Rural Development
- BC Ministry of Health Health Ideas Warehouse
- BC Ministry of Social Development and Poverty Reduction
- BC Ministry of Transportation and Infrastructure
- BC Centre for Disease Control
- BC Oil and Gas Commission
- BC Land Title and Survey Authority
- BC Health Authorities
- BC Wildfire Service
- BC Emergency Health Services
- City of Vancouver
- City of Nelson
- WildSafe BC

The BC Address Geocoder is based on authoritative address data provided by BC municipalities and BC Assessment. It also uses the BC Integrated Road Network which is the authoritative source for road network data in BC. In two independent studies, the BC Geocoder was found to be more accurate than commercial products.

The BC Address Geocoder is a recognized common component that is suitable for use in BC Government applications and APIs.

The BC Address Geocoder is free and open to end-users. Geocoder addresses are covered under an open government license and their locations can be used for any legal purpose. For example, the Geocoder has become quite popular in the real-estate sector. 

#### Open Source
In 2019, source code for the BC Address Geocoder was released as the OLS Address Geocoder under the Apache 2.0 Open Source License with the intent of building a community of government partners to share the effort of code enhancement and maintenance, ideas, documentation, and advocacy. In its current form, the OLS Address Geocoder is most suitable to local, provincial, and federal governments of Canada since it is consistent with Canada Post address standards, deviating only where necessary such as the use of civic authority instead of postal community (e.g., civic authority of North Saanich instead of postal community of Victoria for a civic address in North Saanich).

#### Architecture Overview
The OLS Address Geocoder is designed to run behind an API Gateway such as [Kong](https://github.com/Kong/kong), which is a plugin for [NGINX](https://www.nginx.com/). Both Kong and NGNIX are open source projects. The OLS Address Geocoder API is RESTful which makes it easy to integrate into your application or API. The API is defined using the industry standard OpenAPI 3.0.

OLS Address Geocoder exposes an online API using the [Apache Spring](https://spring.io/) and a batch geocoding API through the [Concurrent Processing Framework](https://bcgov.github.io/cpf/). CPF is another open source project in Open Location Services. The CPF supports a single scheduler, multiple worker architecture. Thanks to the CPF, the batch geocoder can process seven million addresses per hour and there are plans to double throughput in the next year.
<br><br><br>


### OLS Route Planner

Road and ferry travel are essential to the business of government. Route planning can be used to find the best routes, determine proximity of clients to service delivery locations, and optimize goods and service delivery. Route planning is useful across a wide range of government business activities; from field operations to resource and emergency planning, to policy and law making.

#### History
Established as an open source (Apache 2.0) project in 2017, the OLS Route Planner is deployed at the Province of British Columbia as the BC Route Planner. Recognized as a common component suitable for use across the BC Government, the BC Route Planner, is currently in use by several government clients including the Ministry of Transportation and Infrastructure, Ministry of Health, Ministry of Citizen Services, BC Emergency Health Services, and [TransLink](https://translink.apps.gov.bc.ca/trp/).

#### Open Source
The OLS Route Planner is released under the Apache 2.0 Open Source License with the intent of building community of government partners to share the effort of enhancement and maintenance, ideas, documentation, and advocacy. The OLS Route Planner uses the excellent [jSprit](https://jsprit.github.io/index.html) java library for route optimization and [Apache Spring](https://spring.io/) to implement the online geocoder REST framework.

#### Architecture Overview
The OLS Route Planner is designed to run behind an API Gateway such as [Kong](https://github.com/Kong/kong), which is a plugin for [NGINX](https://www.nginx.com/). Both Kong and NGNIX are open source projects. The OLS Route Planner API is RESTful which makes it easy it integrate into your application or API. The OLS Route Planner is defined using the industry standard OpenAPI 3.0.
