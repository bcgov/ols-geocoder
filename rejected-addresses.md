Here is a table of common address errors and how well they are handled in Geocoder 4.1 compared to 4.0:

|Given address|Expected result|Expected score|Actual result in Geocoder 4.0|What 4.0 can't handle|Fixed in 4.1
|---|---|---|---|---|---|
[964 Lillooet Rd North Vancouver, North Vancouver, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=964%20Lillooet%20Rd%20North%20Vancouver,%20North%20Vancouver,%20BC)|964 Lillooet Rd North Vancouver, BC|90+|964 Lillooet Rd North Vancouver, BC and score below 90|Duplicate locality name|yes
[964 LIllooet Rd Road, North Vancouver, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=964%20LIllooet%20Rd%20Road,%20North%20Vancouver,%20BC)|964 Lillooet Rd, North Vancouver, BC|90+|964 Lillooet Rd, North Vancouver, BC with score below 90|Duplicate street type|yes
[1413 Omenica Ave, Prince Rupert, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=1413%20Omenica%20Ave,%20Prince%20Rupert,%20BC)|1413 Omineca Ave, Prince Rupert, BC|90+|Rupert, BC with score below 90|Multiple spelling mistakes|yes
[500 helmcken rd, victoria, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=500%20helmcken%20rd,%20victoria,%20BC)|Helmcken Alley, Victoria, BC|below 90|500 Helmcken St, Vancouver, BC|Locality hopping|yes
[525 Superior St BC Ministry of Environment Victoria BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=525%20Superior%20St%20BC%20Ministry%20of%20Environment%20Victoria%20BC)|525 Superior St, Victoria, BC|90+|BC|Noise|Yes
[1985 Millstream Rd, Old Mill Stream Manor, Highlands, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=1985%20Millstream%20Rd,%20Old%20Mill%20Stream%20Manor,%20Highlands,%20BC)|1985 Millstream Rd, Highlands,BC|90+|Highlands, BC with score below 90|Unknown siteName|yes
[102 A Ave, Surrey, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=102%20A%20Ave,%20Surrey,%20BC)|102A Ave, Surrey, BC|below 90|A Surrey Ave, Kamloops, BC with score below 90|Numbered streetName with detached suffix|yes
[Unit910 7380 ELMBRIDGE WAY, RICHMOND, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=Unit910%207380%20ELMBRIDGE%20WAY,%20RICHMOND,%20BC)| Unit 910 -- 7380 ELMBRIDGE WAY, RICHMOND, BC|90+|7380 Elmbridge Way, Richmond, BC|Unit number stuck to unit designator|Yes
[2248 McAllister Ave, Poco, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=2248%20McAllister%20Ave,%20Poco,%20BC)|2248 McAllister Ave, Port Coquitlam, BC|90+|2248 McAllister Ave, Port Coquitlam, BC|Abbreviated localty name which leads to a false negative (score below 90)|yes
[10381 POPKUM RD S, ROSEDALE, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=10381%20POPKUM%20RD%20S,%20ROSEDALE,%20BC)|10381 Popkum Rd S, Popkum, BC|90+|10381 Popkum Rd S, Popkum, BC with score below 90 or higher|Missing locality alias which leads to a false negative (score below 90)|yes
[105 150 21ST ST BUZZER 49, WEST VANCOUVER, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=105%20150%2021ST%20ST%20BUZZER%2049,%20WEST%20VANCOUVER,%20BC)|UNIT 105 -- 150 21st St, West Vancouver, BC|90+|UNIT 105 -- 150 21st St, West Vancouver, BC with score below 90|Unexpected info (PO Box) at front of address and between streetAddress and locality which leads to a false negative (score below 90)|yes
[58550 Wild Rose Lane Hope BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=58550%20Wild%20Rose%20Lane%20Hope%20BC)|58550 Wildrose Lane, Laidlaw, BC|90+|Hope, BC with score below 90|Compound name entered as separate words|No
3821 Cedarhill Rd, Saanich, BC|3821 Cedar hill Rd, Saanich, BC|90+|3821 Saanich Rd, Saanich, BC|Separate words entered as compound name|No
c/o PO BOX 201, Quesnel, BC|Quesnel, BC|below 90|BC|c/o with PO BOX|Yes
[c/o 442 Kinchant St, Quesnel, BC](https://bcgov.github.io/ols-devkit/ols-demo/index.html?q=c/o%20Joe%20Fonebone%20442%20Kinchant%20St,%20Quesnel,%20BC)|442 Kinchant St, Quesnel, BC|90+|442 Kinchant St, Quesnel, BC with score below 90|c/o with civic address|Yes
c/o Joe Fonebone 442 Kinchant St, Quesnel, BC|442 Kinchant St, Quesnel, BC|90+|BC| c/o with name and civic address|Yes
200 21st Ave, prince george, BC|Prince George, BC|below 90|200 21st Ave N, Cranbrook, BC|Unknown street in locality|no
950 Hope Princeton Way, Hope, BC|950 Old Hope Princeton Way, Hope, BC|90+|950 Hope St, Port Moody, BC|Suffix matching|no
[964 Lillooet Rd St, Lillooet St,  Rd,  North Vancouver, Squamish, BC](964 Lillooet Rd St, Lillooet St,  Rd,  North Vancouver, Squamish, BC)|964 Lilloet Rd, North Vancouver, BC|?|Redundant, conflicting, address elements|Yes

[Here](https://github.com/bcgov/ols-geocoder/blob/gh-pages/understanding-match-scoring.md) is a detailed explanation of address match scoring.
