def pytest_addoption(parser):
    parser.addoption("--api_url", action="store", default="https://geocoder-test.apps.silver.devops.gov.bc.ca/")
