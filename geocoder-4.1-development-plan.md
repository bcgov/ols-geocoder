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
|Consult with clients to better understand their geocoding needs|In progress|
|Provide client training|In progress|
|Improve geocoder documentation|In progress|
|Improve location of rural localities|In progress|MOH requested moving point of a small town to its post office to improve CHSA resolution
|Improve representation of first nations addresses in Geocoder|In progress| For example, we could  model a reserve with a known location but without DRA roads as a single complex with multiple houses (e.g., House 21, Akisqnuk Reserve – Windermere, BC).
|Look for common error patterns in rejected addresses|In progress
|Enhance geocoder parser to solve the common address problems identified by MCFD, MoH, and our Rejected Address Analysis|In review|Multiple spelling mistakes such as Omenica/Omineca
||In review|Glued/separated words as in WildRose/Wild Rose
||In review|Locality hopping when a civic number is not in any block range
||In review|Enhancing the handling of additional postal elements
||In progress|Enhanced handling of c/o elements|In progress
||In progress|Enhanced handling of site/occupant names in the address
||In progress|Removal of duplicate address elements especially locality
||In progress|Adjust geocoder match scoring system to more accurately score matches
||Not started|Suffixes not matching as in George/Prince George
