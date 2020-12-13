Here is a table of common address errors and how well they are handled in Geocoder 4.1 compared to 4.0:

|Given address|Expected result|Actual result in Geocoder 4.0|What 4.0 can't handle|Fixed in 4.1
|---|---|---|---|---|
Oscar the Grouch 1 Centennial Sq Felix the Cat Victoria Winnie the Pooh BC|1 Centennial Sq, Victoria, BC|Atlin, BC|Noise|Yes
964 Lillooet Rd North Vancouver, North Vancouver, BC|964 Lillooet Rd North Vancouver, BC and score of 90 or higher|964 Lillooet Rd North Vancouver, BC and score below 90|Duplicate locality name|yes
964 LIllooet Rd Road, North Vancouver, BC|964 Lillooet Rd, North Vancouver, BC and score of 90 or higher|964 Lillooet Rd, North Vancouver, BC with score below 90|Duplicate street type|yes
1413 Omenica Ave, Prince Rupert, BC|1413 Omineca Ave, Prince Rupert, BC|Rupert, BC|Multiple spelling mistakes|yes
500 helmcken rd, victoria, BC|Helmcken Alley, Victoria, BC|500 Helmcken St, Vancouver, BC|Locality hopping|yes
1985 Millstream Rd, Old Mill Stream Manor, Highlands, BC|1985 Millstream Rd, Highlands,BC|Highlands, BC|Unknown siteName|yes
102 A Ave, Surrey, BC|102A Ave, Surrey, BC|A Surrey Ave, Kamloops, BC|Numbered streetName with detached suffix|yes
Unit910 7380 ELMBRIDGE WAY, RICHMOND, BC | Unit 910 -- 7380 ELMBRIDGE WAY, RICHMOND, BC|7380 Elmbridge Way, Richmond, BC|Unit number stuck to unit designator|Yes
2248 McAllister Ave, Poco, BC|2248 McAllister Ave, Port Coquitlam, BC|2248 McAllister Ave, Port Coquitlam, BC|Abbreviated localty name which leads to a false negative (score below 90)|yes
10381 POPKUM RD S, ROSEDALE, BC|10381 Popkum Rd S, Popkum, BC and score of 90 or higher|10381 Popkum Rd S, Popkum, BC with score below 90 or higher|Missing locality alias which leads to a false negative (score below 90)|yes
105 150 21ST ST BUZZER 49, WEST VANCOUVER, BC|UNIT 105 -- 150 21st St, West Vancouver, BC with score of 90 or higher|UNIT 105 -- 150 21st St, West Vancouver, BC with score below 90|Unexpected info (PO Box) at front of address and between streetAddress and locality which leads to a false negative (score below 90)|yes
HOUSE 900 Malachan Reserve|HOUSE 900 Malachan 11 -- Ditidaht, BC|Malachan 11, BC|Numbered houses in an IR|No in 4.1 Silver, maybe in Gold
58550 Wild Rose Lane Hope BC|58550 Wildrose Lane, Laidlaw, BC|Hope, BC|Compound words separated|No in Silver, maybe in Gold
3821 Cedarhill Rd, Saanich, BC|3821 Cedar hill Rd, Saanich, BC|3821 Saanich Rd, Saanich, BC|Separate words glued together|No in 4.1 Silver, maybe in Gold
c/o Joe Fonebone, PO BOX 201, Quesnel, BC|Quesnel, BC|BC|Care-of info|No in 4.1 Silver, maybe in Gold
200 21st Ave, prince george, BC|Prince George, BC|200 21st Ave N, Cranbrook, BC|Unknown street in locality|no
950 Hope Princeton Way, Hope, BC|Hope Railway Overhead, Hope, BC|950 Old Hope Princeton Way, Hope, BC|Suffix matching (first word(s) missing)|no

[Here](https://github.com/bcgov/ols-geocoder/blob/gh-pages/understanding-match-scoring.md) is a detailed explanation of address match scoring.
