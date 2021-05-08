# Geocoder Data Integration Pipeline Architecture (Proposed)
The Geocoder Data Integration Pipeline takes road data from GeoBC and address data from various authoritative sources across BC and produces a reference road network and reference list of addresses in a form and file location that is easily consumable by the OLS-Geocoder. 

Here are the processing stages of the pipeline:

Stage name|Description|Implementation
|:--:|--|--|
|Gather|Gather reference road and address data from authoritative sources.|Manual download from websites run by Integrated Cadastral Initiative Society, BC Assessment, GeoBC, and municipalities that have addresses on their open data websites but are not members of the ICI Society.
||Each source dataset may have a different access method (e.g. download, API), data schema, and update schedule|
|Transform|Transform each reference address dataset from its local format to a single, standard format called Physical Address Exchange (PAX) Format|One ETL script (in any language) for each source data format (e.g., AddressBC, BC Assessment). Existing ETL scripts are written in FME.
|Integrate|Integrate reference addresses (in PAX format) into reference road segments (blocks) and generate block ranges, block anchor points, address access points, and address parcel ids.|A standalone Java application (e.g., WAR file) called Geocodable BC Maker which contains an embedded instance of the geocoder.
||A reference address is rejected if:<br>* it doesn't conform to the PAX schema<br>* the given street doesn't exist in the given locality in the reference road network.<br>* the given address location is too far from the correct blockface on the given street in the reference road network.<br>* the address is a duplicate of an address that came from a higher-ranked data source.||Data sources are ranked as follows:<br>1. AddressBC<br>2. Open data munis<br>3. BC Assessment<br>
||Also generate locality aliases, qualified locality names from official populated place names, street name and other indexes necessary for speedy address matching|
|Verify|Verify that the new reference address dataset is globally valid.|Geocodable BC Maker| 
||Globally valid means the dataset is <br> * complete (e.g. has addresses from every locality) <br> * correct (e.g., every reference address geocodes perfectly and all test addresses geocode as expected) <br> * spatially-consistent (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction), and <br>  * version-consistent (e.g. locality address counts are higher than the previous version of reference data)|
Deploy| Make new reference road network and address list accessible to online and batch geocoder|Manually trigger online geocoder restart script and restart batch geocoder plugin in CPF using CPF admin application.
