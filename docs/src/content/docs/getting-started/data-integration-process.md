---
title: Geocoder Data Integration Process
description: An overview of the geocoder data integration process and a proposal to improve its implementation.
---

## Overview

This document describes the current geocoder data integration process and how it can be improved.

## What is Geocoder Data Integration?

Here's an overview of the geocoder data integration process:

```
| Gather | Transform | Integrate | Verify | Deploy |
```

- **Gather**: Download source road network and address data
- **Transform**: Transform all source data into standard schemas and formats and do field-level validation
- **Integrate**: Tie addresses to the road network and generate address ranges
- **Verify**: Verify the quality of the integrated data by geocoding test addresses
- **Deploy**: If the validation was successful, deploy the integrated data to a given geocoder environment

The Integrate stage lies at the heart of the process and our change proposal mostly affects this stage.

## Into the Heart of Integrate: Block Assignment

Geocoder data integration is primarily about tying the latest candidate reference addresses to the latest version of the reference road network (e.g., BC Digital Road Atlas) and deriving address ranges.

Each candidate reference address must refer to a road segment (or block) in the DRA and that road segment must have a left or right locality name that is identical to the locality name in the candidate reference address. For *2201 Kaslo Creek South Fork Rd, Kaslo, BC*, the DRA must have a road segment named *Kaslo Creek South Fork Rd* and the left or right locality name for that road segment must be *Kaslo*.

The address block assignment process finds the DRA road segment with the same street and locality names as our candidate reference address then creates an access point along the curb of the road segment at the nearest point to the site point.

A candidate reference address is rejected for any of the following reasons:

1. The DRA has no road segment that contains matching street and locality names.
2. There is no matching road segment within 2km of the candidate reference address point.

## Into the Heart of Integrate: Address Range Generation

Address ranges are derived from the minimum and maximum civic numbers assigned to each block face. Here's a hypothetical example of civic numbers assigned to three consecutive blocks:

| Block 1 | Block 2 | Block 3 |
|---------|---------|---------|
| 7 | 207 | |
| 11 | 217 | |
| 17 | 243 | |
| 43 | 297 | |
| 99 | | |
| | 210 | |
| | 220 | |
| | 240 | |
| | 280 | |
| 4 | | |
| 10 | | |
| 18 | | |
| 48 | | |
| 96 | | |

Here are the address ranges expressed as anchor points which are minimum and maximum civic numbers in bold in each block:

| Block 1 | Block 2 | Block 3 |
|---------|---------|---------|
| **7** | **207** | |
| 7 | 207 | |
| 11 | 217 | |
| 17 | 243 | |
| 43 | 297 | |
| 99 | **297** | |
| **4** | 210 | |
| 10 | 220 | |
| 18 | 240 | |
| 48 | 280 | |
| **96** | | |

The address range generator will stretch the anchor points to the theoretical minimum and maximum:

| Block 1 | Block 2 | Block 3 |
|---------|---------|---------|
| **1** | **201** | |
| 7 | 207 | |
| 11 | 217 | |
| 17 | 243 | |
| 43 | 297 | |
| 97 | **299** | |
| **2** | 210 | |
| 4 | 220 | |
| 10 | 240 | |
| 18 | 280 | |
| 98 | | |
| **200** | | |
| 210 | | |
| 220 | | |
| 240 | | |
| 280 | | |
| **298** | | |

The range generator will also fill in gaps in the address fabric:

| Block 1 | Block 2 | Block 3 |
|---------|---------|---------|
| **1** | **201** | |
| 7 | 101 | |
| 11 | 199 | |
| 17 | 207 | |
| 43 | 217 | |
| 97 | 243 | |
| **99** | 297 | |
| **2** | **299** | |
| 4 | 210 | |
| 10 | 220 | |
| 18 | 240 | |
| 48 | 280 | |
| 96 | **200** | |
| **98** | | |
| **200** | | |
| 210 | | |
| 220 | | |
| 240 | | |
| 280 | | |
| **298** | | |

