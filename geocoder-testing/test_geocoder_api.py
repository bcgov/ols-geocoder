import csv
import json
import requests
import urllib.parse
import pytest

api = "addresses.json?addressString=%s"

ID = 0
QUERY = 1
EXPECTED_MATCH_PRECISION = 2
EXPECTTED_FULL_ADDRESS = 3
EXPECTED_FAULTS = 4

test_input = list()
with open('atp_addresses.csv', encoding = "ISO-8859-1") as atp_file:
    atp_tests = csv.reader(atp_file, delimiter=',', quotechar='"')
    headder_row = next(atp_tests)
    test_input = list(atp_tests)

'''
test_input=[
   ['civicAddressWithCivicNumber',	"525 Superior St	 Victoria	 BC", 'CIVIC_NUMBER', '525 Superior St, Victoria, BC', '[]'],
   ['civicAddressWithCivicNumberSuffix', "4251A Rockbank Pl	 West Vancouver	 BC", 'CIVIC_NUMBER', '4251A Rockbank Pl, West Vancouver, BC', '[]'],
   ['civicAddressWithOrdinalCivicNumberSuffix', "749E Lakeview Arrow Creek Rd	 Wynndel	 BC", 'CIVIC_NUMBER', '749E Lakeview Arrow Creek Rd, Wynndel, BC', '[]']
           ]
           '''

@pytest.fixture(
    scope="class",
    params=test_input,
)
def n(request, pytestconfig):
    # print('setup once per each param', request.param)
    api_url = pytestconfig.getoption("api_url")
    quoted_query = urllib.parse.quote_plus(request.param[QUERY])
    resp = requests.get(api_url + api % quoted_query)
    parsed = json.loads(resp.text)

    return request.param, parsed

class TestClass:

    def test_expected_match_precision(self, n):
        (row, resp) = n

        assert resp['features'][0]['properties']['matchPrecision'] == row[EXPECTED_MATCH_PRECISION]

    def test_expected_full_address(self, n):
        (row, resp) = n

        assert resp['features'][0]['properties']['fullAddress'] == row[EXPECTTED_FULL_ADDRESS]

    def test_expected_faults(self, n):
        (row, resp) = n

        assert str(resp['features'][0]['properties']['faults']) == row[EXPECTED_FAULTS]
