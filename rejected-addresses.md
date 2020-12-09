|Given address|Expected result|Actual result|What Geocoder 4.0 can't handle|Fixed in 4.1
|----|----|----|----|
Omenica St, Hazelton, BC|Omineca St, Hazelton, BC|Omenica St, South Hazelton, BC|Multiple spelling mistakes|yes
2020 Kent Ave S, Vancouver, BC|2020 East Kent Ave S, Vancouver, BC|Dent Ave, Burnaby, BC|Suffix matching|no
3821 Cedarhill Rd, Saanich, BC|3821 Cedar hill Rd, Saanich, BC|3821 Saanich Rd, Saanich, BC|Separate words glued together|no
8514 Horse Shoe Bay Rd, Anglemont, BC|8514 Horseshoe Bay Rd, Anglemont, BC|Anglemont,BC|Compound words separated|no
1985 Millstream Rd, Old Mill Stream Manor, Highlands, BC|1985 Millstream Rd, Highlands,BC|Highlands, BC|Unknown siteName|yes
102 A Ave, Surrey, BC|102A Ave, Surrey, BC|A Surrey Ave, Kamloops, BC|Numbered streetName with detached suffix|yes
c/o Joe Fonebone, PO BOX 201, Quesnel, BC|Quesnel,BC|BC|Care-of info|no
200 21st Ave, prince george, BC|Prince George, BC|200 21st Ave N, Cranbrook, BC|Unknown street in locality|no
500 helmcken rd, victoria, BC|Helmcken Alley, Victoria, BC|500 Helmcken St, Vancouver, BC|Locality hopping|yes
500 helmcken rd, saanich, BC|Helmcken Rd, Saanich, BC|Helmcken Rd, Saanich, BC|Nothing because given address had correct locality|yes
2248 McAllister Ave, Poco, BC|2248 McAllister Ave, Port Coquitlam, BC|2248 McAllister Ave, Port Coquitlam, BC|Non-standard abbreviation which leads to a false negative|yes
10381 POPKUM RD S, ROSEDALE, BC|10381 Popkum Rd S, Popkum, BC|10381 Popkum Rd S, Popkum, BC|Missing locality alias which leads to a false negative|yes
105 150 21ST ST BUZZER 49, WEST VANCOUVER, BC|UNIT 105 -- 150 21st St, West Vancouver, BC with score of 90 or higher|UNIT 105 -- 150 21st St, West Vancouver, BC with score below 90|Unexpected info (PO Box) at front of address and between streetAddress and locality which leads to a false negative|yes
