# Geocoder Address Match Accuracy Study

Michael Ross, DataBC
April 4, 2018

# Introduction

This study was conducted to determine the address match accuracy of four popular web geocoders relative to the BC Address Geocoder. The geocoders under study include:
1. Bing Maps
2. ESRI
3. Google Maps 
4. Here

## Results Summary
Sixty civic addresses were chosen at random from the BC Address Geocoder; thirty in rural areas and thirty in urban areas. These addresses were fed into each of the four geocoders and results were tabulated and classified by match completeness (see Tables 1 and 2). 
Overall, all geocoders tested are suitable for use in urban areas and none are suitable for use in rural areas.
All geocoders did very well in urban areas. Bing and ESRI both achieved 100% full match accuracy. Google Maps and Here both achieved 97% full match accuracy.
All geocoders did very poorly in rural areas. Google achieved 10% full match accuracy, Bing and ESRI 3%, and Here 0%.

## Table 1: Urban address matches by Issue
|Geocoder Name|No match|No street|No civic num|Wrong civic number|Full match|Wrong street|Wrong locality|No locality
|---|---|---|---|---|---|---|---|---|
Bing Maps||||30 (100%)|			
ESRI Geocoder||||30 (100%)|			
Google Maps Geocoder||||29 (97%)||1 (3%)	
Here Geocoder||||29 (97%)|1 (3%)	

Table 2: Rural address matches by Issue
Geocoder	No match 	No 
street	No civic num	Wrong civic number	Full match	Wrong street	Wrong locality	No locality
Bing Maps	16
(53%)	16
(53%)	26
(87%)	2
(7%)	1
(3%)	2
(7%)	5
(17%)	16
(53%)
ESRI Geocoder	1
(3%)	9
(30%)	22
(73%)	0	1
(3%)	4
(13%)	14
(47%)	1
(3%)
Google Maps Geocoder	3
(10%)	3
(10%)	19
(63%)	1
(3%)	3
(10%)	8
(27%)	15
(50%)	3
(10%)
Here Geocoder	2
(7%)	3
(10%)	19
(63%)	1
(3%)	0	11
(37%)	26
(87%)	2
(7%)

Table 3: Civic Addresses Geocoded
Urban	Rural
10400 Algonquin Dr, Richmond, BC	1312 Meadow Lane, Savary Island, BC
1125 Seafield Cres, Nanaimo, BC	14022 203 Rd, Goodlow, BC
1471 Driftwood Cres, Smithers, BC	1800 Demorest Rd Spallumcheen, BC
1590 Cliffe Ave, Courtenay, BC	18370 Viewpoint Rd, Mount Robson, BC
20461 Deniza Ave, Maple Ridge, BC	19670 E Fritton Rd, Kelly Lake, BC
2200 Willis Rd, Campbell River, BC	21239 Milligan Creek Rd Peejay, BC
231 Heron Rd, Prince Rupert, BC	27 Kelly Lake Rd, Kelly Lake, BC
679 Victoria Dr, Penticton, BC	2850 Likely Rd, 150 Mile House, BC
2725 E 48th Ave, Vancouver, BC	3047 Reeves Rd, Donald, BC
2819 Fairlane St, Abbotsford, BC	3630 Sharptail Rd, Jesmond, BC
301 9th Ave S, Cranbrook, BC	3712 Skookumchuck River Rd, Skookumchuck, BC
313 Edward St, Victoria, BC	3720 Gavin Lake Rd, Big Lake Ranch, BC
3199 Beach Ave, Roberts Creek, BC	385 Alaska Hwy, Tetsa River, BC
3725 Thomas St, Terrace, BC	397 Alaska Hwy, Stone Mountain Park, BC
393 George St, Prince George, BC	4120 201 Rd, Doe River, BC
428 Hickey Dr, Coquitlam, BC	43031 Hwy 16 W, Rose Lake, BC
433 Queensway, Prince George, BC	4400 Dog Creek Rd, Alkali Lake, BC
528 3rd Ave, Castlegar, BC	5410 Hwy 93, Roosville, BC
660 Southborough Dr, West Vancouver, BC	5495 Flathead-Lower Sage FSR, Flathead,BC
709 Vernon St, Nelson, BC	5550 Hwy 93, Roosville, BC
730 11 St SE, Salmon Arm, BC	6 3rd Ave, Holberg, BC
7304 Buller Ave, Burnaby, BC	6100 Hwy 5, Blue River, BC
824 Farrell Rd, Revelstoke, BC	737 Fording Hwy, Elkford, BC
8782 Chilliwack Mountain Rd, Chilliwack, BC	7762 Orchard Rd, Robson, BC
907 7th Ave N, Creston, BC	8395 Kinney Lake Rd, Mount Robson, BC
917 Douglas St, Kamloops, BC	9011 Airport Dr, Fort Nelson, BC
924 Glen St, Kelowna, BC	9137 Planet Mine Rd, Quilchena, BC
950 munro St, Kamloops, BC	4637 Kispiox Valley Rd, Kispiox Valley, BC
951 Beach Dr, Oak Bay, BC	100 Little Nimmo Bay, Sullivan Bay, BC
9939 158A St, Surrey, BC	2673 K P Rd, Vavenby, BC


 

