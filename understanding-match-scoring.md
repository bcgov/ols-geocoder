Hi All,
Thanks for agreeing to participate in the Geocoder 4.1 meeting next week. One of the main objectives of Geocoder 4.1 is to improve address match accuracy. To understand this objective, let's take a closer look at the design of the geocoder's address match scoring system.  

An effective address match scoring system should have the following properties:

P1. It should distinguish between good and bad matches without ambiguity

P2. It should be make a minimum of classification errors (e.g., false negatives or false positives)

P3. It should rank good and bad matches (e.g., a good match may be perfect, very good, or good; a bad match may be no match, very bad, or bad)

P4. It should provide enough information  to diagnose and fix the data issues detected by the geocoder (e.g., there is no such locality as Pimbroccolli)

P5. Handle multiple types of input addresses including civic address, non-civic address, and partial addresses (e.g., street-level, locality-level).


The BC Geocoder scoring system has the following properties:

1.	There is a numeric score between 1 and 100 that is computed for each address match. Addresses with a score of 90 or higher are good matches; addresses below 90 are bad matches [P1].

2.	Preliminary analysis of misclassification rates (e.g., false positives and false negatives) suggest there are too many false negatives and some false positives [P2]. 

3.	For good matches, a score of 100 indicates a perfect match, a score of 95-99 indicates a very good match and a score of 90-95 indicates a good match. For bad matches, a score of 1 indicates no match, a score of 2-79 indicates a very bad match, and a score of 80-89 indicates a bad match [P3].

4.	A list of faults is returned indicating the mismatches encountered by the geocoder while trying to match the input address to a reference address [P4]. Each fault includes the type of fault and a penalty but doesn’t include the object of the fault (e.g., the fault message says “unrecognized word” but doesn’t include the word the geocoder couldn’t recognize)

5.	The geocoder scoring system handles multiple types of addresses through the match precision concept. Match precision is the level the match achieved. Levels include province-level (e.g., BC), locality-level (e.g., Salmon Arm, BC),  street-level (e.g., Esquimalt Rd),  block-level (e.g., couldn’t find 832 Esquimalt Rd but found an 800-block of Esquimalt Rd),  civic number level (e.g., found 840 Esquimalt Rd), and unit number level (e.g., found (Unit 201 – 840 Esquimalt Rd). The scoring system assigns an initial score according to the match precision (e.g., 100 for civic number, 99 for block, 68 for street, 48 for locality, 1 for province). It then subtracts the penalties of all the faults encountered to arrive at a final score. Here are some examples (assume no faults):

Input Address                                                              Match precision                             Score
              BC                                                                                  Province                           1
              Victoria, BC                                                                  Locality                             68
              Esquimalt Rd, Esquimalt , BC                                    Street                                78
               832 Esquimalt Rd, Esquimalt, BC                            Block                                 99
               840 Esquimalt Rd, Esquimalt, BC                            Civic-number                   100
              Unit 201 – 840 Esquimalt Rd, Esquimalt, BC         Unit-number                   100

A civic address has a civic number and may also have a unit number. A perfect civic address match is assigned a match precision of civic-number or unit-number depending on whether or not the reference civic address includes a unit number. If an input civic address includes a unit number and the matching reference address doesn’t have it, a unit.notFound fault with a 1 point penalty and net score of 99 will be returned.

              A non-civic address has a site name, an optional street, a locality, and a province. It also has its own match precision of Site. Here is an example:

Input Address                                                              Match precision                             Score
              Ksan Historical Village – Hazelton, BC                    Site                                    100

              
Our analysis tasks
===============

We have completed our analysis of 40,000 HealthIdeas addresses that scored below 80 (very bad matches and no matches) and will present our interim findings in our first consultation meeting. There are an additional 260,000 addresses with a score between 80 and 89 that we hope to analyse by the end of August. Our findings will reveal common errors in both input address data and geocoder parsing, and we will have recommendations on both improving the geocoder and improving your address ETL.

Understanding misclassification rates better will be the focus of our analysis work with an eye on reducing the number of false negatives (e.g., Central Building 620 View St, Victoria, BC gets rejected because the geocoder stumbles on the building name instead of just ignoring it).

We will use the following metric to measure match accuracy improvement:

Given a list of addresses, the match accuracy of geocoding this list is:

         N/C

where N is the number of address with a score of 90 or higher
             C is the number of addresses in the list

