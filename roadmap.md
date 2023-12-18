# Geocoder Roadmap
Version | Release Date | Features
-------: | --------------- | -------------
4.1|Jan 2021|Improved address matching
|||More address ranges
|||Make all locality names unique to avoid user-confusion in autocompletion mode
|||Unincorporated places and communities that aren't known to the BC Digital Road Atlas have names that relate to the appropriate DRA locality (e.g., ***Brentwood Bay in Central Saanich***, ***Fairfield in Victoria***)
|||Indian Reserves have names that relate to the nearest DRA locality (e.g., ***Boston Bar 8 near Boston Bar***)
4.2|Jan 2022| Improved address matching
4.3|Jan 2024| Improved recognition of addresses containing PO boxes ([256](https://github.com/bcgov/ols-geocoder/issues/256))
|||Improved response for cases where no site is found ([333](https://github.com/bcgov/ols-geocoder/issues/333))
|||Improved parsing to better handle non-address elements found in an addressString ([335](https://github.com/bcgov/ols-geocoder/issues/335))
|||Improved handling of locality qualifiers in locality names ([233](https://github.com/bcgov/ols-geocoder/issues/233))
|||Improved error responses for JSON ([21](https://github.com/bcgov/ols-geocoder/issues/21))
|||Continued data integration automation ([336](https://github.com/bcgov/ols-geocoder/issues/336), [344](https://github.com/bcgov/ols-geocoder/issues/344))
