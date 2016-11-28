# Ecological Marine Unit Explorer
Explore our ocean ecosystems with EMUs!

## Description
Using 50 year's worth of aggregated nutrient and physical ocean data from [NOAA](https://www.nodc.noaa.gov/OC5/woa13/), Esri has collaborated with the USGS, the Marine Conservation Institute, NatureServe, the University of Auckland, GRID-Arendal, NOAA, Duke University, the Woods Hole Oceanographic Institution, and many other partners to classify our oceans into 37 statistically distinct [ecological marine units](http://www.esri.com/ecological-marine-units) (EMUs).  Leveraging the [Runtime SDK](https://developers.arcgis.com/) and [ArcGIS](http://www.arcgis.com/home/index.html), we'll show you how this Android mobile app can be used to explore ocean conditions locally and globally. (TODO: Mention how you can add your own data to enhance this rich data set for your own organization!)

## Feature Services
The heart of this application lies in the rich data stores collected by NOAA and analyzed by the scientific collaboration mentioned above.  Over 50 million spatial and non-spatial datapoints are hosted through AGOL [feature services](http://server.arcgis.com/en/server/10.5/publish-services/windows/what-is-a-feature-service-.htm).  All of the read-only feature services in this application were constructed by the [Esri Oceans group](https://esri.maps.arcgis.com/home/user.html?user=esri_oceans), published using [ArcGISPro](https://pro.arcgis.com/en/pro-app/help/sharing/overview/share-with-arcgis-pro.htm) or ArcMap, and made publicly available.

(TODO): Get write or link up from Keith about how these services were put together)

An [ArcGISTiledLayer](https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/layers/ArcGISTiledLayer.html) is used to display the ocean surface EMUs on top of a ocean basemap.  The [tiled layer](https://developers.arcgis.com/android/latest/guide/layers.htm), a cached map service containing pre-generated raster tiles, represents over 670,000 features and is used instead of a FeatureLayer for performance reasons.

```java
// Start with an ocean basemap, centered on the 
// Galapagos Islands at a pre-defined level of detail
ArcGISMap map =  new ArcGISMap(Basemap.Type.OCEANS, GALAPAGOS_LAT, GALAPAGOS_LONG, 4  );
// Attach the map the MapView
mapView.setMap(map)

// Define the EMU Ocean Surface layer
ArcGISTiledLayer layer = 
  new ArcGISTiledLayer("http://esri.maps.arcgis.com/home/item.html?id=d2db1dbd6d2742a38fe69506029b83ac");
// Add the operational layer to the map
map.getOperationalLayers().add(layer);
```

A number of other data sources are consumed in the app but not loaded in the map view.  [ServiceFeatureTables](https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/layers/ArcGISTiledLayer.html) provide summary and detail data for given EMU layers and water columns which are displayed in the app as charts and custom graphics in views separate from the map view.

```java
// Provision a feature table
ServiceFeatureTable serviceFeatureTable = new  
  ServiceFeatureTable("http://utility.arcgis.com/usrsvcs/servers/dbb13dad900d4014b0611358602723dd/rest/services/EMU_Point_Mesh_Cluster/MapServer/0")
```

## Querying Feature Tables
In the app, spatial and non-spatial feature tables are queried.  Spatial queries are used when the user interacts with the map -  the screen location is converted to a geolocation and service feature tables are queried.

```java
// Convert a tapped screen location to a geo location
// by overriding the onSingleTapConfirmed method
// of the MapTouchListener
public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
      android.graphics.Point screenPoint = new android.graphics.Point((int) motionEvent.getX(),
          (int) motionEvent.getY());
      Point geoPoint = mapView.screenToLocation(screenPoint);
```
Given the geo located point, a buffer is created around the point and an envelope is calculated before querying the feature table.

```java
Polygon bufferedLocation = GeometryEngine.buffer(geoPoint, BUFFER_SIZE);
PolygonBuilder builder = new PolygonBuilder(bufferedLocation);
Envelope envelope = builder.getExtent();
```

Now the spatial feature table can be queried using the derived envelope.

```java
QueryParameters queryParameters = new QueryParameters();
queryParameters.setGeometry(envelope);

// We want all the columns returned from the query
ListenableFuture<FeatureQueryResult> futureResult = 
  serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
futureResult.addDoneListener(new Runnable() {
      @Override public void run() {
        try{
          FeatureQueryResult fqr = futureResult.get();
          if (fqr != null){
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              Feature feature = iterator.next();
              Geometry geometry = feature.getGeometry();
              Map<String,Object> map = feature.getAttributes();
              processResults(map);
          }else{
             handleNullResult()
          }
         }catch (Exception e){
            handleException(e);
         }
     });

```

Non-spatial data like summary statistics and datapoints for charts are retrieved by first putting the table in FeatureRequestMode.MANUAL_CACHE and then querying by calling `pouplateFromServiceAsync`.
```java
ServiceFeatureTable summaryStats = new
  ServiceFeatureTable("http://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/EMU_Summary_Table/FeatureServer/0")
summaryStats.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
summaryStats.loadAsync();
summaryStats.addDoneLoadingListener(new Runnable() {
  @Override public void run() {
    QueryParameters queryParameters = new QueryParameters();
    // Get all the rows in the table
    queryParameters.setWhereClause("1 = 1");
    List<String> outFields = new ArrayList<String>();
    // Get all the fields in the table
    outFields.add("*");
    ListenableFuture<FeatureQueryResult> futureResult =
          summaryStats.populateFromServiceAsync(queryParameters,true,outFields);
    processQueryForEmuStats(futureResult);
   }
  });
    
```

## Data Prep
For traversing depth levels, what considerations were made?  What iterations were done?
What are the steps we went through to prepare data/services?
Clustering points --> Dissolving --> Buffering --> Symbology


