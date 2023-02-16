# BC Address Geocoder Match Scoring Reference

<br>

## Address List Match Accuracy = Number of addresses with a match score of 90 or higher divided by the number of addresses in the list

<br><br>

## Address Match Score = Match Precision award - Fault penalties

<br><br>

## Match Precisions
Name | Meaning | Points awarded
-------: | --------------- | -----:
<a name="PROVINCE">Province</a>|No match|1
<a name="LOCALITY">Locality</a>|Locality matched perfectly but finer-grained address elements such as street, civic number, and unit number, if provided, did not match|68
<a name="STREET">Street</a>|Locality and street matched but finer grained address elements such as civic number and unit number, if provided, did not match|78
<a name="BLOCK">Block</a>|An address range on a given block of a street was found that contains the civic number provided but no civic number in that block matched and no unit number, if provided, matched either|99
<a name="CIVIC_NUMBER">Civic Number</a>|Perfect match down to civic number but unit number, if provided, did not match|100
<a name="UNIT">Unit</a>|Perfect match down to unit|100
<a name="SITE">Site</a>|Perfect match down to site name|100
<a name="OCCUPANT">Occupant</a>|Perfect match down to occupant name (only used in occupants resource)|100
<a name="INTERSECTION">Intersection</a>|Perfect match down to intersection name|100

<br>

