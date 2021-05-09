# Geocoder Data Integration Process (Proposed)
The Geocoder Data Integration Process takes road data from GeoBC and address data from various authoritative sources across BC and produces a reference road network and reference list of addresses in a form and file location that is easily consumable by the OLS-Geocoder.

## The new process

Here are the major stages of the new process:

Stage name|Description|Implementation
|:--:|--|--|
|Gather|Gather reference road and address data from authoritative sources.|Manual download from websites run by Integrated Cadastral Initiative Society, BC Assessment, GeoBC, and municipalities that have addresses on their open data websites but are not members of the ICI Society.
||Each source dataset may have a different access method (e.g. download, API), data schema, and update schedule|
|Transform|Transform each reference address dataset from its local schema and format to a single, standard schema and format called the [Physical Address Exchange (PAX) Standard](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md)|One ETL script (in any language) for each source data format (e.g., AddressBC, BC Assessment). We use FME for existing ETL scripts.
|Integrate|Integrate reference addresses (in PAX format) into reference road segments (blocks) and generate block ranges, block anchor points, address access points, and address parcel ids.|A standalone Java application (e.g., WAR file) called Geocodable BC Maker which contains an embedded instance of the geocoder.
||A reference address is rejected if:<br>* it doesn't conform to the PAX schema<br>* the given street doesn't exist in the given locality in the reference road network.<br>* the given address location is too far from the correct blockface on the given street in the reference road network.<br>* the address is a duplicate of an address that came from a higher-ranked data source.|The geocoder embedded in Geocodable BC Maker is configured with its own deployment of the Geocoder Admin App|Data sources are ranked as follows:<br>1. AddressBC<br>2. Open data munis<br>3. BC Assessment<br>
||Also generate locality aliases, qualified locality names from official populated place names, street name and other indexes necessary for speedy address matching|
|Verify|Verify that the new reference address dataset is globally valid.|Geocodable BC Maker| 
||Globally valid means the dataset is <br> * locality-complete (e.g. has addresses from every locality) <br> * match-correct (e.g., all test addresses geocode as expected) <br> * spatially-consistent (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction), and <br>  * version-consistent (e.g. locality address counts are higher than the previous version of reference data)|
Deploy| Make new reference road network and address list accessible to online and batch geocoder|Manually trigger online geocoder restart script and restart batch geocoder plugin in CPF using CPF admin application.

## What's different?

The current geocoder data integration process requires a dedicated, standalone, batch geocoder that must be loaded with reference road and address data three times in the course of the integration process as follows:

* Load 1 requires the latest road network and no addresses to confirm candidate reference addresses have valid streets within localities.
* Load 2 requires the latest road network and new reference addresses to confirm candidate reference occupants have valid addresses.
* Load 3 requires the latest road network and new reference addresses and occupants to confirm correct handling of test addresses.

In the new process, all integration and verification steps are moved from separate FME scripts that call out to the batch geocoder, to a single Java application called Geocodable BC Maker which has an embedded geocoder. This simplifies the data integration architecture by eliminating the need for an external batch geocoder, speeds up the integration process, localizes all integration algorithms into a single component for easier understanding and maintenance, and leaves the task of keeping up with constantly changing data source schemas and formats to easily-updated scripts

Detailed data flow diagrams of the current Geocoder data integration process are available [here](https://github.com/bcgov/ols-geocoder/issues/243)
