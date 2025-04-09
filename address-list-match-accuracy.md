# Address List Match Accuracy

The Address List Match Accuracy (ALMA) is calculated by dividing the number of Geocoded addresses with a score of at least 90, by the total number of addresses in your list. ALMA can be used as a measure of both the quality of addresses in your list and how well the Geocoder handles the errors.
<br><br>
The numbers below were calculated using the same test dataset for consistency to measure how the Geocoder improves over time.
<br><br>
For a summary of changes with each release please see the (What's New)[https://github.com/bcgov/ols-geocoder/blob/gh-pages/whats-new.md] page.

|Version|ALMA|Notes|
|:---|:---:|:---|
|4.02 (prod baseline)|58|Aug 2020 data vintage|
|4.02|60|Added adjacent localities as aliases, ITN address ranges, glued and unglued street names as aliases, streets with directionals in name as aliases; changed BCGNIS localities that aren't DRA localities from localities to sites within DRA localities.|
|4.1 (bronze)|70|Added abbreviations and non-adjacent locality aliases. Also includes the first attempt at removing garbage words with confidence, handling of multiple spelling errors, and the detection of more types of postal address elements.|
|4.1 (silver)|75|Removed redundant BCGNIS localities and StatsCan RNF address ranges; Added unit descriptors. Also includes better garbage word removal, handling of unitNumber stuck to unitDescriptor, handling of detached suffix in a numbered street-name, reducing locality-hopping, penalizing unmatched suffixes during autocompletion, better handling of special characters, and fine tuning the scoring system.|
|4.2|79.53|What’s new:  https://github.com/bcgov/ols-geocoder/blob/gh-pages/whats-new.md|
|4.3|79.98|What’s new:  https://github.com/bcgov/ols-geocoder/blob/gh-pages/whats-new.md|
|4.5.1|80.06|What’s new:  https://github.com/bcgov/ols-geocoder/blob/gh-pages/whats-new.md|

