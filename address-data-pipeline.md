# Geocoder Data Integration Pipeline Architecture
The Geocoder Data Integration Pipeline takes road data from GeoBC and address data from various authoritative sources across BC and produces a master list of addresses in a form and file location that is easily consumable by the OLS-Geocoder. 

Here are the processing stages of the pipeline:

Stage name|Description|Implementation
|--|--|--|
|Gather|Gather reference road and address data from authoritative sources. Each source dataset may have a different access method (e.g. download, API), data schema, and update schedule|Manual download
|Transform|Transform each reference address dataset into a single, standard format called Physical Address Exchange (PAX) Format|One ETL script (in any language) for each source data format (e.g., AddressBC, BC Assessment)
|Integrate| Integrate reference addresses into reference road segments (blocks)|Java app called Geocodable BC Maker|
|Verify|Verify that the new reference address dataset is globally valid. Globally valid means the dataset is <br> * complete (e.g. has addresses from every locality) <br> * correct (e.g., every reference address geocodes perfectly and all test addresses geocode as expected) <br> * spatially-consistent (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction), and <br>
* version-consistent (e.g. locality address counts are higher than the previous version of reference data)|Geocodable BC Maker| 
Deploy| Make new reference road network and address list accessible to online and batch geocoders and restart them|manually trigger online geocoder restart script and restart batch geocoder using  batch geocoder admin application


## Acquisition
In this initial stage, addresses from multiple sources are validated against their own schemas and transformed into a common schema called the OLS-Geocoder integration schema. Every attempt is made to repair source data to fit their own schemas without violating semantics (e.g., common misspellings of domains are corrected).

There are several data sources and each one has a defined schema that is approved by the source data custodian. 
   - all data sources transformed into standard OLS-Geocoder Reference Address Format 
   - data source ranking from highest to lowest is as follows:
      1. AddressBC
          - address parcel points plus anchor points (from unsigned munis)
      2. Open Address Data
          - address parcel points
      3. BCA
         -  derivation of parcel points from ParcelMap BC and address=>jurol=>pid table
      4. StatsCanRNF
          - anchor points only 
 

derivation of access points, address ranges
