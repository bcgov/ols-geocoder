Yes it can! In a recent study conducted at DataBC, out of one hundred random address waypoints around BC, only one OSM road segment had enough misalignment to cause confusion and only three OSM road names were sufficiently different from the BC Digital Road Atlas (DRA) to cause confusion. The BC DRA is used by the Route Planner to generate routes and turn-by-turn directions. Given that OSM is a free and open base map, you can easily fix any discrepencies in OSM and the fix will be published to the world (and MapBox Streets) within about an hour.

To do the comparison, we selected a handful of random addresses in a given rural area or municipality and asked the Route Planner to find the best route using the addresses as waypoints. For each address in the route, we zoomed right in and looked at how well the route line matched the road in the OSM basemap then compared the spelling of road in OSM to the spelling of our address. Detailed observations are available [here](https://github.com/bcgov/ols-geocoder/blob/gh-pages/itn-osm-comparison.csv)

Here is our methodology if you would like to do the comparison yourself:

1. Start up [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)

2. In the Address tab, enter a rural area or incorporated municipality name and press search icon. You may want to zoom out a bit to include the entire populated area.

![image](https://user-images.githubusercontent.com/11318574/96354333-6753ab80-108a-11eb-90ed-de3d236c8caa.png)


4. Select the Route tab and enter five to ten random waypoints using the jump at random icon (*).

![image](https://user-images.githubusercontent.com/11318574/96354368-d3ceaa80-108a-11eb-890d-9956cf12de2a.png)

5. For each address in the route, zoom to the address in the map by selecting the zoom to waypoint icon then zoom to the lowest possible level.

![image](https://user-images.githubusercontent.com/11318574/96354411-2dcf7000-108b-11eb-85bd-cdf23ad80460.png)


6.  Observe and note the differences between the best route line (in mauve) and the road in the OSM base map.

7. Observe and note the differences between the road name in the OSM base map and the road name in the address.
