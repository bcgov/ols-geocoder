|Given address|Expected result|Actual result|What the geocoder can't handle
|----|----|----|----|
Omenica St, Hazelton, BC|Omineca St, Hazelton, BC|  |Multiple spelling mistakes
2020 Kent Ave S, Vancouver, BC|2020 East Kent Ave S, Vancouver, BC|Suffix matching
3821 Cedarhill Rd, Saanich, BC|3821 Cedar hill Rd, Saanich, BC|||Separate words glued together
8514 Horse Shoe Bay Rd, Anglemont, BC|8514 Horseshoe Bay Rd, Anglemont, BC||Compound words separated
Old Mill Stream Manor 1985 Millstream Rd, Highlands, BC|Old Mill Stream -- 1985 Millstream Rd, Highlands||Unknown siteNames or missing frontGate
c/o Mr Allen & Mrs Bear, PO BOX 102, Quesnel,BC|Quesnel,BC||Care-of info
200 21st Ave, prince george, BC|Prince George,BC||Locality hopping
B0X 188, KALEDEN, BC|KALEDEN,BC||Locality hopping
2248 McAllister Ave, Poco, BC|2248 McAllister Ave, Port Coquitlam, BC||Non-standard abbreviations
102 A Ave, Surrey, BC|102A Ave, Surrey, BC||Numbered streetName with detached suffix
1977 TAPPEN NOTCH HILL RD, SORRENTO, BC|1977 Tappen Notch Hill Rd, Tappen, BC||Missing locality aliases
105 150 21ST ST BUZZER 49, WEST VANCOUVER, BC|UNIT 105 -- 150 21st St, West Vancouver, BC||Unexpected info (PO Box) at front of address and between streetAddress and locality
#8 Blueberry Reserve, Buick|House 8, Blueberry Reserve -- Buick, BC||House ids within an Indian Reserve
