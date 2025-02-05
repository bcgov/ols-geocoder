
**Understanding Physical Address**

**Geocoder Results**

**March 21, 2014**

**Document Version 0.1**

This document explains how to use the output of the Physical Address Geocoder to improve the quality of your addresses.

The geocoder returns two quality indicators: address match score and location positional accuracy. Address match score reflects how well an input address matches an address in the geocoder’s reference list of addresses. Location positional accuracy reflects how well the geocoder knows the geographic position of a given address.

# Matching geocoder input to output

The online geocoder always shows you the input address as well as one or more result addresses. The batch geocoder results file doesn’t include input address. The fullAddress field in the results file is the standardized, corrected, and matched address, not the input address. To effectively analyse batch geocoder output, you need to open and view both input and results files at the same time. In each line of the results file there is a sequence number which represents the nth address in your input file. Remember to account for the first row being column definitions (e.g., sequence number 6091 is row 6092 in input file). If you pull up both files in MS Excel and turn on View. View Side by Side and Synchronous Scrolling, you can see both input and result addresses at the same time.

# Address match score

The geocoder determines the quality of an address match by computing a score between 0 and 100. The score is determined in two parts: match precision and match faults. A match is initially assigned a precision value then any match faults are subtracted from this amount.

The geocoder determines match precision on the address elements that matched the input address after various address corrections and standardizations are applied. A more precise match has a higher precision value.

As the geocoder corrects and standardizes the input address, it keeps track of each fault found and its associated penalty. Penalties are then subtracted from the precision value to get a match score.

**Match Score Examples**

| **Ex** | **Input address** | **Output address** | **Match Precision (value)** | **Faults (penalty)** | **Score**<br><br>**\= Match Precision - Faults** | **Explanation** |
| --- | --- | --- | --- | --- | --- | --- |
| 1   | BC  | BC  | PROVINCE (1) |     | 1   | An input of BC gets a precision of province since no other address elements were provided. |
| 2   | Baa | BC  | PROVINCE (1) |     | 1   | An input that can’t be matched at all is assigned a match precision of PROVINCE |
| 3   | kelowna bc | Kelowna, BC | LOCALITY (68) |     | 68  | An input of locality name gets a precision of LOCALITY. |
| 4   | Klowna bc | Kelowna, BC | LOCALITY (68) | Locality spelled wrong (2) | 66  | Input is spell-correctly before being successfully matched to the locality, Kelowna. |
| 5   | Mystery St, Kelowna bc | Kelowna, BC | LOCALITY (68) | Street name not matched (12) | 56  | Mystery St not found in Kelowna so it is ignored and a match precision of LOCALITY is returned. |
| 6   | St Paul St, Kelowna | St Paul St, Kelowna, BC | STREET (78) | Province missing (1) | 77  | Input address is missing a province code so the province code BC was added to the output. |
| 7   | 2000 St Paul St, Kelowna | St Paul St, Kelowna, BC | STREET (78) | Civic number not in any block (10)<br><br>Province missing (1) | 67  | There is no block of St Paul St in Kelowna that contains civic number 2000. |
| 8   | 1347 St Paul St, Kelowna, bc | 1347 St Paul St, Kelowna, BC | BLOCK (99) |     | 99  | Civic number 1347 is not matched but an address range on St Paul St that contains 1347 is matched. In this case, a match precision of BLOCK is assigned. |
| 9   | 1345 St Paul St, Kelowna | 1345 St Paul St, Kelowna, BC | CIVIC_NUMBER (100) |     | 100 | Civic number 1345 on St Paul St is matched and a match precision of CIVIC_NUMBER is assigned. This is a perfect match. |
| 10  | 1345 St Paul St, okanagan mission, bc | 1345 St Paul St, Kelowna, BC | CIVIC_NUMBER (100) | Locality is an alias (4) | 96  | St Paul St was not found in Okanagan Mission but it was found in Kelowna which is a locality alias of Okanagan Mission. |
| 11  | 1345 St Paul St, West Kelowna | 1345 St Paul St, Kelowna, BC | CIVIC_NUMBER (100) | Street direction not matched (2) | 98  | There are two plausible interpretations of the input:<br><br>St Paul St in West Kelowna<br><br>St Paul St W in Kelowna<br><br>The second interpretation gets a much higher score since there is no St Paul St in West Kelowna. St Paul St in Kelowna has no street directional, hence the fault. |

# Fixing addresses that match poorly

The quickest way to fix an input address is to assume it is wrong in some way and look for the following common problems:

