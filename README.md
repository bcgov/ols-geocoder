# BC Address Geocoder
The BC Address Geocoder provides address cleaning and standardization, correction, completion, geocoding, and reverse geocoding for government and the public at large. To see it in use by an application, visit [Location Services in Action](https://ols-demo.apps.gov.bc.ca/index.html).

Geocoding turns a physical address into a form of spatial data called a point that can be used to display the location of the address on a map or ask spatial questions about the address such as:
   - how close is the nearest school, firehall, hospital, or swimming pool?
   - how far is the nearest garbage dump or sewer outfall?
   - how far is a particular shopping centre, how long does it take to drive there, and what's the best route to take?
   
Reverse geocoding finds the nearest address to a given point. Address cleaning and standardization allow you to deduplicate your data and link data records that have nothing in common but address.
Established in 2013, the BC Address Geocoder is used by over two dozen government ministries and agencies in dozens of government digital services. As of March 2019, the BC Address Geocoder had geocoded over one-half a billion addresses. From January to August of 2019, the Geocoder processed over 150 million addresses.

The BC Address Geocoder is based on authoritative address data provided by BC municipalities and BC Assessment. It also uses the BC Integrated Road Network which is the authoritative source for road network data in BC. In two independent studies, the BC Geocoder was found to be more accurate than commercial products.

The BC Address Geocoder is free and open to end-users. Geocoded addresses are covered under an open government license and their locations can be used for any legal purpose. Source code for the geocoder is in the process of being released under an Apache 2.0 license with the intent of building community to share enhancement and maintenance costs, ideas, documentation, and advocacy.

The BC Address Geocoder is registered with the [BC Data Catalogue](http://catalogue.data.gov.bc.ca/dataset/physical-address-geocoding-web-service), uses the DataBC API Gateway, and uses industry-standards for its design and specification (e.g., REST, OpenAPI) which makes it very easy for developers to use in their own digital services. The Geocoder fully complies with the [BC Physical Address and Geocoding Conceptual Model](https://www2.gov.bc.ca/assets/gov/government/services-for-government-and-broader-public-sector/information-technology-services/standards-files/physical_address_and_geocoding_standards_-_conceptual_model.pdf)

Another developer friendly feature is our [Location Services in Action](https://ols-demo.apps.gov.bc.ca/index.html) application that demonstrates the geocoder (and route planner) in action. Source code is freely available to the public on [GitHub](https://github.com/bcgov/ols-devkit/tree/gh-pages/ols-demo).

The BC Address Geocoder complies with data sovereignty policy which protects the privacy of citizens by controlling access and discretely managing user requests.
