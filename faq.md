# Frequently Asked Questions

#### Q: What are the system requirements for using the Geocoder?
A web browser is required to use Geocoder web applications.
To use the Geocoder in Google Earth, Google Earth 6.1 or higher needs to be installed on the user’s computer.
The Batch Geocoder is not generally available to the public.  To request access please open a ticket with the [Data Systems & Services request system](https://dpdd.atlassian.net/servicedesk/customer/portal/1/group/7/create/15).
Addresses to be geocoded should be physical addresses and not mailing addresses.
Postal codes are not supported and will be ignored.

#### Q: How do I get help with using the Geocoder?

Please open a ticket with the [Data Systems & Services request system](https://dpdd.atlassian.net/servicedesk/customer/portal/1/group/7/create/15)

#### Q: How do I get postal code included with my geocoded address?

The Geocoder does not use or provide postal codes as they are not part of a physical address.

#### Q: Does the Geocoder use postal code to determine an address location?

The Geocoder does not use postal codes to determine address location. Address location is determined by looking up the input address, down to the civic number, in the set of known addresses in the Geocoder’s reference dataset. If no civic number match is found, the road network is searched for the block face containing the input address and a location is interpolated along the matching block face.

#### Q: How do I get a Geocoder API key for use in a production BC Government application?

To request a production Geocoder API key please open a ticket with the [Data Systems & Services request system](https://dpdd.atlassian.net/servicedesk/customer/portal/1/group/7/create/15) and include a description of the intended usage and consuming application name.

#### Q: How do I get Geocoder API keys for use by a developer?

Steps:

1. Open the API Service Portal (https://api.gov.bc.ca/)

2. Click the ‘login’ button on the top right side of the screen and login with your account. You can login with a GitHub or IDIR account.

3. Click on the API Directory tab

4. Click on BC Address Geocoder Web Service

5. Scroll down and click on the ‘request access’ hyperlink below BC Address Geocoder (Public) (not the button below BC Address Geocoder Parcels)

6. In the next screen, click the blue ‘+’ button to create an entry for your application, project or script.

7. Within the same screen click on ‘prod’ to ensure your API key is granted to the production Geocoder environment.

8. Click submit

9. In the next screen also click the button to ‘generate secrets’ and copy the resulting string to a file on your computer. This is your API key for the BC Address Geocoder.

10. At that point, our team will receive an email request, we will login and grant your API key to the BC Address Geocoder, following that you will receive a confirmation email.