1. You spelled a name wrong. For example, you entered “fairfeild” instead of ‘fairfield”, “valey” instead of “valley”, or “farfield” instead of “fairfield”. The geocoder will find and correct one such problem per word but a word with more than one spelling error will not be corrected
2. You entered a postal locality instead of the appropriate municipality or community. For example, in the input address, _1300 esquimalt rd victoria bc_, Victoria is a mailing locality for this address. The correct physical locality is the municipality of Esquimalt
3. You left a digit off a civic number and get the fault, Civic number not in any block. For example, you entered _102 Pine Springs Rd, Kamloops, BC, instead of 1026 Pine Springs Rd, Kamloops, BC_.
4. You entered an unofficial street type abbreviation (e.g., Div instead of Divers). Only Canada Post street type abbreviations are supported (see [http://www.canadapost.ca/tools/pg/manual/PGaddress-e.asp#1423617](http://www.canadapost.ca/tools/pg/manual/PGaddress-e.asp%231423617)). Try using the unabbreviated street type instead (e.g., Diversion).
5. You entered the wrong locality. For example, you entered _4705 Trans-Canada Hwy, Cowichan Bay, BC_ and only get a street-level match of _Trans-Canada Hwy, Duncan, BC_. Using the online geocoder, leave off the locality, set max results to 10 and try again. This time, we get _4705 Trans-Canada Hwy, Cowichan Bay, BC_ amongst other equally scoring matches but no _4705 Trans-Canada Hwy, Duncan, BC_. This implies the correct locality is Cowichan Bay, not Duncan since Cowichan Bay is near Duncan.

If you are still having problems, it could be because the geocoder’s reference data is incomplete or incorrect.

1. If the locality you entered is in a rural area, it could be correct and the geocoder’s reference data is wrong. The geocoder’s locality data doesn’t currently have official community names as assigned by addressing authorities (e.g., regional districts). We expect this to be fixed in fiscal 2014/15. In the meantime, use the locality assigned by the geocoder if it is actually near the locality you originally entered. The easiest way to confirm this is to use the [Physical Address Viewer in Google Earth](https://openmaps.gov.bc.ca/kml/geocoder/BCGov_Physical_Address_Viewer_Loader.kml). Turn on the layer named Geographical Names from BCGNIS, click on the layer name Find a Geographical Name in BC, enter your locality name and click Search.
2. If the address you entered includes a unit designator and unit number, leave it off and try again. Most _unit number not matched_ faults are due to lack of reference data in the geocoder. Same goes for civic number suffixes and site names.
3. If you get faults, Civic number not in any block or Locality not matched, the geocoder may have missing or incorrect address ranges. The Physical Address Viewer in Google Earth can be used to confirm this. Turn on the following layers: Site Addresses, Intersection Addresses, and Geographical Names From BCGNIS. Click on layer name Find an Address, enter your address, set Max results to 20, and press geocode. A list of matching addressing will be added to the end of Temporary Places.
4. There may be the odd incorrect street name, street type, street direction, or locality name in the geocoder reference data. Only an independent geocode or web search can confirm this.

# Hunting elusive addresses with the Physical Address Viewer

1. The batch geocoder said _615 Taku Rd, Heriot Bay, BC_ should be _615 Taku Rd, Quathiaski Cove, BC_. Is this correct?

Start up the [Physical Address Viewer in Google Earth](https://openmaps.gov.bc.ca/kml/geocoder/BCGov_Physical_Address_Viewer_Loader.kml)

![image](https://github.com/user-attachments/assets/5e587b2a-ff9c-490e-9d84-1cd135d224a6)


Click on Find Address, enter _615 Taku Rd, Heriot Bay, BC_, then press Geocode

![image](https://github.com/user-attachments/assets/1cb6968e-3fc0-4361-a301-8788efda50e5)


The geocoded location will be shown on screen as a push pin.

![image](https://github.com/user-attachments/assets/b1e0de2b-2940-4592-acf2-2aaed0fb6a0f)


Turn on Geographic Names from BCGNIS layer and zoom out until you can see Heriot Bay and Quathiaski Cove on Quadra Island.

![image](https://github.com/user-attachments/assets/ddabfa27-5a75-4d4d-b197-ea3b7508d552)


In Geographical Names from BCGNIS, turn off all names except Heriot Bay and Quathiaski Cove.

![image](https://github.com/user-attachments/assets/ca17a27c-8855-44c7-9fb4-0d842a0faf29)


It looks like the correct locality of 615 Taku Rd is Heriot Bay, not Quathiaski Cove. However, the geocoded location appears to be correct.

1. Let’s look for 145 Cow Bay Rd, Prince Rupert. The geocoder says there is no such civic number on this street:

![image](https://github.com/user-attachments/assets/a4a296e7-cc55-4582-8d38-8313c3b497d3)


Zoom out a bit, turn on Site Addresses and Intersection Addresses, and turn off all addresses except those on Cow Bay Rd.

![image](https://github.com/user-attachments/assets/6a4baffa-35ab-40cf-bff1-aa957fc69a94)


It is clear from the intersection addresses that Cow Bay Rd has three blocks, not just one. It is also clear from the site addresses that the first block has the address range 1-99. The second block is likely the 100 block, so 145 Cow Bay Rd is likely located half-way down the second block. Also, the Integrated Transportation Network, which the geocoder uses for address ranges, is missing the 100 block since 145 Cow Bay Rd returns a _civic number not in any block_ fault.

1. Let’s look for _Murtle Lake Rd and Hwy 5, Blue River, BC_. The geocoder returns BC and a score of 1, no match. Turn on Intersection addresses and Geographical Names and then find address _Murtle Lake Rd, Blue River BC_.

![image](https://github.com/user-attachments/assets/fc2f19fc-8539-4108-aaf7-2f8edc238bab)


Zoom out a bit and we see we’re near Blue River.

![image](https://github.com/user-attachments/assets/85959b6f-e90a-458a-a8c2-52148e39a9f0)


Follow Murtle Lake Rd up to Hwy 5. A closer looks reveals that Murtle Lake Rd intersects Blue River West Frontage Rd, not Hwy 5.

![image](https://github.com/user-attachments/assets/5d69a1aa-be84-49ab-9045-47f00a5bb20b)


In this case, the input address of _Murtle Lake Rd and Hwy 5, Blue River, BC_ is wrong and the intersection address of _Murtle Lake Rd and Blue River West Frontage Rd, Blue River, BC_ is right.

# Address Location

The geocoder returns the following address properties related to location:

1. A geometric point specifying coordinates on the earth.
2. The projection of the coordinates.
3. locationDescription which specifies the type of location returned.
4. locationPositionalAccuracy.

In csv format, x and y contain an address’ coordinates and the srsCode contains the projection code. An srsCode of 4326 specifies the geographic projection used by Google Map, Google Earth, and Bing Maps. This code also means that x will contain longitude and y, latitude. For additional examples of geometry formats in other file formats, see the [online geocoder rest api](https://www2.gov.bc.ca/gov/content?id=118DD57CD9674D57BDBD511C2E78DC0D) .

# Address Location Descriptor

The locationDescriptor property of the geocoder may have one of the following values:

**parcelPoint** – a point guaranteed to be within the boundaries of the parcel that has been assigned a civic number

![image](https://github.com/user-attachments/assets/4b1c8780-7f32-4fc5-98b8-dabfdd8ee941)


**accessPoint** – where the driveway or walkway meets the curb

![image](https://github.com/user-attachments/assets/90dd3760-b56c-4e3e-abec-be79f63f4f7d)


**roofTopPoint** - a point on the roof of a building or structure that has been assigned a civic number

![image](https://github.com/user-attachments/assets/bdd7663a-31d7-463f-b745-59624e0219c3)


**frontDoorPoint** – location of front door of a house or entrance to a building or store

![image](https://github.com/user-attachments/assets/20863fc3-9171-4cc2-a6a0-84d00daf6dfd)


**routingPoint** – a point lying on a road centreline and directly in front of a site's accessPoint. A routing point is intended for use by routing algorithms to find routes between addresses.

![image](https://github.com/user-attachments/assets/ee0123bd-82db-4ee0-a4b1-7c65defb90b2)


# Address location positional accuracy

Location positional accuracy reflects how well the geocoder knows the geographic position of a given address. The locationPositionalAccuracy field in the geocoder output can have one of the following values:

**high** – position was observed or measured using GPS or survey instruments, or digitized off imagery with a resolution of one metre or better. Here is an address with a high-accuracy rooftop point:

![image](https://github.com/user-attachments/assets/0bd5a978-96de-487f-85ca-f2f563834c54)


Rooftop and front-door points always have high-accuracy. Parcel points can be high or medium accuracy

**medium –** position was derived from parcel boundaries or from a point known to be inside a parcel. Here is an address as a medium accuracy parcel point:

![image](https://github.com/user-attachments/assets/2089b72e-080f-4bd5-b5f7-e8e4bf4214d6)


**low –** position was interpolated along a block face address range. Access points are the most common type of low accuracy point.

![image](https://github.com/user-attachments/assets/f083015a-23df-4cee-a93c-ca3dddbd725a)


**coarse –** position represents an entire street, locality, or province

![image](https://github.com/user-attachments/assets/507a4e6b-7192-4da7-97ed-c57dc818aeb9)


# Representing location descriptor and positional accuracy in KML

In KML output format, location descriptor is represented by a placemark icon and positional accuracy by a colour as follows:

![image](https://github.com/user-attachments/assets/2eaac0c9-4928-4ed8-99ac-84759d8a6852)

