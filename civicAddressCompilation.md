# Digitizing new civic addresses using Location Services in Action and the new_civic_addresses spreadsheet

This tutorial shows you how to use the Location Services in Action application to digitize the locations of new civic addresses and put them into the New Address Worksheet.

To get started, you will need a Chrome web browser and a copy of the MS Excel spreadsheet called new_civic_addresses.xlsx If you don't have MS Excel, you can use new_civic_addresses.csv and your favourite spreadsheet software instead.  Feel free to add your agency name to the file name. new_civic_addresses.xlsx is compliant with the [BC Physical Address eXchange (PAX) Standard](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md)

1. Open up new_civic_addresses.xlsx. It will have some example addresses in it to show you which columns address elements go into.

2.  Open up a Chrome browser and visit [Location Services In Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)

![image](https://user-images.githubusercontent.com/11318574/123008528-dc8d2400-d36f-11eb-8872-9a2b51b83543.png)

3. In the Address tab, enter your locality and press the Search (magnifying glass)  icon, then pan and zoom into the specific area you are creating new addresses for.

![image](https://user-images.githubusercontent.com/11318574/123138330-bcaa3e80-d409-11eb-8de6-d59aa34714e0.png)


Turn on imagery and other cartographic information layers as follows:

 
4. Click on the layers icon in the top right corner of the map:

![image](https://user-images.githubusercontent.com/11318574/123008730-460d3280-d370-11eb-9117-3769eae3b8a9.png)


The app will display a list of layers grouped into background layers and foreground layers.

![image](https://user-images.githubusercontent.com/11318574/123009893-7229b300-d372-11eb-9a01-a7bb0a66ad4f.png)

 
5. Select the best background imagery layer for your area. There are four to choose from: Mapbox Satellite, ESRI imagery, Google Satellite, and Bing Aerial.

![image](https://user-images.githubusercontent.com/11318574/123138681-2cb8c480-d40a-11eb-99b5-1a196460b379.png)

6. Select foreground layers Digital Road Atlas and ParcelMap BC. Turn on the Address - parcel point layer to see all existing addresses in the area.

![image](https://user-images.githubusercontent.com/11318574/123010311-36dbb400-d373-11eb-9d48-37914f097f8e.png)

7. Select the Route tab next to the Address tab

![image](https://user-images.githubusercontent.com/11318574/123138920-75707d80-d40a-11eb-8f27-eb6d4cb89484.png)


8. Zoom in to the first address you would like to digitize.

![image](https://user-images.githubusercontent.com/11318574/123139617-427ab980-d40b-11eb-9c03-851704463c85.png)


9. Put the cursor on the roof, just above the front door of the building getting the new address and click on the map. If there is no existing building, click on a parcel centroid or a point within the parcel where the roof is likely to be.

![image](https://user-images.githubusercontent.com/11318574/123141141-ea44b700-d40c-11eb-86ef-45cefc07d0a2.png)


The app stores and displays the lat/lon of the digitized point in the *Add waypoint* text box located just below the route waypoint list. 

10. Click on the copy to clipboard icon just to the right of the coordinates:

![image](https://user-images.githubusercontent.com/11318574/123142974-df8b2180-d40e-11eb-807b-79bde867385f.png)

11. Switch to your spreadsheet window and paste the coordinates into the siteLatLon column in an empty row. 

![image](https://user-images.githubusercontent.com/11318574/123144977-2712ad00-d411-11eb-93ea-064e9d1d91d0.png)

12.  Enter all the elements of the new civic address into the other columns of the same row. If you are defining a range of unitNumbers associated with an address, as in row 3, you don't need to put any coordinates into siteLatLon. It will be assumed that all units share the same location as the associated building address (e.g., row 2). If there are some units at a civic number that are separate buildings (e.g., townhouses, cabins), feel free to add a separate row for each such unit and its siteLatLon.  Less frequently used columns appear to the right of the notes column.  Columns are defined in the [BC Physical Address eXchange (PAX) Standard](https://github.com/bcgov/ols-geocoder/blob/gh-pages/BCAddressExchangeSchema.md#schema)

13. Digitize the remaining new civic addresses on your list.

14. Delete the example addresses at the top of the list.
