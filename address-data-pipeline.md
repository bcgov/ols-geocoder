# Address Data Pipeline Architecture
The address data pipeline takes data from various authoritative sources across BC and produces a master list of addresses in a form and file location that is easily consumable by the OLS-Geocoder. 

Here are the processing stages of the pipeline:

Acquisition | Integration | Verification | Deployment

## Acquisition
In this initial stage, all data sources are validated against their own schemas and transformed into a common schema called the OLS-Geocoder integration schema. Every attempt is made to repair data to fit their own schemas without violating semantics (e.g., common misspellings of domains are corrected).

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

https://2019.stateofthemap.org/attachments/W8BA9K_sl_Anderson_corparate_editing.pdf


