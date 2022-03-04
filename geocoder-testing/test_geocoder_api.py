import csv
import json
import requests
import urllib.parse
import pytest

api = "addresses.json?addressString=%s"
ID = 0
QUERY = 1
EXPECTED_MATCH_PRECISION = 2
EXPECTED_FULL_ADDRESS = 3
EXPECTED_FAULTS = 4
STATUS = 5

'''
test input: https://bcgov.github.io/ols-geocoder/atp_addresses.csv
header and first row as an example:
yourId,addressString,expectedMatchPrecision,expectedFullAddress,expectedFaults,status,parcelPoint,issue,expectedAccuracyHigh,expectedConfidenceHigh
00002-civicAddressWithCivicNumber,"525 Superior St Victoria BC",CIVIC_NUMBER,"525 Superior St, Victoria, BC",[],R,SRID=4326;POINT(-123.370780 48.417926),,T,T
'''

def is_regression_test(row):
    return row[STATUS] == 'R'

def is_future_test(row):
    return row[STATUS] == 'F'

def is_unit_test(row):
    return row[STATUS] == 'U'

test_input = list()
with open('/workspace/file/atp_addresses.csv', encoding = "UTF-8") as atp_file:
    atp_tests = csv.reader(atp_file, delimiter=',', quotechar='"')
    headder_row = next(atp_tests)
    test_input = list(atp_tests)


@pytest.fixture(
    scope="class",
    params=test_input,
)
def n(request, pytestconfig):
    api_url = pytestconfig.getoption("api_url")
    quoted_query = urllib.parse.quote(request.param[QUERY])
    resp = requests.get(api_url + api % quoted_query)
    parsed = json.loads(resp.text)
    return request.param, parsed

class TestClass:

    '''
    To quote Brian Kelsey: Testing for matchPrecision is a nice way to spot cases where we have lost a specific address (known parcel point = CIVIC_NUMBER)
    and the Geocoder is now interpolating it based on known address points or block ranges from the road network (BLOCK or STREET level).
    This also helps to identify cases where a site name is no longer in the source data (such as with “Building name – 123 main street, Vancouver, BC”).
    Any additional tests beyond checking the fullAddress is extending the scope of our previous tests.
    Staying with fullAddress tests will mean that often the ATP test will yield zero failures and can provide a pass/fail flag.
    Adding matchPrecision and faults will provide a deeper analysis but potentially a more manual review as there may be slight variations in source data from address authorities from month to month.
    '''

    def test_expected_full_address(self, n):
        (row, resp) = n

        if is_regression_test(row):
            assert resp['features'][0]['properties']['fullAddress'] == row[EXPECTED_FULL_ADDRESS]
