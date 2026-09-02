---
title: BC Address Geocoder
description: Documentation for the BC Address Geocoder.
---

The BC Address Geocoder provides REST APIs for address cleaning, correction, completion, geocoding, and reverse geocoding. The BC Address Geocoder has processed over one billion addresses since its initial release in 2013. To see it in use by an application, visit [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html).

For more information about using the BC Address Geocoder and incorporating it into your automated workflows, please consult the following documents:

## Getting Started

| Document | Description | Audience |
|----------|-------------|----------|
| [Open Location Services Product Vision](getting-started/product-vision) | Explains why BC built a geocoder and other products in its Open Location Services product line | Everyone |
| [Location Services by the Numbers](getting-started/location-services-by-the-numbers) | Usage and adoption statistics | Everyone |
| [License](https://github.com/bcgov/ols-geocoder/blob/main/LICENSE) | License under which all documents and source code in this repository are released | Everyone |
| [Copyright Notices](getting-started/notice) | Copyright notices of all software packaged used by this repository | Everyone |
| [What's New](getting-started/whats-new) | What's new in latest release of Geocoder | Everyone |
| [Geocoder Roadmap](getting-started/roadmap) | Planned major enhancements to the geocoder | Everyone |
| [FAQ](getting-started/faq) | Frequently Asked Questions | Everyone |
| [Glossary of Geocoder Terms](getting-started/glossary) | Defines all technical terms used by the geocoder | Geocoder clients, app developers, address data suppliers |

## Batch Geocoder

| Document | Description | Audience |
|----------|-------------|----------|
| [How to Register to Use the Batch Geocoder](batch-geocoder/registration) | If you have a very long list of addresses, here's the three-step process to getting access to the batch geocoder | Anyone in a crown corporation, local or federal government, local health authority, or university |
| [Preparing Your Address File for Batch Geocoding](batch-geocoder/data-preparation) | An example-driven introduction to the input formats supported by the Address List Editor and the batch geocoder | Everyone |
| [Batch Geocoder Application Guide](batch-geocoder/application-guide) | Step by step tutorial on submitting your address list to the batch geocoder | Everyone |
| [Understanding Batch Geocoder Output](batch-geocoder/understanding-results) | An example-driven introduction to the geocoder scoring system and the many fault messages that can help you pinpoint data issues | Everyone |
| [Rejected Addresses](batch-geocoder/rejected-addresses) | Common types of bad addresses that earlier versions of the geocoder didn't handle well | Geocoder clients, app developers |
| [Batch Geocoder User Acceptance Test Plan](batch-geocoder/uat) | Plan for user acceptance testing of new versions of the geocoder | Batch geocoder clients |

## Developer Guide

| Document | Description | Audience |
|----------|-------------|----------|
| [Geocoder Developer Guide](developer-guide) | Learn the online geocoder API through a series of examples | App developers |
| [Single-Line Address Format](developer-guide/single-line-address-format) | The single-string format of addresses supported by the geocoder | Geocoder clients, app developers, address data suppliers |
| [Understanding Address Match Scoring](developer-guide/match-scoring) | Explains how address matches are ranked by the geocoder | Geocoder clients, app developers, address data suppliers |
| [Geocoder Address Match Scoring Reference](developer-guide/match-scoring-reference) | Defines how address matches are ranked by the geocoder | Geocoder clients, app developers, address data suppliers |

## Comparisons

| Document | Description | Audience |
|----------|-------------|----------|
| [Comparative Address Geocoder Match Accuracy Study](comparisons/geocoder-comparison) | A comparison between Bing Maps, ESRI, Google Maps, and Here | Everyone |
| [OpenStreetMap Suitability Study](comparisons/osm-suitability) | Is OSM a suitable base map to display addresses from the BC Address Geocoder? | App developers |

## Addressing Standards & Data

| Document | Description | Audience |
|----------|-------------|----------|
| [BC Physical Address Exchange Schema](getting-started/bc-address-exchange-schema) | Defines a schema for the exchange of reference addresses between address authorities and geocoding service providers | Address authorities, app developers, geocoder developers and operators |
| [Physical Address Conceptual Model](getting-started/physical-address-conceptual-model) | Defines what a physical address is and how it differs from a mailing address | Address authorities, address data suppliers, app developers, geocoder clients |
| [Geocoder Data Integration Process](getting-started/data-integration-process) | An overview of geocoder data integration process and a proposal to improve its implementation | Geocoder developers |

## Reference Data & Configuration

| Document | Description | Audience |
|----------|-------------|----------|
| [Geocoder Configuration Files](https://github.com/bcgov/ols-geocoder/tree/main/docs/config/bc) | Configuration files for the BC Address Geocoder including abbreviations, unit designators, and locality aliases | Geocoder developers and operators, other Canadian jurisdictions |
| [Test Addresses](https://github.com/bcgov/ols-geocoder/blob/main/docs/atp_addresses.csv) | List of test addresses for QA of new versions of the geocoder | Geocoder clients, app developers |
| [BC Address Coverage Test Addresses](https://github.com/bcgov/ols-geocoder/blob/main/docs/sites_bc.csv) | One address from every locality in BC for verification of Geocodable BC | BC Geocoder administrators, geocoder prospects |
| [OSM Suitability Test Addresses](https://github.com/bcgov/ols-geocoder/blob/main/docs/itn-osm-comparison.csv) | Random addresses selected for use in the OSM suitability study | App developers |

## Installation

| Document | Description | Audience |
|----------|-------------|----------|
| [OLS Geocoder Installation Instructions](https://github.com/bcgov/ols-geocoder/blob/main/INSTALL.md) | Instructions for installing OLS-Geocoder in your own environment | Developers |
