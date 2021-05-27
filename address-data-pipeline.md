# A Proposal For A Better Implementation of the Geocoder Data Integration Process

## Executive Summary
Integrating addresses and their occupants into a road network requires geocoding the addresses twice. Validating this integration requires an additional geocoding of test addresses. Currently, the integrator and geocoder are separately deployed components. We propose to combine the two into a single application, called Geocodable BC Maker, to simplify administration and maintenance, and dramatically reduce processing time. This will allow more frequent geocoder data updates with less effort.

## Introduction
This document outlines a proposal for a new implementation of the geocoder data integration process that is simpler to operate, easier to maintain, faster to run, and more supportive of complex addressing scenarios. We first look at the integration process itself, describe how it can be improved, and contrast the proposed implementation with the current one.

## What is Geocoder Data Integration?
Here's an overview of the geocoder data integration process:
<br><br>

| Gather | Transform | _**Integrate**_ | Validate | Deploy

<br>

In Gather, we manually download source road network and address data.

In Transform, we transform all source data into standard schemas and formats.

In Integrate, we tie addresses to the road network and generate address ranges.

In Validate, we geocode our test address data using the integrated data.

In Deploy,  if the validation was successful, we Deploy the integrated data to a given geocoder environment.

The Integrate stage lies at the heart of the process and our change proposal mostly affects this stage so let's take a closer look.  


### Tying addresses to block-faces
Geocoder data integration is primarily about tying the latest candidate reference addresses to the latest version of the reference road network (e.g.,  BC Digital Road Atlas) and deriving address ranges. Here is a small portion of the latest DRA and the latest candidate reference addresses:

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/9a80391a-d380-4f4e-a018-1a8bb3d6dcfa)

Each candidate reference address must refer to a road segment in the DRA and that road segment must have a left or right locality name that is identical to the locality name in the candidate reference address. For _2201 Kaslo Creek South Fork Rd, Kaslo, BC_, the DRA must have a road segment named _Kaslo Creek South Fork Rd_ and the left or right locality name for that road segment must be _Kaslo_

