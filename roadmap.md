# Geocoder Roadmap
Version | Release Date | Features
-------: | --------------- | -------------
4.1|Feb 2021|Improved address matching
|||More address ranges
|||Make all locality names unique to avoid user-confusion in autocompletion mode
|||Unincorporated places and communities that aren't known to the BC Digital Road Atlas have names that relate to the appropriate DRA locality (e.g., ***Brentwood Bay in Central Saanich***, ***Fairfield in Victoria***)
|||Indian Reserves have names that relate to the nearest DRA locality (e.g., ***Boston Bar 8 near Boston Bar***)
4.2|May 2022| Improved recognition of street and locality names containing missing or extra blank spaces.
|||Improved handling of unitNumberSuffix in structured address requests.
4.3|Feb 2024| Improved recognition of addresses containing PO boxes ([256](https://github.com/bcgov/ols-geocoder/issues/256))
|||Improved response for cases where no site is found ([333](https://github.com/bcgov/ols-geocoder/issues/333))
|||Improved parsing to better handle non-address elements found in an addressString ([335](https://github.com/bcgov/ols-geocoder/issues/335))
|||Improved handling of locality qualifiers in locality names ([233](https://github.com/bcgov/ols-geocoder/issues/233))
|||Improved error responses for JSON ([21](https://github.com/bcgov/ols-geocoder/issues/21))
|||Continued data integration automation ([336](https://github.com/bcgov/ols-geocoder/issues/336), [344](https://github.com/bcgov/ols-geocoder/issues/344))
4.4|Aug 2024| Improvements to address data integration process.
4.5|Jan 2025| Added new exactSpelling parameter to improve autoComplete suggestions for partial civic addresses and locality names ([387](https://github.com/bcgov/ols-geocoder/issues/387))
|||Added new fuzzyMatch parameter to sort autoComplete suggestions using a fuzzy match comparison to the addressString.
|||Added streetAddress property to the BC Address Geocoder response (except when using brief=true).([54](https://github.com/bcgov/ols-geocoder/issues/54))
|||Improved handling of addresses with a missing street name.([223](https://github.com/bcgov/ols-geocoder/issues/223))
