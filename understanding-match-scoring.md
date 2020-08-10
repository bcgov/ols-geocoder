# Understanding Address Match Scoring

Understanding address match scoring will help you understand why a given address was rated good or bad by the geocoder and where you should best spend your time improving the overall match accuracy of your address list to get the most business value for your clients.
 
## Properties of an effective address match scoring system

An effective address match scoring system should have the following properties:

P1. It should distinguish between good and bad matches without ambiguity

P2. It should be make a minimum of classification errors (e.g., false negatives or false positives)

P3. It should rank good and bad matches (e.g., a good match may be perfect, very good, or good; a bad match may be no match, very bad, or bad)

P4. It should provide enough information  to diagnose and fix the data issues detected by the geocoder (e.g., there is no such locality as Pimbroccolli)

P5. Handle multiple types of input addresses including civic address, non-civic address, and partial addresses (e.g., street-level, locality-level).

## How the BC Address Geocoder measures up

The BC Geocoder scoring system has the following properties:

1.	There is a numeric score between 1 and 100 that is computed for each address match. Addresses with a score of 90 or higher are good matches; addresses below 90 are bad matches [P1].

2.	Preliminary analysis of misclassification rates (e.g., false positives and false negatives) suggest there are too many false negatives and some false positives [P2]. 

3.	For good matches, a score of 100 indicates a perfect match, a score of 95-99 indicates a very good match and a score of 90-95 indicates a good match. For bad matches, a score of 1 indicates no match, a score of 2-79 indicates a very bad match, and a score of 80-89 indicates a bad match [P3].

4.	A list of faults is returned indicating the mismatches encountered by the geocoder while trying to match the input address to a reference address [P4]. Each fault includes the type of fault and a penalty but doesn’t currently include the object of the fault (e.g., the fault message says “unrecognized word” but doesn’t include the word the geocoder couldn’t recognize)

5.	The geocoder scoring system handles multiple types of addresses through the match precision concept. Match precision is the level the match achieved. Levels include province-level (e.g., BC), locality-level (e.g., Salmon Arm, BC),  street-level (e.g., Esquimalt Rd),  block-level (e.g., couldn’t find 832 Esquimalt Rd but found an 800-block of Esquimalt Rd),  civic number level (e.g., found 840 Esquimalt Rd), and unit number level (e.g., found (Unit 201 – 840 Esquimalt Rd). The scoring system assigns an initial score according to the match precision (e.g., 100 for civic number, 99 for block, 68 for street, 48 for locality, 1 for province). It then subtracts the penalties of all the faults encountered to arrive at a final score.

### Scoring Civic and Non-civic addresses

A civic address has a civic number and may also have a unit number. A perfect civic address match is assigned a match precision of civic-number or unit-number depending on whether or not the reference civic address includes a unit number. If an input civic address includes a unit number and the matching reference address doesn’t have it, a unit.notFound fault with a 1 point penalty and net score of 99 will be returned.

A non-civic address has a site name, an optional street, a locality, and a province. It also has its own match precision of Site.


### Examples of matches with diferent match precisions

Assuming no faults, here is how addresses containing various match precisions are scored:

Input Address|Match Precision|Score
--------:|-------|------:
BC|Province|1
Victoria, BC|Locality|68
Esquimalt Rd, Esquimalt,BC|Street|78
832 Esquimalt Rd, Esquimalt, BC|Block|99
840 Esquimalt Rd, Esquimalt, BC|Civic Number|100
Unit 201 -- 840 Esquimalt Rd, Esquimalt,BC|Unit Number|100
Ksan Historical Village -- Hazelton, BC|Site|100

<br><br>
              
## How to improve the match accuracy of your address list

The first step to improving the address match accuracy of your address list is to measure its current accuracy by running it through the BC Address Geocoder and performing the following calculation:

Address List Match Accuracy = number of addresses with a score of 90 or higher divided by the number of addresses in your address list

The score of 90 is the minimum score of a good match.

Once you have measured the initial match accuracy, use the match precision and the list of faults in the geocoder output to analyse the misclassification rates with an eye on reducing the number of false negatives (e.g., Central Building 620 View St, Victoria, BC gets rejected because it is missing the front-gate delimiter ("--"). For more detailed information on faults, see the [BC Address Geocoder Match Scoring Reference](https://github.com/bcgov/ols-geocoder/blob/gh-pages/faults.md). For examples of faulty addresses and their root causes, see [Understanding Batch Geocoder Output](https://www2.gov.bc.ca/assets/gov/data/geographic/location-services/geocoder/understanding_geocoder_results.pdf). 

Once you have the misclassification rates, you can determine what combination of address ETL improvement and manual editing gives you the best return on your effort. You should also try to set an accuracy improvement goal. As you work through your address list, keep measuring match accuracy to measure progress toward your accuracy improvement goal.
