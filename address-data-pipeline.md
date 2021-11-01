# Geocoder Data Integration Process

## Table of Contents
[Introduction](#intro)<br>
[What is geocoder data integration?](#integration-defined)<br>
[Into the Heart of Integrate: Tying addresses to blocks](#tying)<br>
[Into the Heart of Integrate: Address Range Generation](#address-range)<br>
[Weaknesses of current implementation](#weaknesses)<br>
[Requirements of a new implementation](#requirements)<br>
[Data Flow Diagrams](#data-flow)<br>
[Activity diagrams](#activity)<br>
[Architecture Diagrams](#architecture)<br>

<a name=intro></a>
## Introduction
This document describes the current geocoder data integration process and how it can be improved.

<a name=integration-defined></a>  
## What is Geocoder Data Integration?
Here's an overview of the geocoder data integration process:
<br><br>

### | **Gather** | **Transform** | _**Integrate**_ | **Verify** | **Deploy**|

<br>

In Gather, we download source road network and address data.

In Transform, we transform all source data into standard schemas and formats and do field-level validation.

In Integrate, we tie addresses to the road network and generate address ranges.

In Verify, we verify the quality of the integrated data by geocoding our Acceptance Test addresses.

In Deploy, if the validation was successful, we deploy the integrated data to a given geocoder environment.

The Integrate stage lies at the heart of the process and our change proposal mostly affects this stage so let's take a closer look.  

<a name=tying></a>
### Into the Heart of Integrate: Block Assignment
Geocoder data integration is primarily about tying the latest candidate reference addresses to the latest version of the reference road network (e.g.,  BC Digital Road Atlas) and deriving address ranges. Here is a small portion of the latest DRA and the latest candidate reference addresses:

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/9a80391a-d380-4f4e-a018-1a8bb3d6dcfa)
<br><br>
Each candidate reference address must refer to a road segment (or block) in the DRA and that road segment must have a left or right locality name that is identical to the locality name in the candidate reference address. For _2201 Kaslo Creek South Fork Rd, Kaslo, BC_ (see below), the DRA must have a road segment named _Kaslo Creek South Fork Rd_ and the left or right locality name for that road segment must be _Kaslo_

![image](https://user-images.githubusercontent.com/11318574/119045191-fc47bb80-b96f-11eb-80f8-0d08928a5677.png)

The address block assignment process finds the DRA road segment with the same street and locality names as our candidate reference address then creates an access point along the curb of the road segment at the nearest point to the site point.

![image](https://user-images.githubusercontent.com/11318574/119048092-9826f680-b973-11eb-8bd2-4aa4d2c96f25.png)

A candidate reference address is rejected for any of the following reasons:

1. The DRA has no road segment that contains matching street and locality names.
2. There is no matching road segment within 2km of the candidate reference address point.

The address block assignment process is repeated for all remaining candidate reference addresses.

![image](https://user-images.githubusercontent.com/11318574/119048628-58144380-b974-11eb-9913-f973ae75a52d.png)

<a name=address-range></a>
### Into the Heart of Integrate: Address Range Generation
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

<a name=weaknesses></a>
### Weaknesses of current implementation

The current geocoder data integration process has three main weaknesses:

- It is too fussy. The current process requires many manual steps that require careful typing and lots of visual confirmation.
- It takes too long. It takes approximately three business days to complete.
- It ties up our delivery and test environments; conversely, delivery and testing of code fixes and enhancements often blocks the data integration process. We rarely keep to our stated monthly data update schedule.
<br><br>

<a name=requirements></a>
## Requirements of a new Geocoder Data Integration Process

Here are the requirements of a new geocoder data integration process:

Stage name|Description|Implementation
|:--:|--|--|
|Gather|Gather reference road, address, occupant, locality name, parcel, and Electoral Area data from authoritative sources.|Manual download from BC Data Catalogue, websites run by Integrated Cadastral Initiative Society, BC Assessment, GeoBC, and municipalities that have addresses on their open data websites but are not members of the ICI Society. Download could be automated but sources that require login will pose some difficulty. 
||Each source dataset may have a different access method (e.g. download, API), data schema, and update schedule.| Latest OGL-licensed occupants are automatically exported from the Geographic Site Registry to a staging area using an FME script then verified manually.
|Transform|Transform each reference address dataset from its local schema and format to a single, standard schema and format|One ETL script (doesn't have to be FME) for each source data format (e.g., AddressBC, BC Assessment). We use FME for existing ETL scripts.<br><br> We should encourage address authorities that provide addresses directly to GeoBC to use the [*Physical Address Exchange* (PAX) Standard](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md). PAX can support complex addressing scenarios more easily than the current AddressBC schema and format. For example, four-hundred unit-level addresses for an apartment building with 20 floors and 20 units per floor can be defined in two records, one for the apartment building itself and one for all the units using unitNumber expressions (e.g., 101-120,201-220,...,2001-2020).<br><br> We should also encourage AddressBC to adopt PAX to make it easier for address authorities to provide unit numbers which is something they are currently reluctant to do.
||Addresses are rejected if they fail field-level validation. Types of field validation failure include a field value not being the right type (e.g., a string where a number is expected), no value for a mandatory field, field value not in the allowed list of values, and numeric field value out of range. Rejected addresses are saved for QA analysis by the appropriate data authorities.
||Addresses are also rejected if their street doesn't exist within the specified locality. For example, an address like 10 Main St W, Lantzville will pass field-level validation. To determine existence of *Main St W* in *Lantzville*, the Transform stage needs to perform a street-level geocode of the address using a geocoder that is loaded with the latest reference road network.|With the appropriate orchestration tool, an isolated batch geocoder can be preconfigured, deployed and loaded with the appropriate candidate address data.<br><br>Determination of street existence within a locality is currently done using only unique street/locality pairs of which there are only about 200,000. This approach does require a complex join to the original full address which is quite time consuming. Alternately, all four million addresses could be street-level geocoded which would eliminate the need for the extra join and might save execution time.
|Integrate|Generate block ranges, block anchor points, address access points, and address parcel ids.| The current Block Assignment and Address Range Generator (BAARG) is written in Java and works very well. It just needs to be enhanced to better detect bad parcel points and its execution coordinated by a capable orchestration tool.
||A candidate reference address should be rejected if:<br>* the given address location is too far from the correct blockface on the given street in the reference road network.<br>* The access line (e.g., the straight line between a site's location and its access point) crosses one or more non-strata roads.<br>* the address is a duplicate of an address that came from a higher-ranked data source.||Data sources are ranked as follows:<br>1. AddressBC<br>2. Open data munis<br>3. BC Assessment<br>
||After reference addresses have been integrated, associate candidate occupants with reference addresses.<br><br>Reject occupants whose address is not a valid reference address.|To confirm validity, geocode an occupant's address using a geocoder that is loaded with the latest reference road network and reference addresses.<br><br>With the appropriate orchestration tool, an isolated batch geocoder can be preconfigured, deployed and loaded with the latest reference address data.
||Also generate locality aliases, qualified locality names from official populated place names, street name indexes, and other indexes necessary for speedy address matching|
||All rejected addresses and occupants are saved for QA analysis by the appropriate data authorities.|
|Verify|Verify that the new reference address dataset is globally valid.|With the appropriate orchestration tool, an isolated batch geocoder can be preconfigured, deployed and loaded with the appropriate candidate address data for verification.
||Globally valid means the dataset is <br> * locality-complete (e.g. has addresses from every locality) <br> * match-correct (e.g., all test addresses geocode as expected) <br> * spatially-consistent (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction), and <br>  * version-consistent (e.g. locality address counts are higher than the previous version of reference data)|
||A data administrator should review the validation results before deployment of any new data to production
Deploy| If validation is successful, make new reference road network and address list accessible to online and batch geocoders|This process can be automated but should be manually initiated.

<a name=weaknesses></a>
### Weaknesses of current implementation?

The current implementation of the geocoder data integration process needs a dedicated, standalone, batch geocoder that must be loaded with reference data three times during the integration process as follows:

* Load 1 requires the latest road network and no addresses to confirm candidate reference addresses have valid streets within localities.
* Load 2 requires the latest road network and new reference addresses to confirm candidate reference occupants have valid addresses.
* Load 3 requires the latest road network and new reference addresses and occupants to confirm correct handling of test addresses.

The geocoder is written in Java. 

The current implementation also needs a standalone Java application which handles address block assignment and range generation and is appropriately named the *Block  Assignment and Address Range Generator* (BAARG).

In the new implementation, all integration and verification steps will be moved from separate FME scripts that call out to the batch geocoder, to a single Java application called Geocodable BC Maker which will have an embedded geocoder. Geocodable BC Maker will also incorporate an enhanced version of the BAARG. This simplifies the data integration architecture by eliminating the need for an external batch geocoder, speeds up the integration process, localizes all integration algorithms into a single component for easier understanding and maintenance, and leaves the task of keeping up with constantly changing data source schemas and formats to easily-updated scripts.

It will be a major challenge to design a parallel architecture for the embedded geocoder so it can process six million addresses (two million reference addresses three times) as fast as the current batch geocoder (e.g., one hour at six million per hour).

<br><br>

<a name=data-flow></a>  
## Data flow diagrams

### Gather and Transform Steps
Here is the data flow for the current implementation of the gather and transform steps.

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/a714c132-e764-41c7-983c-67fa952465c0)

<br><br>

### Current implementation of Geocoder data integration process

#### Data Flow 1: Shared Geocoder and Route Planner data integration

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/c875e03b-e01d-4244-948d-f60edb9a948b)
<br><br>

#### Data Flow 2 - Geocoder data integration

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/e9202169-36b8-4748-acf8-eef1555fbb72)
<br><br>

#### Data Flow 3 - Route Planner data integration

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/f6e0cc51-366d-4847-9373-f47189aed3d8)
<br><br>

<a name=activity></a>
## Activity diagram

Here is the activity diagram of the current implementation

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/d19ba328-5612-404b-a364-6b0216817c9a)
<br><br><br>

<a name=architecture></a>
## Architecture Diagrams

The current implementation is on the left; one possible implementation on the right:

![image.png](https://images.zenhubusercontent.com/57a52ca5e40e5714b16d039c/082b65d9-cc15-43af-9b39-dd0dc6b68215)

<br><br>


