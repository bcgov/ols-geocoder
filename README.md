# BC Address Geocoder
The BC Address Geocoder provides address cleaning and standardization, correction, completion, geocoding, and reverse geocoding for government and the public at large. To see it in use by an application, visit [Location Services in Action](https://ols-demo.apps.gov.bc.ca/index.html).


For more information about using the BC Address Geocoder and incorporating it into your automated workflows, please consult the following documents:

Document|Description|Audience
|---:|---|---|
[License](https://github.com/bcgov/ols-geocoder/blob/gh-pages/LICENSE)|License under which all documents and source code in this repository are released under|All
[Copyright Notices](https://github.com/bcgov/ols-geocoder/blob/gh-pages/notice.md)|Copyright notices of all software packaged used by this repository|All
[Geocoder Developer Guide](https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-developer-guide.md)|Learn the online geocoder API through series of examples|App developers
[Single-line Address Format](https://github.com/bcgov/ols-geocoder/blob/gh-pages/singleLineAddressFormat.md)|The single-string format of addresses supported by the geocoder|Geocoder clients, app developers, address data suppliers
[Glossary of Geocoder Terms](https://github.com/bcgov/ols-geocoder/blob/gh-pages/glossary.md)|Defines all technical terms used by the geocoder|geocoder clients, app developers, address data suppliers|
[Understanding Address Match Scoring](https://github.com/bcgov/ols-geocoder/blob/gh-pages/understanding-match-scoring.md)|Explains how geocoder matches are ranked by the geocoder;|Geocoder clients, app developers, address data suppliers
[Geocoder Address Match Scoring Reference](https://github.com/bcgov/ols-geocoder/blob/gh-pages/faults.md)|Defines how addresses matches are ranked by the geocoder. A companion document to [Understanding Address Match Scoring](https://github.com/bcgov/ols-geocoder/blob/gh-pages/understanding-match-scoring.md)|Geocoder clients, app developers, address data suppliers
[Batch Geocoder User Acceptance Test Plan](https://github.com/bcgov/ols-geocoder/blob/gh-pages/batch-geocoder-uat.md)|Plan for user acceptance testing of new versions of the geocoder|Batch geocoder clients
[Examples of addresses Geocoder 4.0 can't handle well](https://github.com/bcgov/ols-geocoder/blob/gh-pages/rejected-addresses.md)|Common types of bad addresses that geocoder 4.1 doesn't handle well. They were discovered by analysing some of the biggest address lists in government|Geocoder clients, app developers
[Geocoder 4.1 Product Development Plan](https://github.com/bcgov/ols-geocoder/blob/gh-pages/geocoder-4.1-development-plan.md)| List of tasks and their status in support of geocoder 4.1 development|Geocoder clients, app developers
[Addresses for geocoder feature and configuration data testing](https://github.com/bcgov/ols-geocoder/blob/gh-pages/atp_addresses.csv)|List of test addresses for QA of new versions of the geocoder|Geocoder clients, app developers
[Addresses for verifying presence of addresses from every locality in BC](https://github.com/bcgov/ols-geocoder/blob/gh-pages/sites_bc.csv)|One address from every locality in BC to be used in verification of latest release of Geocodable BC which is the reference data the geocoder will use|BC Geocoder administrators, geocoder prospects  
[Geocoder configuration files](https://github.com/bcgov/ols-geocoder/tree/gh-pages/config/bc)|Configuration files for the BC deployment of the geocoder including abbreviations used in street and site names, unit designators, locality aliases, match fault values, match precision values, and global parameter defaults. Abbreviations and unit designators are a super-set of those found in the Canada Post Mailing Address Standard|Geocoder developers and operators, other Canadian jurisdictions interested in adopting the geocoder
[BC Physical Address Exchange Schema](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md)|Define a schema for the exchange of reference addresses between address authorities and geocoding service providers|Address authorities, app developers, geocoder developers and operators
[Draft BC Physical Address Conceptual Model v2](https://github.com/bcgov/ols-geocoder/blob/gh-pages/physical-address-conceptual-model.md)|Defines what a physical address is and how it differs from a mailing address|Address authorities, address data suppliers, app developers, geocoder clients
[Geocoder Data Integration Pipeline Architecture](https://github.com/bcgov/ols-geocoder/blob/gh-pages/address-data-pipeline.md)|An overview of the geocoder reference data integration pipeline|Geocoder developers
[OpenStreetMap Suitability Study](https://github.com/bcgov/ols-geocoder/blob/gh-pages/osm-suitability.md)|Is OSM a suitable base map to display addresses from the BC Address Geocoder?|App developers
[OpenStreetMap Suitability Addresses](https://github.com/bcgov/ols-geocoder/blob/gh-pages/itn-osm-comparison.csv)|Random addresses selected for use in the OSM suitability study|App developers
[Geocoder Roadmap](https://github.com/bcgov/ols-geocoder/blob/gh-pages/roadmap.md)|Planned major enhancements to the geocoder|Everyone
[Geocoder Standards Roadmap](https://github.com/bcgov/ols-geocoder/blob/gh-pages/standards-roadmap.md)||Data administrators, geocoder clients,app developers,address authorities, address data suppliers
