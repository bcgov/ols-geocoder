# Geocoder V4.1 Product Development Plan
Last updated November 3, 2020 by MR

1. Add missing abbreviations and non-adjacent locality aliases to geocoder configuration data. (In QA)

1. Generate locality aliases for all adjacent localities. (In QA)

1. Add address data from other sources such as address ranges from ITN (In QA)

1. Add verified addresses from our major clients to the DataBC Address Exception List (Not started)

1. Request simple changes to an individual client’s ETL where practical such as eliminating exact duplicates (Not started)

1. Devise and suggest potential improvements to our clients’ workflows (In progress)
- examples include re-geocoding failed addresses periodically to take advantage of continuous improvements in address and road network data; accept locality-level matches if locality has only one post-office; provide streetAddress + locality to the geocoder instead of a single address string)

1. Continue to consult with clients to better understand their geocoding needs (In progress)

1. Provide client training (In progress)

1. Improve geocoder documentation (In progresS)

1. Improve location of rural localities (e.g., move point to post office; requested by MoH to improve CHSA resolution).

1. Improve representation of first nations addresses in Geocoder. For example, we could  model a reserve with a known location but without DRA roads as a single complex with multiple houses (e.g., House 21, Akisqnuk Reserve – Windermere, BC).

1. Continue looking through current MoH and MCFD rejected addresses for common error patterns.

1. Enhance geocoder parser to solve the common address problems identified by MCFD, MoH, and our Rejected Address Analysis including:

   * Multiple spelling mistakes(e.g., Omenica/Omineca)
  
   * Glued words (e.g., WildRose/Wild Rose)
  
   * Suffixes not matching (e.g., George/Prince George)
  
   * Locality hopping when a civic number is not in any block range
  
   * Enhancing the handling of additional postal elements
  
   * Enhancing the handling of site/occupant names in the address
  
   * Removal of duplicate address elements especially locality.

1. Adjust geocoder match scoring system to more accurately score matches.
