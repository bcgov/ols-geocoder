# Submitting an address list to the batch geocoder interactively

This document describes the process of submitting a list of addresses to the batch geocoder.

## Preliminaries

If you don't have an active BC IDIR account, you will have to [register](https://github.com/bcgov/ols-geocoder/blob/gh-pages/bc-location-services/batch-geocoder-registration.md) with the batch geocoder using a BCeID account.

You will also need a list of addresses in a format and schema the batch geocoder understands.  For help with this, click [here](https://www2.gov.bc.ca/assets/gov/data/geographic/location-services/geocoder/batch_address_data_prep.pdf). For the purposes of this tutorial, we have provided an address list if you don't have one.

## Submitting a list of addresses

**STEP 1.** Visit the [batch geocoder](https://apps.gov.bc.ca/pub/cpf/secure/ws/apps/geocoder/). The system will display a login page.

![image](https://user-images.githubusercontent.com/11318574/106337878-60654780-62cd-11eb-9ee7-fa20b5c1cdab.png)

**STEP 2.** Enter you BCeID or IDIR login and password. If successfully logged in, you will see the following page:

![image](https://user-images.githubusercontent.com/11318574/106338032-c81b9280-62cd-11eb-82dd-30f6b12e4367.png)


**STEP 3.** Click on the tab named Create Multi-Request Job

![image](https://user-images.githubusercontent.com/11318574/106338121-f4371380-62cd-11eb-9f24-c4553ea9c92f.png)
  
The system will display the following form:

![image](https://user-images.githubusercontent.com/11318574/106338197-29436600-62ce-11eb-90aa-f58edada19b9.png)

**STEP 4.** Enter the name of a file or a file URL. A file URL must be a public URL or a URL that is accessible within BC government network.

![image](https://user-images.githubusercontent.com/11318574/106338415-c3a3a980-62ce-11eb-806d-7545e0fda5dd.png)

The URL entered here is a list of addresses, one for every incorporated municipality in BC.

**STEP 5.** Close the Input Data and Application parameters sections of the form by clicking on their section names. The Result format section will then be visible.

![image](https://user-images.githubusercontent.com/11318574/106338675-59d7cf80-62cf-11eb-80ad-5adb8116f5eb.png)

If you don't know what a map projection is, leave it unchanged. The default projection called WGS84 (longitude, latitude) is what all mapping apps understand.

**STEP 6.** Choose your Result data content type, otherwise known as output format. Let's leave the default of CSV for now since it is easy to view in a spreadsheet. 

![image](https://user-images.githubusercontent.com/11318574/106339855-caccb680-62d2-11eb-8186-139bb9f18304.png)

**STEP 7.** Click on the Result format section name to close it. You will now see a Create Job button at the bottom of the form.

![image](https://user-images.githubusercontent.com/11318574/106339936-154e3300-62d3-11eb-84c7-8823851f6556.png)

The batch geocoder calls a submitted address list a job. 

**STEP 8.** Click on the Create Job button. The system will tell you it is creating geocoding requests, one for each address in your list.

![image](https://user-images.githubusercontent.com/11318574/106340033-69591780-62d3-11eb-9525-7ed62fb24f44.png)


## Checking if list of addresses is geocoded and ready to download

Refresh the current page in your browser by clicking the Refresh icon next to the browser URL box at the top of the page.

![image](https://user-images.githubusercontent.com/11318574/106341962-0b7bfe00-62da-11eb-9d7b-f4b990f10153.png)

The system will display the current status. In this case, the system has completed processing and your geocoded address list is ready to download.

![image](https://user-images.githubusercontent.com/11318574/106342080-7af1ed80-62da-11eb-8806-c73474f0bc13.png)

If your results weren't ready, you would get a status page saying it was still processing or creating the results file for download.

## Downloading your geocoded addresses

Just click on the download icon and the results file will be downloaded to your machine.

![image](https://user-images.githubusercontent.com/11318574/106342178-de7c1b00-62da-11eb-80e7-cbd7c8e9ec24.png)

After clicking the button, the system downloaded the file and my browser showed the downloaded filename at the bottom left of the window

![image](https://user-images.githubusercontent.com/11318574/106342241-226f2000-62db-11eb-8b42-a1dd8eda2a4d.png)

Click on the downloaded filename and a spreadsheet or text editor will pop up with your geocoded addresses displayed.

Here are the first fifteen rows of my results file:

![image](https://user-images.githubusercontent.com/11318574/106342425-d83a6e80-62db-11eb-9e65-b6f16b3de0a0.png)

All 15 addresses resulted in a perfect match score of 100 with a match precision of civic number which means the address matched right down to the civic number on a block on a street in a locality in the Province of BC. 

The fullAddress column contains the cleaned and standardized address as a single string in [Single-Line Address Format](https://github.com/bcgov/ols-geocoder/blob/gh-pages/singleLineAddressFormat.md).

Other columns just off the right contain all the individual address elements such as civicNumber, streetName, streetType, streetDirection, locality, etc.

What you won't see in this file is the original address as it appeared in your list. This is to keep the result file to a more manageable size. There is a yourId column that is carried through from your input file to the result file that you can use to associate each geocoded address with its original address. For more details, see [Preparing your address list for batch geocoding](https://www2.gov.bc.ca/assets/gov/data/geographic/location-services/geocoder/batch_address_data_prep.pdf)

For a more complete explanation of the geocoder results file, click [here](https://www2.gov.bc.ca/assets/gov/data/geographic/location-services/geocoder/understanding_geocoder_results.pdf)