![image](https://user-images.githubusercontent.com/11318574/119045191-fc47bb80-b96f-11eb-80f8-0d08928a5677.png)

The address block assignment process finds the DRA road segment with the same street and locality names as our candidate reference address then creates an access point along the curb of the road segment at the nearest point to the site point.

![image](https://user-images.githubusercontent.com/11318574/119048092-9826f680-b973-11eb-8bd2-4aa4d2c96f25.png)

A candidate reference address is rejected for any of the following reasons:

1. The DRA has no road segment that contains matching street and locality names.
2. There is no matching road segment within 2km of the candidate reference address point.
3. The access line (e.g., a line between the site location and access point) crosses one or more roads.

The address block assignment process is repeated for all remaining candidate reference addresses.

![image](https://user-images.githubusercontent.com/11318574/119048628-58144380-b974-11eb-9913-f973ae75a52d.png)

### Address Range Generation
Address ranges are derived from the minimum and maximum civic numbers assigned to each block face. Here's a hypothetical example of civic numbers assigned to three consecutive blocks:

Block 1|Block 2|Block 3|
|--|--|--|
7  11 17 43 99 |                      | 207 217 243 297
4 10 18 48 96 |                       |  210 220 240 280

Here are the address ranges expressed as anchor points which are minimum and maximum civic numbers in bold in each block:

Block 1|Block 2|Block 3|
|--|--|--|
**7** 7  11 17 43 99 **99** |                      | **207** 207 217 243 297 **297** |
**4** 4 10 18 48 96 **96** |                       | **210** 210 220 240 280 **280** |

The address range generator will stretch the anchor points to the theoretical minimum and maximum as follows:

Block 1|Block 2|Block 3|
|--|--|--|
**1** 7  11 17 43 97 **99** |                      | **201** 207 217 243 297 **299**
**2** 4 10 18 48 96 **98** |                       |  **200** 210 220 240 280 **298**

The range generator will also fill in gaps in the address fabric as follows:

Block 1|Block 2|Block 3|
|--|--|--|
**1** 7  11 17 43 97 **99** | **101** **199** | **201** 207 217 243 297 **299**
**2** 4 10 18 48 96 **98** |                       |  **200** 210 220 240 280 **298**

If a block has even numbers on one side and odd numbers on the other, and one of the sides has no civic numbers, the range generator will fill in the missing side appropriately as follows:

Block 1|Block 2|Block 3|
|--|--|--|
**1** 7  11 17 43 97 **99** | **101** **199** | **201** 207 217 243 297 **299**
**2** 4 10 18 48 96 **98** |**100** **198** |  **200** 210 220 240 280 **298**

<br><br>
## Proposed Implementation of the Geocoder Data Integration Process

Here is the proposed implementation of the geocoder data integration process:

Stage name|Description|Implementation
|:--:|--|--|
|Gather|Gather reference road, address, and occupant data from authoritative sources.|Manual download from websites run by Integrated Cadastral Initiative Society, BC Assessment, GeoBC, and municipalities that have addresses on their open data websites but are not members of the ICI Society. This stage is unchanged.
||Each source dataset may have a different access method (e.g. download, API), data schema, and update schedule| For the latest occupants, manually initiate the occupant export function of the Geographic Site Registry in the BC Geographic Warehouse.
|Transform|Transform each reference address dataset from its local schema and format to a single, standard schema and format called the [*Physical Address Exchange* (PAX) Standard](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md) PAX can support complex addressing scenarios more easily than the current target schema and format. For example, four-hundred unit-level addresses for an apartment building with 20 floors and 20 units per floor can be defined in 20 lines, one line per floor.|One ETL script (doesn't have to be FME) for each source data format (e.g., AddressBC, BC Assessment). We use FME for existing ETL scripts.
|Integrate|Integrate reference addresses (in PAX format) into reference road segments (blocks) and generate block ranges, block anchor points, address access points, and address parcel ids.|A standalone Java application (e.g., WAR file) called Geocodable BC Maker which contains an embedded instance of the geocoder.
||A reference address is rejected if:<br>* it doesn't conform to the PAX schema<br>* the given street doesn't exist in the given locality in the reference road network.<br>* the given address location is too far from the correct blockface on the given street in the reference road network.<br>* the address is a duplicate of an address that came from a higher-ranked data source.|The geocoder embedded in Geocodable BC Maker is configured with its own deployment of the Geocoder Admin App|Data sources are ranked as follows:<br>1. AddressBC<br>2. Open data munis<br>3. BC Assessment<br>
||After reference addresses have been integrated, associate candidate occupants with reference addresses; reject occupants whose address is not a valid reference address.
||Also generate locality aliases, qualified locality names from official populated place names, street name indexes, and other indexes necessary for speedy address matching|
|Validate|Verify that the new reference address dataset is globally valid.|The embedded geocoder in Geocodable BC Maker will be loaded with the new data and used to geocode test addresses.| 
||Globally valid means the dataset is <br> * locality-complete (e.g. has addresses from every locality) <br> * match-correct (e.g., all test addresses geocode as expected) <br> * spatially-consistent (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction), and <br>  * version-consistent (e.g. locality address counts are higher than the previous version of reference data)|
Deploy| If validation is successful, make new reference road network and address list accessible to online and batch geocoder|Manually trigger online geocoder restart script and restart batch geocoder plugin in CPF using CPF admin application.

### What's different?

The current implementation of the geocoder data integration process needs a dedicated, standalone, batch geocoder that must be loaded with reference data three times during the integration process as follows:

* Load 1 requires the latest road network and no addresses to confirm candidate reference addresses have valid streets within localities.
* Load 2 requires the latest road network and new reference addresses to confirm candidate reference occupants have valid addresses.
* Load 3 requires the latest road network and new reference addresses and occupants to confirm correct handling of test addresses.

The geocoder is written in Java. 

The current implementation also needs a standalone Java application which handles address block assignment and range generation and is appropriately named the *Block  Assignment and Address Range Generator* (BAARG).

In the new implementation, all integration and verification steps will be moved from separate FME scripts that call out to the batch geocoder, to a single Java application called Geocodable BC Maker which will have an embedded geocoder. Geocodable BC Maker will also incorporate an enhanced version of the BAARG. This simplifies the data integration architecture by eliminating the need for an external batch geocoder, speeds up the integration process, localizes all integration algorithms into a single component for easier understanding and maintenance, and leaves the task of keeping up with constantly changing data source schemas and formats to easily-updated scripts.

## Activity diagrams of current and proposed implementations

### Current implementation

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/d19ba328-5612-404b-a364-6b0216817c9a)
<br><br><br>

### Proposed implementation

![image](https://user-images.githubusercontent.com/11318574/119588557-ea0db900-bd85-11eb-9e3f-6e6880570b9d.png)

<br><br>

## Architecture Diagrams of current and proposed implementations

The current implementation is on the left; the proposed implementation on the right:

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/082b65d9-cc15-43af-9b39-dd0dc6b68215)

<br><br>
## Data flow diagrams of current implementation

### Gather and Transform Steps
Here is the data flow for the current implementation of the gather and transform steps. In the proposed implementation, the gather step is the same but the transform step must be changed to support the new target Physical Address Exchange Standard.

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/a714c132-e764-41c7-983c-67fa952465c0)

<br><br>

### Current implementation of Geocoder data integration process

Data flow diagrams of current implementation of geocoder data integration process are available [here](https://github.com/bcgov/ols-geocoder/issues/243)
