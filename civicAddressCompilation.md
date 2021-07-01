# How to Design a Civic Address Fabric and Compile Civic Addresses using Location Services in Action and the new_civic_addresses Spreadsheet

This tutorial shows you how to use the Location Services in Action application to design a civic address fabric and compile civic addresses. Compiling civic addresses involves digitizing the coordinate location of each address and storing all address information in the new\_civic\_addresses spreadsheet which is compliant with the  [Physical Address eXchange (PAX) Schema](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md).

To get started, you will need a Chrome web browser and a copy of the MS Excel spreadsheet called new_civic_addresses.xlsx If you don't have MS Excel, you can use new_civic_addresses.csv and your favourite spreadsheet software instead.  Feel free to add your agency name to the file name.

1. Open up new_civic_addresses.xlsx. It will have some example addresses in it to show you which columns address elements go into.

2.  Open up a Chrome browser and visit [Location Services In Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)

![image](https://user-images.githubusercontent.com/11318574/123008528-dc8d2400-d36f-11eb-8872-9a2b51b83543.png)
<br><br>
3. In the Address tab, enter your locality and press the Search (magnifying glass)  icon, then pan and zoom into the specific area you are creating new addresses for.

![image](https://user-images.githubusercontent.com/11318574/123138330-bcaa3e80-d409-11eb-8de6-d59aa34714e0.png)
<br>

Turn on imagery and other cartographic information layers as follows:

 
4. Click on the layers icon in the top right corner of the map:

![image](https://user-images.githubusercontent.com/11318574/123008730-460d3280-d370-11eb-9117-3769eae3b8a9.png)


The app will display a list of layers grouped into background layers and foreground layers.

![image](https://user-images.githubusercontent.com/11318574/123009893-7229b300-d372-11eb-9a01-a7bb0a66ad4f.png)
<br><br>
 
5. Select the best background imagery layer for your area. There are four to choose from: Mapbox Satellite, ESRI imagery, Google Satellite, and Bing Aerial.

![image](https://user-images.githubusercontent.com/11318574/123138681-2cb8c480-d40a-11eb-99b5-1a196460b379.png)
<br><br>

6. Select foreground layers Digital Road Atlas and ParcelMap BC. Turn on the Address - parcel point layer to see all existing addresses in the area.

![image](https://user-images.githubusercontent.com/11318574/123010311-36dbb400-d373-11eb-9d48-37914f097f8e.png)
<br><br>
7. Close the side panel by clicking on the icon circled in red. This will let you make the map bigger.

![image](https://user-images.githubusercontent.com/11318574/123313916-d2d5fe80-d4de-11eb-981f-51b46beb470c.png)
<br><br>
8.  Take a screenshot of the map and open up the side panel again.

9.  Print the screenshot or load it up into your favourite paint program.

![image](https://user-images.githubusercontent.com/11318574/123315947-234e5b80-d4e1-11eb-942b-4456862f4932.png)
<br><br>

10. Design your address fabric on your screenshot. Add or change road names, decide on address ranges for each block, and add civic numbers. Here are some design tips: 

- Address ranges usually increase by 100 per block
- One blockface is usually assigned even numbers and the other odd numbers
- Leave number gaps between civic numbers to allow infilling.
- Place civic numbers roughly where future building roofs will be.

For more design tips see the powerpoint presentation called  address_fabric_design_for_NG911

When you're happy with your address fabric, its time to digitize each civic address site location.

![image](https://user-images.githubusercontent.com/11318574/123883273-8be07280-d8fd-11eb-95b5-6c1a297a538a.png)
<br><br>

11. In Location Services in Action, select the Route tab next to the Address tab

![image](https://user-images.githubusercontent.com/11318574/123138920-75707d80-d40a-11eb-8f27-eb6d4cb89484.png)
<br><br>

12. Zoom in to the first address you would like to digitize.

![image](https://user-images.githubusercontent.com/11318574/123139617-427ab980-d40b-11eb-9c03-851704463c85.png)
<br><br>

13. Put the cursor on the roof, just above the front door of the building getting the new address and click on the map. If there is no existing building, click on a parcel centroid or a point within the parcel where the roof is likely to be.  In this tutorial, we are going to click on the centroid of the parcel just below 47202 Homewood Rd (e.g. in the centre of the yellow ellipse shown below). This location is going to be 47186 Homewood Rd.

![image](https://user-images.githubusercontent.com/11318574/124089844-24174e00-da09-11eb-9eff-c4fac0272a99.png)
<br><br>

The application will store and display the lat/lon of the digitized point in the *Add waypoint* text box located just below the route waypoint list. 

14. Click on the copy to clipboard icon just to the right of the coordinates:

![image](https://user-images.githubusercontent.com/11318574/123142974-df8b2180-d40e-11eb-807b-79bde867385f.png)
<br><br>

15. Switch to your spreadsheet window and paste the coordinates into the siteLatLon column in an empty row. 

![image](https://user-images.githubusercontent.com/11318574/123144977-2712ad00-d411-11eb-93ea-064e9d1d91d0.png)
<br><br>

16.  Enter all the elements of the new civic address into the other columns of the same row.  

- Columns are defined in the [Physical Address eXchange (PAX) Schema](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md#schema). 

- Less frequently used columns appear to the right of the notes column.

- *If you were defining a range of unitNumbers associated with an address, as in row 3, you wouldn't need to put any coordinates into siteLatLon. It would be assumed that all units share the same location as the associated building address (e.g., row 2). If there are some units at a civic number that are separate buildings (e.g., townhouses, cabins), feel free to add a separate row for each such unit and its siteLatLon.*
<br><br>

17. Digitize the remaining new civic addresses on your list.

18. Delete the example addresses at the top of the list.
