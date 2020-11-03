# Geocoder V4.1 Development Plan
Last updated November 3, 2020

1.	Add missing abbreviations and non-adjacent locality aliases to geocoder configuration data.

2.	Generate locality aliases for all adjacent localities. 

3.	Add address data from other sources (e.g., missing address ranges from DRA)

4.	Add verified addresses from our major clients to the DataBC Address Exception List (e.g., House 900 -- Malachan Reserve – Ditidaht,BC; Malachan Reserve – Ditidaht, BC; SAULTEAU RESERVE -- MOBERLY LAKE, BC)

5.	Request simple changes to an individual client’s ETL where practical (e.g., eliminating exact duplicates)

6.	Devise and suggest potential improvements to our clients’ workflows (e.g., re-geocode failed addresses periodically to take advantage of continuous improvements in address and road network data; accept locality-level matches if locality has only one post-office; provide streetAddress + locality to the geocoder instead of a single address string)

7.	Continue to consult with clients to better understand their geocoding needs.

8.	Provide client training.

9.	Improve geocoder documentation (e.g., new geocoder address scoring guide and reference done)

10.	Improve location of rural localities (e.g., move point to post office; requested by MoH to improve CHSA resolution).

11.	Improve representation of first nations addresses in Geocoder. For example, we could  model a reserve with a known location but without DRA roads as a single complex with multiple houses (e.g., House 21, Akisqnuk Reserve – Windermere, BC).

12.	Continue looking through current MoH and MCFD rejected addresses for common error patterns.

13.	Enhance geocoder parser to solve the common address problems identified by MCFD, MoH, and our Rejected Address Analysis including:

a.	Multiple spelling mistakes(e.g., Omenica/Omineca)
b.	Glued words (e.g., WildRose/Wild Rose)
c.	Suffixes not matching (e.g., George/Prince George)
d.	Locality hopping when a civic number is not in any block range
e.	Enhancing the handling of additional postal elements
f.	Enhancing the handling of site/occupant names in the address
g.	Removal of duplicate address elements especially locality 

14. Adjust geocoder match scoring system to more accurately score matches.