## Match Faults
Name | Meaning | Penalty
-------: | --------------- | -----:
<a name="ADDRESS.autoCompleted">ADDRESS.autoCompleted</a> |This fault applies to automatic word extensions done to the last word in autocomplete input. For example, the word 'Bar' in Boston Bar extended to Barkerville or Barret.|5
<a name="ADDRESS.missing">ADDRESS.missing</a> | No address was found in an occupant-address request. Just an occupant name.|12
<a name="CIVIC_NUMBER.missing">CIVIC_NUMBER.missing</a> | A given address didn't contain a civic number but one was found.|10
<a name="CIVIC_NUMBER.notInAnyBlock">CIVIC_NUMBER.notInAnyBlock</a> | A given civic number is not in any known address range for a given street in a given locality. The street within the given locality is returned with a match precision of STREET.|1
<a name="CIVIC_NUMBER_SUFFIX.notMatched">CIVIC_NUMBER_SUFFIX.notMatched</a> | A given civic number suffix for a given civic number and street was not found in a given locality.|1
<a name="INITIAL_GARBAGE.notAllowed">INITIAL_GARBAGE.notAllowed</a> |Unrecognized words found at beginning of address and before a unit number or civic number.|3
<a name="LOCALITY_GARBAGE.notAllowed">LOCALITY_GARBAGE.notAllowed</a> |Unrecognized words found between street and locality name.|3
<a name="LOCALITY.isAlias">LOCALITY.isAlias</a>	| A given civic number and street were found in an alias of the given locality but not the locality itself.|Up to 20 points depending on alias confidence. For example, if Victoria aliases to Willis Point with a confidence of 80%, the penalty is 4 points. If the same alias had a confidence of 20%, the penalty would be 16
<a name="LOCALITY.missing">LOCALITY.missing</a> | A given address didn’t contain a locality name but one was found that contains the given civic number and street.|10
<a name="LOCALITY.notMatched">LOCALITY.notMatched</a> | A given locality does not contain a given civic number and street but another locality was found that does.|35
<a name="LOCALITY.partialMatch">LOCALITY.partialMatch</a> | Some of the words in a locality name were matched. A match with a full locality name is returned.|1
<a name="LOCALITY.spelledWrong">LOCALITY.spelledWrong</a> | A given locality was spelled wrong but was successfully corrected to match a known locality.|2
<a name="POSTAL_ADDRESS_ELEMENT.notAllowed">POSTAL_ADDRESS_ELEMENT.notAllowed</a> | An element of a mailing address was detected (e.g., PO, BOX nn, SS, RR nn, a postal code. All such elements are ignored.|1
<a name="PROVINCE.missing">PROVINCE.missing</a> | A given address didn't contain a province code (e.g., BC)|1
<a name="PROVINCE.notMatched">PROVINCE.notMatched</a> |	A province code other than BC was found|1
<a name="PROVINCE_GARBAGE.notAllowed">PROVINCE_GARBAGE.notAllowed</a> |Unrecognized words found between locality name and province code.|2
<a name="SITE_NAME.missing">SITE_NAME.missing</a> |	A given address didn't contain a site name but one was found.|0
<a name="SITE_NAME.notMatched">SITE_NAME.notMatched</a> | A given site name was not found. A match without a site name is returned.|10
<a name="SITE_NAME.partiallyMatched">SITE_NAME.partialMatch</a> | Some of the words in a site name were matched. A match with a full site name is returned.| Up to 10 points depending on how weak the partial match is.
<a name="SITE_NAME.spelledWrong">SITE_NAME.spelledWrong</a> |	A given site name was spelled wrong but was successfully matched to a known site.|1
<a name="STREET.missing">STREET.missing</a> | A given address didn't contain a street but one was found.|0
<a name="STREET_DIRECTION.missing">STREET_DIRECTION.missing</a> | A given address didn’t contain a street direction for a given street name and street type in a given locality but one was found.|2
<a name="STREET_DIRECTION.notMatched">STREET_DIRECTION.notMatched</a> | A given street direction for a given street name and street type in a given locality was not found. A match without the street direction is returned.|2
<a name="STREET_DIRECTION.notMatchedInHighway">STREET_DIRECTION.notMatchedInHighway</a> | A given street direction for a given highway name and street type in a given locality was not found. A match without the street direction is returned.|0
<a name="STREET_DIRECTION.notPrefix">STREET_DIRECTION.notPrefix</a> | A given street direction was placed before street name instead of after. A match with a correctly positioned street direction is returned.|0
<a name="STREET_DIRECTION.notSuffix">STREET_DIRECTION.notSuffix</a> | A given street direction was placed after street name instead of before. A match with a correctly positioned street direction is returned.|0
<a name="STREET_DIRECTION.spelledWrong">STREET_DIRECTION.spelledWrong</a> | A given street direction was spelled wrong. A match with a correctly spelled street direction is returned.|1
<a name="STREET_NAME.isAlias">STREET_NAME.isAlias</a> | A given street name is an alias for the official street name. A match with the official street name is returned.|1
<a name="STREET_NAME.isHighwayAlias">STREET_NAME.isHighwayAlias</a> | A given street name is an alias for an official highway name. |0
<a name="STREET_NAME.missing">STREET_NAME.missing</a> | A given address didn't contain a street name but one was found.|1
<a name="STREET_NAME.notMatched">STREET_NAME.notMatched</a> | A given streetName within a given locality was not found. The locality is returned with a match precision of LOCALITY. Other addresses in different localities that contain the given civic number and street will also be returned but with a lesser score.|12
<a name="STREET_NAME.partialMatch">STREET_NAME.partialMatch</a> | Some of the words in a street name were matched. A match with a full street name is returned.|1
<a name="STREET_NAME.spelledWrong">STREET_NAME.spelledWrong</a> | A given street name was spelled wrong but was successfully corrected to match a known street name with the given locality.|2
<a name="STREET_QUALIFIER.missing">STREET_QUALIFIER.missing</a> | A given address didn't contain a street qualifier but one was found.|1
<a name="STREET_QUALIFIER.notMatched">STREET_QUALIFIER.notMatched</a> | A given street qualifier was not found. A match without a street qualifier is returned.|1
<a name="STREET_QUALIFIER.spelledWrong">STREET_QUALIFIER.spelledWrong</a> | A given street qualifier was spelled wrong but was successfully corrected to match a known street qualifier.|1
<a name="STREET_TYPE.missing">STREET_TYPE.missing</a> | A given address didn’t contain a street type for a given street name in a given locality but one was found.|6
<a name="STREET_TYPE.notMatched">STREET_TYPE.notMatched</a> | A given street type for a given street name in a given locality was not found. A match containing the correct street type is returned.|3
<a name="STREET_TYPE.notPrefix">STREET_TYPE.notPrefix</a> | A given street street was placed before street name instead of after. A match with a correctly positioned street type is returned.|0
<a name="STREET_TYPE.notSuffix">STREET_TYPE.notSuffix</a> | A given street type was placed after street name instead of before. A match with a correctly positioned street type is returned.|0
<a name="STREET_TYPE.spelledWrong">STREET_TYPE.spelledWrong</A> | A given street type was spelled wrong but was successfully corrected to match a known street type.|1
<a name="UNIT_DESIGNATOR.isAlias">UNIT_DESIGNATOR.isAlias</A> | A given unit designator is an alias of the official unit designator. A match containing the official unit designator is returned.|0
<a name="UNIT_DESIGNATOR.missing">UNIT_DESIGNATOR.missing</A> | A given address didn't contain a unit designator but one was found.|0
<a name="UNIT_DESIGNATOR.notMatched">UNIT_DESIGNATOR.notMatched</A> | A given unit designator was not found. A match containing the correct unit designator is returned.|1
<a name="UNIT_DESIGNATOR.spelledWrong">UNIT_DESIGNATOR.spelledWrong</A> | A given unit designator was spelled wrong but was successfully corrected to match a known unit designator.|1
<a name="UNIT_NUMBER.missing">UNIT_NUMBER.missing</A> | A given address didn't contain a unit number but one was found.|1
<a name="UNIT_NUMBER.notMatched">UNIT_NUMBER.notMatched</A> | A given unit number was not found. A match containing the correct unit number is returned.|1
<a name="UNIT_NUMBER.spelledWrong">UNIT_NUMBER.spelledWrong</A> | A given unit  number was spelled wrong but was successfully corrected to match a known unit number.|1
<a name="UNIT_NUMBER.suffixMissing">UNIT_NUMBER_SUFFIX.missing</A> | A given address didn't contain a unit number suffix but one was found.|1
<a name="UNIT_NUMBER.suffixNotMatched">UNIT_NUMBER_SUFFIX.notMatched</A> | A given unit number suffix was not found. A match containing the correct unit number suffix is returned.|1
<a name="UNRECOGNIZED_ELEMENT.notAllowed">UNRECOGNIZED.notAllowed</a>	| There are unnecessary or redundant words in an address. For example, the following address has a redundant streetType (e.g., Road): 33457 COTTAGE LANE ROAD ABBOTSFORD BC. The following address has an unnecessary site name (e.g., ABERDEEN SQUARE) : ABERDEEN SQUARE 101-2764 BARNET HIGHWAY COQUITLAM BC |3 * number of words not recognized
