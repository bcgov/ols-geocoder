# Geocoder V4.1 Product Development Plan
Last updated November 3, 2020 by MR

Task|Status|Details
|---|---|---|
|Add missing abbreviations and non-adjacent locality aliases to geocoder configuration data|In review|
|Generate locality aliases for all adjacent localities|In review|
| Add address data from other sources such as address ranges from ITN|In Review|
|Add verified addresses from our major clients to the DataBC Address Exception List|Not started
|Request simple changes to an individual client’s ETL where practical such as eliminating exact duplicates|Not started
|Devise and suggest potential improvements to our clients’ workflows|In progress|Suggested that Health Ideas re-geocode failed addresses periodically to take advantage of continuous improvements in address and road network data
|||Potentially accept locality-level matches if locality has only one post-office
|Consult with clients to better understand their geocoding needs|In progress|Conducted consultations with MoH, MCFD, Vital Stats, and BC EHS; ongoing follow-up discussions
|Provide client training|In progress|
|Improve geocoder documentation|In progress|
|Improve location of rural localities|In progress|MOH requested moving point of a small town to its post office to improve CHSA resolution
|Improve representation of first nations addresses in Geocoder|In progress| All BCGNIS localities that are not DRA localities are now modelled as sites within a DRA locality. This allows numbered houses on roads that are unnamed or unknown to the ITN to be easily represented. An example is House 21, Akisqnuk Reserve – Windermere, BC. MCFD has some of these types of address. The Provincial Toponymist approved this model in October, 2020.
|Look for common error patterns in rejected addresses|In progress
|Enhance geocoder parser to solve the common address problems identified by MCFD, MoH, and our Rejected Address Analysis|In review|The address:<br>10 main st blah blah pimbreton bc<br> should be cleaned to:<br> 10 Main St, Mount Currie, BC<br><br> https://ssl.refractions.net/ols/pub/geocoder/addresses.html?maxResults=10&echo=true&brief=true&addressString=10+main+st+blah+blah+pimbreton+bc
||Improve noise immunity| 1175 Douglas St back alley Victoria BC; back alley is where much garbage is found so we taught v4.1 how to collect the garbage
||Multiple spelling mistakes such as Omenica/Omineca|
||In review|Glued/separated words as in WildRose/Wild Rose
||In review|Locality hopping when a civic number is not in any block range
||In review|Enhancing the handling of additional postal elements
||In progress|Enhanced handling of c/o elements|In progress
||In progress|Enhanced handling of site/occupant names in the address
||In progress|Removal of duplicate address elements especially locality
||In progress|Adjust geocoder match scoring system to more accurately reflect address match accuracy
||Not started|Suffixes not matching as in George/Prince George
|Improve fault traceability|In review|Can now see value of address element at fault (e.g., Roseway is an unknown streetType)
|Disambiguate locality-level bianyms|In review|An example of a bianym is Mill Bay on the Malahat and Mill Bay near Gincolith; requested by ICBC in June, 2020; proposed solution approved by Provincial Toponymist in Oct, 2020
|Add support for named highway and freeway exits|In review|An example is Hwy 1 and Exit 366; requested by WildFire in 2018
