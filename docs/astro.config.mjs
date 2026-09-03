import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import starlightOpenAPI, { openAPISidebarGroups } from "starlight-openapi";

export default defineConfig({
	site: "https://bcgov.github.io",
	base: "/ols-geocoder",
	integrations: [
		starlight({
			title: "BC Address Geocoder",
			social: [
				{
					icon: "github",
					label: "GitHub",
					href: "https://github.com/bcgov/ols-geocoder",
				},
			],
			plugins: [
				starlightOpenAPI([
					{
						base: "api",
						schema: "docs/public/openapi.json",
						sidebar: { label: "OpenAPI Reference" },
					},
				]),
			],
			sidebar: [
			{
				label: "Getting Started",
				items: [
					"getting-started/product-vision",
					"getting-started/location-services-by-the-numbers",
					"getting-started/whats-new",
					"getting-started/roadmap",
					"getting-started/faq",
					"getting-started/glossary",
					"getting-started/notice",
					"getting-started/physical-address-conceptual-model",
					"getting-started/bc-address-exchange-schema",
					"getting-started/data-integration-process",
				],
			},
				{
					label: "Batch Geocoder",
					items: [
						"batch-geocoder/registration",
						"batch-geocoder/api-key",
						"batch-geocoder/data-preparation",
						"batch-geocoder/application-guide",
						"batch-geocoder/understanding-results",
						"batch-geocoder/rejected-addresses",
						"batch-geocoder/uat",
					],
				},
				{
					label: "Developer Guide",
					items: [
						"developer-guide",
						"developer-guide/single-line-address-format",
						"developer-guide/match-scoring",
						"developer-guide/match-scoring-reference",
					],
				},
				...openAPISidebarGroups,
				{
					label: "Comparisons",
					items: [
						"comparisons/geocoder-comparison",
						"comparisons/osm-suitability",
					],
				},
			],
		}),
	],
});
