# Frequently Asked Questions

#### Q: What are the system requirements for using the Geocoder?
A web browser is required to use the Geocoder web application and Address List Editor.
To use the Geocoder in Google Earth, Google Earth 6.1 or higher needs to be installed on the user’s computer.
The Batch Geocoder is not generally available to the public.  To request access please [contact DataBC](https://forms.gov.bc.ca/databc-contact-us/).
Addresses to be geocoded should be physical addresses and not mailing addresses.
Postal codes are not supported and will be ignored.

#### Q: How do I get help with using the Geocoder?

If you need help, please call the Service Desk’s Toll Free service at 1-866-952-6801. A representative is available Monday through Friday (except Holidays) from 8:30 am to 4:30 pm (PST). Please leave a voice message is you are calling after hours and a representative will return your call the next business day. You can also E-mail the NRS Service Desk. A representative is available Monday through Friday (except Holidays) from 8:30 am to 4:30 pm (PST).

#### Q: How do I get postal code included with my geocoded address?

The Geocoder does not use or provide postal codes as they are not part of a physical address.

#### Q: Does the Geocoder use postal code to determine an address location?

The Geocoder does not use postal codes to determine address location. Address location is determined by looking up the input address, down to the civic number, in the set of known addresses in the Geocoder’s reference dataset. If no civic number match is found, the road network is searched for the block face containing the input address and a location is interpolated along the matching block face.

#### Q: How do I get a Geocoder API key for use in a production BC Government application?

To request a production Geocoder API key [contact DataBC](https://forms.gov.bc.ca/databc-contact-us/) and include a description of the intended usage and consuming application name.

#### Q: How do I get Geocoder API keys for use by a developer?

There are two steps involved:

Step 1 (one time only): 
Launch the [API Gateway administration (GWA)](https://gwa.apps.gov.bc.ca/ui/apiKeys) application and login with your GitHub account. This will register you as a developer with the API gateway.

Step 2 (per API): 
To be granted access to create your own developer Geocoder API keys please [contact DataBC](https://forms.gov.bc.ca/databc-contact-us/) and include your GitHub account name as well as confirmation that you have logged into GWA.

Once you have logged into GWA, and following our team granting access, you should see the BC Address Geocoder listed on the right side of your screen. By expanding each item, you will see the associated rate limit.

You can generate and delete as many API keys as required on the left side of the screen. Please note that these API keys expire every 90 days. Your account access will remain, but the API key will need to be renewed from within GWA. Screen captures that illustrate this process are available in the [GWA user guide](https://github.com/bcgov/gwa/wiki/Developer-Guide).