If a block has even numbers on one side and odd numbers on the other, and one of the sides has no civic numbers, the range generator will fill in the missing side appropriately.

## Weaknesses of current implementation

The current geocoder data integration process has three main weaknesses:

- It is too fussy. The current process requires many manual steps that require careful typing and lots of visual confirmation.
- It takes too long. It takes approximately three business days to complete.
- The current data integration pipelines only run the latest version by default. When we need an earlier version we must manually update the pipelines to run.
- Data version should always match up with Geocoder's version. However there is yet a check if the supplied data does not match up Geocoder's version. It could lead to hard bugs.

## Requirements of a new Geocoder Data Integration Process

| Stage | Description | Implementation |
|-------|-------------|----------------|
| Gather | Gather reference road, address, occupant, locality name, parcel, and Electoral Area data from authoritative sources. | Manual download from BC Data Catalogue, websites run by Integrated Cadastral Initiative Society, BC Assessment, GeoBC, and municipalities that have addresses on their open data websites but are not members of the ICI Society. |
| Transform | Transform each reference address dataset from its local schema and format to a single, standard schema and format | One ETL script for each source data format. We use FME and Java for existing ETL scripts. |
| Integrate | Generate block ranges, block anchor points, address access points, and address parcel ids | The current Block Assignment and Address Range Generator (BAARG) is written in Java and works very well. |
| Verify | Verify that the new reference address dataset is globally valid | With the appropriate orchestration tool, an isolated batch geocoder can be preconfigured, deployed and loaded with the appropriate candidate address data for verification. |
| Deploy | If validation is successful, make new reference road network and address list accessible to online and batch geocoders | This process can be automated but should be manually initiated. |

### Data Source Ranking

Data sources are ranked as follows:

1. AddressBC
2. Open data munis
3. BC Assessment

### Address Rejection Criteria

A candidate reference address should be rejected if:

- The given address location is too far from the correct blockface on the given street in the reference road network.
- The access line (e.g., the straight line between a site's location and its access point) crosses one or more non-strata roads.
- The address is a duplicate of an address that came from a higher-ranked data source.

### Verification Requirements

Globally valid means the dataset is:

- **Locality-complete** (e.g., has addresses from every locality)
- **Match-correct** (e.g., all test addresses geocode as expected)
- **Spatially-consistent** (e.g., address locations on every block increase in the same direction as their civic numbers, blockface address ranges don't overlap and increase in the same direction)
- **Version-consistent** (e.g., locality address counts are higher than the previous version of reference data)

A data administrator should review the validation results before deployment of any new data to production.

## Weaknesses of current implementation

The current implementation of the geocoder data integration process needs a dedicated, standalone, batch geocoder that must be loaded with reference data three times during the integration process as follows:

- **Load 1**: Requires the latest road network and no addresses to confirm candidate reference addresses have valid streets within localities.
- **Load 2**: Requires the latest road network and new reference addresses to confirm candidate reference occupants have valid addresses.
- **Load 3**: Requires the latest road network and new reference addresses and occupants to confirm correct handling of test addresses.

The current implementation also needs a standalone Java application which handles address block assignment and range generation and is appropriately named the *Block Assignment and Address Range Generator* (BAARG).

In the new implementation, all integration and verification steps will be moved from separate scripts that call out to the batch geocoder, to a single Java application called Geocodable BC Maker which will have an embedded geocoder. Geocodable BC Maker will also incorporate an enhanced version of the BAARG. This simplifies the data integration architecture by eliminating the need for an external batch geocoder, speeds up the integration process, localizes all integration algorithms into a single component for easier understanding and maintenance, and leaves the task of keeping up with constantly changing data source schemas and formats to easily-updated scripts.

It will be a major challenge to design a parallel architecture for the embedded geocoder so it can process six million addresses (two million reference addresses three times) as fast as the current batch geocoder (e.g., one hour at six million per hour).
