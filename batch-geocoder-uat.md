# Batch Geocoder User Acceptance Testing
If you are a current batch geocoder client and we have asked you to participate in the testing of a new release, this document describes what's involved so you effectively plan and execute acceptance testing.


|Test Name|Description|
| ---- | ----|
|Data Integration Test|Confirm that the new geocoder works with your existing batch scripts
|Data Volume Test|Confirm that the new geocoder can handle you current address lists
|Address Match Accuracy Test|Measure improvement in match accuracy of new geocoder over current geocoder. Match accuracy is measured as the number of addresses with a score of 90 or higher divided by number of addresses with a score below 90. I understand that an additional, relevant accuracy measure might be the extent of the increase in the number of addresses that can be assigned a geographic location using the new geocoder.
|Parser Validation test|Confirm parser changes are working as expected. We will provide a [test case file](https://github.com/bcgov/ols-geocoder/blob/gh-pages/atp_addresses.csv) containing addresses and expected results that you can submit to the batch geocoder and analyse the results to better understand the behavior of the new parser.
