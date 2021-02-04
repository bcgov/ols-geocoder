# Geocoder V4.1 Product Development Plan
Last updated November 3, 2020 by MR

Task|Status|Details
|-----------|---|---|
|Generate locality aliases for all adjacent localities|Done| Data integration process extended to generate and include aliases for all adjacent localities
|Generate street aliases for glued and unglued street names|Failed|Data integration process extended. For example, Hill Side Ave becomes an alias of Hillside Ave
|Generate street aliases for streets that have a directional word in their name|Done|Data integration process extended. For example, N Park St becomes an alias of North Park St
|Generate street aliases for numbered streets that have gaps in their names|Done|For example, 1200 102 A St, Surrey, BC becomes an alias for 1200 102A St, Surrey, BC
|Add address data from other sources such as address ranges from ITN|Done| Data integration process extended to include all ITN address ranges
|Add non-adjacent locality aliases identified in rejected address analysis to geocoder configuration data|Done|Non-adjacent aliases such as Victoria to Willis Point added to geocoder configuration data
|Add abbreviations identified in rejected address analysis to geocoder configuration data|Done|Abbreviations added to geocoder configuration data
|Add verified addresses from our major clients to the DataBC Address Exception List|Not started| Don't expect to start until 4.1 released and clients trained in preparing reference addresses
|Request simple changes to an individual client’s ETL where practical such as eliminating exact duplicates|Not started| Can't start until 4.1 complete.
|Devise and suggest potential improvements to our clients’ workflows|Done|Suggested that Health Ideas re-geocode failed addresses periodically to take advantage of continuous improvements in address and road network data
|||Potentially accept locality-level matches if locality has only one post-office. Provincial toponymist willing to work with us
|Consult with clients to better understand their geocoding needs|In progress|Had follow-up discussions with MoH, MCFD on providing CHSA of an address; all agreed it would be useful to business for analysis, reporting, and planning
|||Had chat with BC EHS and they also need address CHSA for analysis, reporting, and planning; also said improving indigenous health services is a priority at MoH 
|Provide client training|In progress| Ongoing one-on-one training with Health Ideas, MCFD, and Vital Stats about Geocoding scoring system and match accuracy metrics
|Improve geocoder documentation|In progress|Added geocoder-4.1-development-plan (this document), OSM base-suitability assessment, and product vision. Previously added geocoder scoring guide and reference, batch geocoder registration, batch geocoder UAT process, rejected address examples, conceptual model of addressing,  unit descriptors, and abbreviations.
|Improve location of rural localities|Not started|MOH requested moving point of a small town to its post office to improve CHSA resolution; Provincial toponymist agreed to work on this
|Improve representation of first nations addresses in Geocoder|In progress| All BCGNIS localities that are not ITN localities are now modelled as sites within a ITN locality. This allows numbered houses on roads that are unnamed or unknown to the ITN to be easily represented. An example is HOUSE 900 Malachan 11 -- Ditidaht, BC. MCFD has some of these types of address. The Provincial Toponymist approved this model in October, 2020.
|Look for common error patterns in rejected addresses|Done|Used stratified random sampling to speed up analysis of thirteen million addresses from Health Ideas Warehouse; used sampling on MCFD data and confirmed results of previous analysis but found no new patterns.
|Release plan|Done| v4.1 will have bronze, silver, and gold releases; only gold will be released to the public. Bronze will be delivered to Ministry this week.
|Enhance geocoder parser to solve the common address problems identified by MCFD, MoH, Vital Stats, and our Rejected Address Analysis|Done|The address:<br><br>10 main st back alley garbage pimbreton bc<br><br> should be cleaned and corrected to:<br><br> 10 Main St, Mount Currie, BC<br><br> This requires correcting two spelling errors, aliasing to the correct locality, and picking up the garbage. For details, visit:<br><br> https://cmhodgson.github.io/ols-devkit/ols-demo/index.html?gc=tst&q=10+main+st+back+alley+garbage+pimbreton+bc <br><br> https://geocodertst.api.gov.bc.ca/addresses.html?maxResults=5&addressString=10%20main%20st%20back%20alley%20garbage%20pimbreton%20bc <br> <br> The scoring system has been enhanced to show you the objects at fault for better traceability.
|Improve noise immunity (parser)|Done| 1175 Douglas St back alley Victoria BC; back alley (between street and locality) is where much garbage is found so we taught v4.1 how to pick up the garbage
|Multiple spelling mistakes (parser)|Done|Omenica/Omineca, Pimbreton/Pemberton|
|Locality hopping when a civic number is not in any block range (parser)|Done|
|Improve handling of postal elements (parser)|Done|
|Ignore c/o elements (parser)|Done|
|Improve handling of site/occupant names (parser)|Done|
|Removal of duplicate address elements especially locality (parser)|Done|
|Adjust geocoder match scoring system to more accurately reflect address match accuracy (parser)|Done
|Add support for suffix matching (parser)|Not done| For example match George to Prince George
|Improve fault traceability (parser)|Done|Can now see value of address element at fault (e.g., Roseway is an unknown streetType)
|Disambiguate locality-level bianyms (parser)|Done|An example of a bianym is Mill Bay on the Malahat and Mill Bay near Gincolith; requested by ICBC in June, 2020; proposed solution approved by Provincial Toponymist in Oct, 2020
|Add support for named highway and freeway exits|Done|An example is Hwy 1 and Exit 366; requested by WildFire in 2018; solved by generating appropriate street aliases
|Quality Assurance|Done|Prepared test cases (480 in total); Prepared test framework including scripts for metrics calculation and stratified random sampling. Metrics calculation will be integrated with batch list address submitter so metrics will be calculated every time a client submits a batch job. MOH, MCFD, Vital Stats, and AG agreed to participate in UAT.