Methodology
The set of random addresses generated in the BC StreetMap Comparison were used in this study. For completeness, the following defines how to generate new sets of random addresses suitable for this comparison.
 
Creating sets of random urban and rural addresses
The Location Services in Action (LSIA) application was used to generate sample addresses and to analyse all street maps. The app runs in any HTML5-compatible browser and is located at:
https://ols-demo.apps.gov.bc.ca/index.html
Two sets of samples of the BC Address Geocoder civic addresses were taken:
1.	Thirty urban street addresses
2.	Thirty rural street addresses
Urban street address samples were taken as follows:
1.	Select thirty urban localities at random.
2.	For each urban locality, repeat steps 3 to 6.
3.	Enter locality into address tab of Location Services demo app then click search icon. 
4.	Zoom out until full extent of urban core is visible.
5.	Click Jump at Random icon and system will select and display a civic address chosen randomly from current map extent.
6.	Record civic address in an urban addresses spreadsheet.
Rural street address samples were taken as follows:
1.	Zoom map to full provincial extent
2.	In address tab, click Jump at Random icon. The system will select and display a civic address chosen randomly from the current map extent.
3.	Record civic address in a rural addresses spreadsheet.
4.	Repeat steps 1 to 4 until 30 civic addresses have been found.

Evaluating geocoder results
1.	Open an HTML5 compatible web browser.
2.	Open each geocoder in a separate tab. 
a.	Bing Maps is located at https://www.bing.com/maps
b.	ESRI geocoder is located at https://governmentofbc.maps.arcgis.com/home/webmap/viewer.html?webmap=b8ea19982bd74db3bd968d3c7f038e43
c.	Google Maps is located at https://www.google.ca/maps
d.	Here is located at: https://wego.here.com
e.	The BC Geocoder is not being evaluated but can be used for QA purposes and can be found in the Location Services in Action application located at: https://ols-demo.apps.gov.bc.ca/index.html?
3.	For each sample civic address and for each geocoder, do the following:
4.	Select the tab of the appropriate geocoder (e.g., Bing ESRI, Google, Here).
5.	Geocode the civic address and assess the quality of the response using the following criteria:
a.	No match - no address returned; no match also means no street, civic number, etc
b.	No street - no address returned or address returned had no street
c.	No civic number - no address returned or address returned had no civic number
d.	Wrong civic number
e.	Full match
f.	Wrong street 
g.	Wrong locality
h.	No locality - no address returned or address returned had no locality
Ignore any additional administration areas such as regional district or community.
