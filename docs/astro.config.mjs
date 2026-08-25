import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";

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
		}),
	],
});
