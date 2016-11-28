# Ecological Marine Unit Explorer
Explore our ocean ecosystems with EMUs!

## Description
Using 50 year's worth of aggregated nutrient and physical ocean data from [NOAA](https://www.nodc.noaa.gov/OC5/woa13/), Esri has collaborated with the USGS, the Marine Conservation Institute, NatureServe, the University of Auckland, GRID-Arendal, NOAA, Duke University, the Woods Hole Oceanographic Institution, and many other partners to classify our oceans into 37 statistically distinct [ecological marine units](http://www.esri.com/ecological-marine-units) (EMUs).  Leveraging the [Runtime SDK](https://developers.arcgis.com/) and [ArcGIS](http://www.arcgis.com/home/index.html), we'll show you how this Android mobile app can be used to explore ocean conditions locally and globally. (TODO: Mention how you can add your own data to enhance this rich data set for your own organization!)

## Feature Services
The heart of this application lies in the rich data stores collected by NOAA and analyzed by the scientific collaboration mentioned above.  Over 50 million spatial and non-spatial datapoints are hosted through AGOL [feature services](http://server.arcgis.com/en/server/10.5/publish-services/windows/what-is-a-feature-service-.htm).  All of the read-only feature services in this application were constructed by the [Esri Oceans group](https://esri.maps.arcgis.com/home/user.html?user=esri_oceans) and publicly available.

(TODO): Get write or link up from Keith about how these services were put together)

A [tiled layer](https://developers.arcgis.com/android/latest/guide/layers.htm) is used to display the ocean surface EMUs on top of a ocean basemap.

```java
// Start with an ocean basemap, centered on the 
// Galapagos Islands at a pre-defined level of detail
ArcGISMap map =  new ArcGISMap(Basemap.Type.OCEANS, GALAPAGOS_LAT, GALAPAGOS_LONG, 4  );
// Attach the map the MapView
mapView.setMap(map)

// Define the EMU Ocean Surface layer
ArcGISTiledLayer layer = 
  new ArcGISTiledLayer("http://esri.maps.arcgis.com/home/item.html?id=d2db1dbd6d2742a38fe69506029b83ac");
// Add the operational layer to map
graphicOverlay  = new GraphicsOverlay();
mapView.getGraphicsOverlays().add(graphicOverlay);
map.getOperationalLayers().add(layer);


```

Why these types of layers?  Show how they're set up.

## Querying Feature Tables
Querying by geometry
Querying non-spatial data

## Data Prep
For traversing depth levels, what considerations were made?  What iterations were done?
What are the steps we went through to prepare data/services?
Clustering points --> Dissolving --> Buffering --> Symbology


