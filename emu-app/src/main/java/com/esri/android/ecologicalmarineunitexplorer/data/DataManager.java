package com.esri.android.ecologicalmarineunitexplorer.data;
/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;

import java.util.*;

public class DataManager {
  private final ServiceFeatureTable mMeshClusterTable;

  private final ServiceFeatureTable mMeshPointTable;

  private final ServiceFeatureTable mSummaryStats;

  private LocatorTask mLocatorTask = null;

  private final Context mContext;

  private final ServiceFeatureTable mEmuByDepthTable;

  private final FeatureLayer mEmuByDepthLayer;

  private static DataManager instance = null;

  private WaterColumn mCurrentWaterColumn = null;

  private final Collection<Integer> mCachedLayers = new HashSet<>();

  private final SparseArray summary_table = new SparseArray<>();

  private static Double MAX_NITRATE = null;
  private static Double MAX_OXYGEN = null;
  private static Double MAX_PHOSPHATE = null;
  private static Double MAX_SALINITY = null;
  private static Double MAX_SILICATE = null;
  private static Double MAX_TEMPATURE = null;

  private static Double MIN_NITRATE = null;
  private static Double MIN_OXYGEN = null;
  private static Double MIN_PHOSPHATE = null;
  private static Double MIN_SALINITY = null;
  private static Double MIN_SILICATE = null;
  private static Double MIN_TEMPATURE = null;


  private DataManager(final Context applicationContext){

    mContext = applicationContext;

    final ServiceFeatureTable mClusterPolygonTable = new ServiceFeatureTable(
        mContext.getString(R.string.service_emu_polygon));

    mMeshClusterTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_mesh_cluster));

    mMeshPointTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_point_mesh));

    mSummaryStats = new ServiceFeatureTable(mContext.getString((R.string.service_emu_summary)));

    mEmuByDepthTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_by_depth));
    mEmuByDepthTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
    mEmuByDepthLayer = new FeatureLayer(mEmuByDepthTable);
  }
  /**
   * A singleton that provides access to data services
   * @param applicationContext - Context
   */
  public static DataManager getDataManagerInstance(final Context applicationContext){
    if (DataManager.instance == null){
      DataManager.instance = new DataManager(applicationContext);
    }
    return DataManager.instance;
  }

  /**
   * Query for water column data at the given geometry
   * @param envelope - represents a buffered geometry around selected point in map
   * @param callback - SummaryCallback used when query is completed
   */
  public void queryForEmuAtLocation(final Envelope envelope, final ServiceApi.SummaryCallback callback){
    final QueryParameters queryParameters = new QueryParameters();
    queryParameters.setGeometry(envelope);
    final ListenableFuture<FeatureQueryResult> futureResult = mMeshClusterTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
    processQueryForEMuAtLocation(envelope, futureResult, callback);
  }

  /**
   * Query for all EMU summary statistics (~40 rows of data).
   * This is done once and the results cached locally.
   * @param callback - The StatCallback called when query is completed
   */
  public void queryEMUSummaryStatistics(final ServiceApi.StatCallback callback){
    if (summary_table.size() > 0){
      callback.onStatsLoaded(true);
    }else{
      mSummaryStats.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
      mSummaryStats.loadAsync();
      mSummaryStats.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          final QueryParameters queryParameters = new QueryParameters();
          // Get all the rows in the table
          queryParameters.setWhereClause("1 = 1");
          final Collection<String> outFields = new ArrayList<String>();
          // Get all the fields in the table
          outFields.add("*");
          final ListenableFuture<FeatureQueryResult> futureResult = mSummaryStats.populateFromServiceAsync(queryParameters,true,outFields);
          processQueryForEmuStats(futureResult, callback);
        }
      });
    }
  }

  /**
   * Query for data for a specific ocean location from the mesh point table.
   * @param point - a point representing the location of a specific water column
   * @param callback - The ColumnProfileCallback called when query is completed.
   */
  public void queryForEMUColumnProfile(final Point point, final ServiceApi.ColumnProfileCallback callback){
    final QueryParameters queryParameters = new QueryParameters();
    queryParameters.setGeometry(point);
    final ListenableFuture<FeatureQueryResult> futureResult = mMeshPointTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
    final WaterProfile profile = new WaterProfile();
    processQueryForEMUColumnProfile(futureResult, callback, profile);
  }

  /**
   * Query for a location by name
   * @param location - a location name or address
   * @param sr - the desired spatial reference for the geocoded result
   * @param callback - The GeocodingCallback to be called upon completion of the geocoding task
   */
  public void queryForAddress(@NonNull final String location, @NonNull final SpatialReference sr,  final ServiceApi.GeocodingCallback callback){
    // Create Locator parameters from single line address string
    final GeocodeParameters geoParameters = new GeocodeParameters();
    geoParameters.setOutputSpatialReference(sr);
    geoParameters.setMaxResults(2);
    if (mLocatorTask == null){
      mLocatorTask = new LocatorTask(mContext.getString(R.string.geocode_url));
    }
    mLocatorTask.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED){
          final ListenableFuture<List<GeocodeResult>> futureResults = mLocatorTask.geocodeAsync(location, geoParameters);
          futureResults.addDoneListener(new Runnable() {
            @Override public void run() {
              try{
                final List<GeocodeResult> geocodeResults = futureResults.get();
                Log.i("DataManager",  geocodeResults.size() + " geocoding results returned.");
                callback.onGecodeResult(geocodeResults);
              }catch ( final Exception e){
                callback.onGecodeResult(null  );
              }
            }
          });
        }else{
          callback.onGecodeResult(null);
          Log.i("DataManager", "Locator Task failed to load: " + mLocatorTask.getLoadStatus().name());
        }
      }
    });

    mLocatorTask.loadAsync();

  }

  /**
   * Retrieve polygons from cache or download from service
   * @param depth Integer representing a particular depth index
   * @param callback ServiceApi.EMUByDepthCallback
   */
  public void manageEmuPolygonsByDepth(final Integer depth, final ServiceApi.EMUByDepthCallback callback){
    // If depth level is 1, don't download, just default to TiledLayer
    if (depth == 1) {
      mEmuByDepthLayer.setVisible(false);
      return;
    }
    mEmuByDepthLayer.setVisible(true);

    if (mCachedLayers.contains(depth)){
      Log.i("DataManager", "EMU polygons downloaded already for depth " + depth);
      mEmuByDepthLayer.setDefinitionExpression(" Depth = " + depth);
    }else{
      Log.i("DataManager", "Downloading EMU polygons for for depth " + depth);
      queryEmuByDepth(depth, callback);
    }
  }
  /**
   * Query for EMU polygons by depth level
   * @param depth - Integer representing a depth interval
   * @param callback - ServiceApi.EMUByDepthCallback - function called on completion of async retrieval
   */
  public void queryEmuByDepth (final Integer depth, final ServiceApi.EMUByDepthCallback callback){
    final QueryParameters queryParameters = generateEmuByDepthQueryParameters(depth);
    try{
      // Return all the output fields
      final List<String> outFields = Collections.singletonList("*");

      final ListenableFuture<FeatureQueryResult> results =
          mEmuByDepthTable.populateFromServiceAsync(queryParameters,false, outFields);
      results.addDoneListener(new Runnable() {
        @Override public void run() {
          try {
            final FeatureQueryResult fqr = results.get();
            if (fqr.iterator().hasNext()){
              Log.i("DataManager", "FeatureQueryResult found...");
              // Cache the depth level so we don't download
              // the same data again
              mCachedLayers.add(depth);

              // Set the definition expression to show only the depth of interest
              Log.i("DataManager", "Setting definition expression for depth " + depth);
              mEmuByDepthLayer.setDefinitionExpression("Depth = " + depth);

              // Notify caller
              callback.onPolygonsRetrieved(mEmuByDepthLayer);
            }
          } catch (final Exception e) {
            Log.e("DataManager", "Error querying EMU by depth " + e.getMessage());
            callback.onPolygonsRetrieved(null);
          }
        }
      });

    } catch (final Exception e) {
      String additionalInfo = getAdditionalInfo(e);
      if (additionalInfo!=null){
        Log.e("DataManager", "Error query emu by depth :" +  e.getMessage() + " Additional info: " + additionalInfo);
      }else{
        Log.e("DataManager", "Error query emu by depth :" +  e.getMessage());
      }
    }
  }

  /**
   * Prepare the query parameters to query the service
   * by depth
   * @param depth - Integer representing depth level
   * @return QueryParameters
   */
  private static QueryParameters generateEmuByDepthQueryParameters(final int depth){
    final QueryParameters queryParameters = new QueryParameters();
    queryParameters.setWhereClause(" Depth = " + depth);
    return queryParameters;
  }

  /**
   * Retrieve measurements from queried features
   * @param futureResult - ListenableFuture<FeatureQueryResult>
   * @param callback - ServiceApi.ColumnProfileCallback callback
   * @param profile - WaterProfile
   */
  private void processQueryForEMUColumnProfile(final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.ColumnProfileCallback callback, final WaterProfile profile) {
    futureResult.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          Map<String,Object> map;

          final FeatureQueryResult fqr = futureResult.get();
          if (fqr != null){
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              final Feature feature = iterator.next();
              map =  feature.getAttributes();
              final Measurement measurement = createMeasurement(map);
              profile.addMeasurement(measurement);
            }
          }
        } catch (final Exception e) {
          String additionalInfo = getAdditionalInfo(e);
          if (additionalInfo!=null){
            Log.e("DataManager", "No measurements found for water column profile due to error " +  e.getMessage() + " Additional info: " + additionalInfo);
          }else{
            Log.e("DataManager", "No measurements found for water column profile due to error " +  e.getMessage());
          }
        }
        callback.onProfileLoaded(profile);
      }
    });
  }

  /**
   * Build up a Measurement
   * @param map - Map<String,Object>
   * @return Measurement
   */
  private static Measurement createMeasurement(final Map<String, Object> map) {
    final Measurement m = new Measurement();


    // EMU name
    final Integer name = Integer.parseInt(extractValueFromMap("Cluster37", map));
    m.setEmu(name);

    // Depth
    final Double depth = Double.parseDouble(extractValueFromMap("UnitTop", map));
    m.setDepth(depth);

    // Dissolved oxygen
    try{
      final Double dissolvedOx = Double.parseDouble(extractValueFromMap("dissO2", map));
      m.setDissolvedOxygen(dissolvedOx != null ? dissolvedOx : 0d);
    }catch (final NumberFormatException ne){
      m.setDissolvedOxygen(0d);
    }


    // Salinity
    try {
      final Double salinity = Double.parseDouble(extractValueFromMap("salinity", map));
      m.setSalinity(salinity != null ? salinity : 0d);
    }catch (final NumberFormatException ne){
      m.setSalinity(0d);
    }

    // Temperature
    try{
      final Double temp = Double.parseDouble(extractValueFromMap("temp", map));
      m.setTemperature(temp != null ? temp : 0d);
    }catch (final NumberFormatException ne){
      m.setTemperature(0d);
    }


    // Silicate
    try{
      final Double silicate = Double.parseDouble(extractValueFromMap("silicate", map));
      m.setSilicate(silicate != null ? silicate : 0d);
    }catch (final NumberFormatException ne){
      m.setSilicate(0d);
    }


    // Nitrate
    try{
      final Double nitrate = Double.parseDouble(extractValueFromMap("nitrate", map));
      m.setNitrate(nitrate != null ? nitrate : 0d);
    } catch ( final NumberFormatException ne){
      m.setNitrate(0d);
    }


    // Phosphate
    try{
      final Double phoshpate = Double.parseDouble(extractValueFromMap("phosphate", map));
      m.setPhosphate(phoshpate != null ? phoshpate : 0d );
    }catch (final NumberFormatException ne){
      m.setPhosphate(0d);
    }

    return m;
  }

  /**
   * Process the listenable future by creating a WaterColumn for
   * any returned data.
   * @param envelope - an Envelope representing the search area
   * @param futureResult - a ListenableFuture<FeatureQueryResult> to process
   * @param callback  - a SummaryCallback called when query processing is complete
   */
  private void processQueryForEMuAtLocation(final Envelope envelope, final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.SummaryCallback callback){

    futureResult.addDoneListener(new Runnable() {
      @Override public void run() {
        try{
          final FeatureQueryResult fqr = futureResult.get();

          final Map<Geometry,WaterColumn> pointWaterColumnMap = new HashMap<Geometry, WaterColumn>();

          if (fqr != null){

            final Collection<EMUObservation> emuObservations = new ArrayList<EMUObservation>();
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              final Feature feature = iterator.next();
              final Map<String,Object> map = feature.getAttributes();
              final EMUObservation observation = createEMUObservation(map);
              emuObservations.add(createEMUObservation(map));
            }
            // Now we have a list with zero or more EMUObservations
            // 1.  Create a map of WaterColumn keyed on location
            // 2.  Determine the closest WaterColumn to the envelope.

            final ImmutableSet<EMUObservation> immutableSet = ImmutableSet.copyOf(emuObservations);
            final Function<EMUObservation, Point> locationFunction = new Function<EMUObservation, Point>() {
              @Nullable @Override public Point apply(final EMUObservation observation) {
                return observation.getLocation();
              }
            };
            final ImmutableListMultimap< Point, EMUObservation> observationsByLocation = Multimaps.index(immutableSet, locationFunction);
            final ImmutableMap<Point,Collection<EMUObservation>> map = observationsByLocation.asMap();
            final Set<Point> keys = map.keySet();
            final Iterator<Point> pointIterator = keys.iterator();
            while (pointIterator.hasNext()){
              final Point p = pointIterator.next();
              final WaterColumn waterColumn = new WaterColumn();
              waterColumn.setLocation(p);
              final Collection<EMUObservation> observations = map.get(p);
              for (final EMUObservation o : observations){
                waterColumn.addObservation(o);
              }
              pointWaterColumnMap.put(p, waterColumn);
            }

            // If there is more than one water column, we only care about the
            // one closest to the point clicked in the map.
            mCurrentWaterColumn = findClosestWaterColumn(envelope, pointWaterColumnMap);
          }else{
            mCurrentWaterColumn = null;
          }

          // Processing is complete, notify the callback
          callback.onWaterColumnsLoaded(mCurrentWaterColumn);


        }catch (final Exception e){
          String additionalInfo = getAdditionalInfo(e);
          if (additionalInfo!=null){
            Log.e("DataManager", "No measurements found for location due to error " +  e.getMessage() + " Additional info: " + additionalInfo);
          }else{
            Log.e("DataManager", "No measurements found for location due to error " +  e.getMessage());
          }
        }
      }
    });
  }

  /**
   * Get an EMUStat from the summary table.  Returns null for any EMUs with no statistic.
   * @param emuName int representing an EMU name
   * @return EMUStat (Nullable)
   */
  public EMUStat getStatForEMU(final int emuName){
    EMUStat stat = null;
    if (summary_table.size() > 0){
      stat = (EMUStat) summary_table.get(emuName);
    }
    return stat;
  }

  /**
   * Get the maximum temperature value from the summary statistics table
   * @return Double
   */
  public Double getMaxTemperatureFromSummary(){
    if (DataManager.MAX_TEMPATURE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++){
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_TEMPATURE == null){
          DataManager.MAX_TEMPATURE = stat.getTemp_max();
        }else{
          if (stat.getTemp_max() > DataManager.MAX_TEMPATURE){
            DataManager.MAX_TEMPATURE = stat.getTemp_max();
          }
        }
      }
    }
    return DataManager.MAX_TEMPATURE;
  }

  /**
   * Get the minimum temperature value from the summary statistics table
   * @return Double
   */
  public Double getMinTemperatureFromSummary(){
    if (DataManager.MIN_TEMPATURE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++){
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_TEMPATURE == null){
          DataManager.MIN_TEMPATURE = stat.getTemp_min();
        }else{
          if (stat.getTemp_min() < DataManager.MIN_TEMPATURE){
            DataManager.MIN_TEMPATURE = stat.getTemp_min();
          }
        }
      }
    }
    return DataManager.MIN_TEMPATURE;
  }

  /**
   * Get the maximum salinity value from the summary statistics table
   * @return Double
   */
  public Double getMaxSalinityFromSummary(){
    if (DataManager.MAX_SALINITY == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_SALINITY == null){
          DataManager.MAX_SALINITY = stat.getSalinity_max();
        }else{
          if (stat.getSalinity_max() > DataManager.MAX_SALINITY){
            DataManager.MAX_SALINITY = stat.getSalinity_max();
          }
        }
      }
    }
    return DataManager.MAX_SALINITY;
  }

  /**
   * Get the minimum salinity value from the summary statistics table
   * @return Double
   */
  public Double getMinSalinityFromSummary(){
    if (DataManager.MIN_SALINITY == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_SALINITY == null){
          DataManager.MIN_SALINITY = stat.getSalinity_min();
        }else{
          if (stat.getSalinity_min() < DataManager.MIN_SALINITY){
            DataManager.MIN_SALINITY = stat.getSalinity_min();
          }
        }
      }
    }
    return DataManager.MIN_SALINITY;
  }

  /**
   * Get the maximum oxygen value from the summary statistics table
   * @return Double
   */
  public Double getMaxOxygenFromSummary(){
    if (DataManager.MAX_OXYGEN == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_OXYGEN == null){
          DataManager.MAX_OXYGEN = stat.getDisso2_max();
        }else{
          if (stat.getDisso2_max() > DataManager.MAX_OXYGEN){
            DataManager.MAX_OXYGEN = stat.getDisso2_max();
          }
        }
      }
    }
    return DataManager.MAX_OXYGEN;
  }

  /**
   * Get the minimum oxygen value from the summary statistics table
   * @return Double
   */
  public Double getMinOxygenFromSummary(){
    if (DataManager.MIN_OXYGEN == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_OXYGEN == null){
          DataManager.MIN_OXYGEN = stat.getDisso2_min();
        }else{
          if (stat.getDisso2_min() < DataManager.MIN_OXYGEN){
            DataManager.MIN_OXYGEN = stat.getDisso2_min();
          }
        }
      }
    }
    return DataManager.MIN_OXYGEN;
  }

  /**
   * Get the maximum phosphate value from the summary statistics table
   * @return Double
   */
  public Double getMaxPhosphateFromSummary(){
    if (DataManager.MAX_PHOSPHATE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_PHOSPHATE == null) {
          DataManager.MAX_PHOSPHATE = stat.getPhosphate_max();
        } else {
          if (stat.getPhosphate_max() > DataManager.MAX_PHOSPHATE) {
            DataManager.MAX_PHOSPHATE = stat.getPhosphate_max();
          }
        }
      }
    }
    return DataManager.MAX_PHOSPHATE;
  }

  /**
   * Get the minimum phosphate value from the summary statistics table
   * @return Double
   */
  public Double getMinPhosphateFromSummary(){
    if (DataManager.MIN_PHOSPHATE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_PHOSPHATE == null){
          DataManager.MIN_PHOSPHATE = stat.getPhosphate_min();
        }else{
          if (stat.getPhosphate_min() < DataManager.MIN_PHOSPHATE){
            DataManager.MIN_PHOSPHATE = stat.getPhosphate_min();
          }
        }
      }
    }
    return DataManager.MIN_PHOSPHATE;
  }

  /**
   * Get the maximum nitrate value from the summary statistics table
   * @return Double
   */
  public Double getMaxNitrateFromSummary(){
    if (DataManager.MAX_NITRATE == null) {
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_NITRATE == null){
          DataManager.MAX_NITRATE = stat.getNitrate_max();
        }else {
          if (stat.getNitrate_max() > DataManager.MAX_NITRATE) {
            DataManager.MAX_NITRATE = stat.getNitrate_max();
          }
        }
      }
    }
    return DataManager.MAX_NITRATE;
  }

  /**
   * Get the minimum nitrate value from the summary statistics table
   * @return Double
   */
  public Double getMinNitrateFromSummary(){
    if (DataManager.MIN_NITRATE == null ){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_NITRATE == null){
          DataManager.MIN_NITRATE = stat.getNitrate_min();
        }else{
          if (stat.getNitrate_min() < DataManager.MIN_NITRATE){
            DataManager.MIN_NITRATE = stat.getNitrate_min();
          }
        }
      }
    }
    return DataManager.MIN_NITRATE;
  }

  /**
   * Get the maximum silicate value from the summary statistics table
   * @return Double
   */
  public Double getMaxSilicateFromSummary(){
    if (DataManager.MAX_SILICATE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MAX_SILICATE == null){
          DataManager.MAX_SILICATE = stat.getSilicate_max();
        }else{
          if (stat.getSilicate_max() > DataManager.MAX_SILICATE){
            DataManager.MAX_SILICATE = stat.getSilicate_max();
          }
        }
      }
    }
    return DataManager.MAX_SILICATE;
  }

  /**
   * Get the minimum silicate value from the summary statistics table
   * @return Double
   */
  public Double getMinSilicateFromSummary(){
    if (DataManager.MIN_SILICATE == null){
      final int x = summary_table.size();
      for (int i =0; i < x; i++) {
        final EMUStat stat = (EMUStat) summary_table.valueAt(i);
        if (DataManager.MIN_SILICATE == null){
          DataManager.MIN_SILICATE = stat.getSilicate_min();
        }else{
          if (stat.getSilicate_min() < DataManager.MIN_SILICATE){
            DataManager.MIN_SILICATE = stat.getSilicate_min();
          }
        }
      }
    }
    return DataManager.MIN_SILICATE;

  }

  /**
   * Return the current WaterColumn
   * @return WaterColumn
   */
  public WaterColumn getCurrentWaterColumn(){
    return mCurrentWaterColumn;
  }

  /**
   * Parse returned data and create EMUStat items for each returned row
   * @param futureResult - a ListenableFuture<FeatureQueryResult> to process
   * @param callback - a StatCallback called when query processing is complete
   *
   */
  private void processQueryForEmuStats(final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.StatCallback callback){
    futureResult.addDoneListener(new Runnable() {

      @Override public void run() {
        try {
          final FeatureQueryResult fqr = futureResult.get();
          if (fqr != null){
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              final Feature feature = iterator.next();
              final Map<String,Object> map = feature.getAttributes();
              final EMUStat stat = createEMUStat(map);
              summary_table.put(stat.getEmu_name(), stat);
            }
          }
          callback.onStatsLoaded(true);
        } catch (final Exception e) {
          callback.onStatsLoaded(false);
          String additionalInfo = getAdditionalInfo(e);
          if (additionalInfo!=null){
            Log.e("DataManager", "There was a problem querying for EMU statistics " +  e.getMessage() + " Additional info: " + additionalInfo);
          }else{
            Log.e("DataManager", "There was a problem querying for EMU statistics " +  e.getMessage());
          }
        }
      }
    });

  }

  /**
   * Given a Map containing strings as keys and objects as values,
   * create an EMUObservation
   * @param map Map<String,Object> representing field values indexed by field names
   * @return an EMUObservation for map.
   */
  private EMUObservation createEMUObservation(final Map<String,Object> map){
    final EMUObservation observation = new EMUObservation();

    final EMU emu = new EMU();
    observation.setEmu(emu);

    // Set emu number
    final Integer emuNumber = Integer.parseInt( extractValueFromMap(mContext.getString(R.string.emu_number),map));
    emu.setName(emuNumber);

    // Set descriptive name
    final String emuName = extractValueFromMap(mContext.getString(R.string.name_emu),map);

    // Get the physical and nutrient summaries which are concatenated in the emuName
    final String [] results = emuName.split(" with ");
    if (results.length == 2){
      emu.setPhysicalSummary(results[0]);
      emu.setNutrientSummary(results[1]);
    }else{
      emu.setPhysicalSummary("not found");
      emu.setNutrientSummary("not found");
    }

    // Set geomorphology base for emu
    final String geoBase = extractValueFromMap(mContext.getString(R.string.geo_base),map);
    emu.setGeomorphologyBase(geoBase);

    // Set geomorphology features
    final String geoFeatures = extractValueFromMap(mContext.getString(R.string.geo_features),map);
    emu.setGeomorphologyFeatures(geoFeatures);

    // Set the top
    final String tString = extractValueFromMap("UnitTop", map);
    observation.setTop(Integer.parseInt(tString));

    // Set the geoLocation
    final double x = Double.parseDouble(extractValueFromMap(mContext.getString(R.string.point_x),map));
    final double y = Double.parseDouble(extractValueFromMap(mContext.getString(R.string.point_y),map));
    final Point point = new Point(x,y);
    observation.setLocation(point);

    // Set the thickness
    observation.setThickness(Integer.parseInt(extractValueFromMap(mContext.getString(R.string.thickness),map)));

    final String temp = extractValueFromMap("temp", map);
    if (temp != null && !temp.isEmpty()){
      observation.setTemperature(Double.parseDouble(temp));
    }

    final String salinity = extractValueFromMap("salinity", map);
    if (salinity != null && !salinity.isEmpty()){
      observation.setSalinity(Double.parseDouble(salinity));
    }

    final String dissolvedOx = extractValueFromMap("dissO2", map);
    if (dissolvedOx != null  && !dissolvedOx.isEmpty()){
      observation.setOxygen(Double.parseDouble(dissolvedOx));
    }

    final String phosphate = extractValueFromMap("phosphate", map);
    if (phosphate != null && !phosphate.isEmpty()){
      observation.setPhosphate(Double.parseDouble(phosphate));
    }

    final String silicate = extractValueFromMap("silicate", map);
    if (silicate !=  null  && !silicate.isEmpty()){
      observation.setSilicate(Double.parseDouble(silicate));
    }

    final String nitrate = extractValueFromMap("nitrate", map);
    if (nitrate != null  && !nitrate.isEmpty()){
      observation.setNitrate(Double.parseDouble(nitrate));
    }

    return observation;
  }

  /**
   * Create an EMUStat object from given a map of key value pairs
   * @param map Map<String,Object></String,Object>
   * @return EMUStat
   */
  private static EMUStat createEMUStat(final Map<String, Object> map){
    final EMUStat stat = new EMUStat();

    stat.setEmu_name(Integer.parseInt(extractValueFromMap("Cluster37", map)));

    final String min_temp = extractValueFromMap("MIN_temp", map);
    if (min_temp != null && !min_temp.isEmpty()){
      stat.setTemp_min(Double.parseDouble(min_temp));
    }
    final String max_temp = extractValueFromMap("MAX_temp", map);
    if (max_temp != null  && !max_temp.isEmpty()){
      stat.setTemp_max(Double.parseDouble(max_temp));
    }
    final String mean_temp = extractValueFromMap("MEAN_temp", map);
    if (mean_temp != null  && !mean_temp.isEmpty()){
      stat.setTemp_mean(Double.parseDouble(mean_temp));
    }

    final String min_salinity = extractValueFromMap("MIN_salinity", map);
    if (min_salinity != null  && !min_salinity.isEmpty()){
      stat.setSalinity_min(Double.parseDouble(min_salinity));
    }
    final String max_salinity = extractValueFromMap("MAX_salinity", map);
    if (max_salinity != null && !max_salinity.isEmpty()){
      stat.setSalinity_max(Double.parseDouble(max_salinity));
    }
    final String mean_salinity = extractValueFromMap("MEAN_salinity", map);
    if (mean_salinity != null && !mean_salinity.isEmpty()){
      stat.setSalinity_mean(Double.parseDouble(mean_salinity));
    }

    final String min_disso2 = extractValueFromMap("MIN_dissO2", map);
    if (min_disso2 != null  && !min_disso2.isEmpty()){
      stat.setDisso2_min(Double.parseDouble(min_disso2));
    }
    final String max_disso2 = extractValueFromMap("MAX_dissO2", map);
    if (max_disso2 != null && !max_disso2.isEmpty()){
      stat.setDisso2_max(Double.parseDouble(max_disso2));
    }
    final String mean_disso2 = extractValueFromMap("MEAN_dissO2", map);
    if (mean_disso2 != null && !mean_disso2.isEmpty()){
      stat.setDisso2_mean(Double.parseDouble(mean_disso2));
    }

    final String min_phosphate = extractValueFromMap("MIN_phosphate", map);
    if (min_phosphate != null && !min_phosphate.isEmpty()){
      stat.setPhosphate_min(Double.parseDouble(min_phosphate));
    }
    final String max_phosphate = extractValueFromMap("MAX_phosphate", map);
    if (max_disso2 != null && !max_disso2.isEmpty()){
      stat.setPhosphate_max(Double.parseDouble(max_phosphate));
    }
    final String mean_phosphate = extractValueFromMap("MEAN_phosphate", map);
    if (mean_phosphate != null && !mean_phosphate.isEmpty()){
      stat.setPhosphate_mean(Double.parseDouble(mean_phosphate));
    }

    final String min_silicate = extractValueFromMap("MIN_silicate", map);
    if (min_silicate != null && !min_silicate.isEmpty()){
      stat.setSilicate_min(Double.parseDouble(min_silicate));
    }
    final String max_silicate = extractValueFromMap("MAX_silicate", map);
    if (max_silicate != null && !max_silicate.isEmpty()){
      stat.setSilicate_max(Double.parseDouble(max_silicate));
    }
    final String mean_silicate = extractValueFromMap("MEAN_silicate", map);
    if (mean_silicate != null && !mean_silicate.isEmpty()){
      stat.setSilicate_mean(Double.parseDouble(mean_silicate));
    }

    final String min_nitrate = extractValueFromMap("MIN_nitrate", map);
    if (min_nitrate != null && !min_nitrate.isEmpty()){
      stat.setNitrate_min(Double.parseDouble(min_nitrate));
    }
    final String max_nitrate = extractValueFromMap("MAX_nitrate", map);
    if (max_nitrate != null && !max_nitrate.isEmpty()){
      stat.setNitrate_max(Double.parseDouble(max_nitrate));
    }
    final String mean_nitrate = extractValueFromMap("MEAN_nitrate", map);
    if (mean_nitrate != null && !mean_nitrate.isEmpty()){
      stat.setNitrate_mean(Double.parseDouble(mean_nitrate));
    }


    return  stat;
  }

  /**
   * Get the string value from the map given the column name
   * @param columnName - a non-null String representing the name of the column in the map
   * @param map - a map of objects indexed by string
   * @return  - the string value, may be empty but not null.
   */
  private static String extractValueFromMap(@NonNull final String columnName, @NonNull final Map<String, Object> map){
    String value = "";
    if (map.containsKey(columnName) && map.get(columnName) != null){
      value = map.get(columnName).toString();
    }
    return value;
  }

  /**
   * Find the closest WaterColumn to the center of the given Envelope
   * @param envelope - Envelope
   * @param waterColumnMap - Map<Geometry,WaterColumn> map of WaterColumn values keyed by Geometry objects.
   * @return WaterColumn
   */
  private static WaterColumn findClosestWaterColumn(final Envelope envelope,
      final Map<Geometry, WaterColumn> waterColumnMap){
    WaterColumn closestWaterColumn = null;
    if (waterColumnMap.size() == 1){
      final WaterColumn[] columns = waterColumnMap.values().toArray(new WaterColumn[1]);
      closestWaterColumn = columns[0];
    }
    if (waterColumnMap.size() > 1){
      final Point center = envelope.getCenter();
      final LinearUnit linearUnit = new LinearUnit(LinearUnitId.METERS);
      final AngularUnit angularUnit = new AngularUnit(AngularUnitId.DEGREES);
      final Set<Geometry> geometries = waterColumnMap.keySet();
      final Iterator<Geometry> iterator = geometries.iterator();
      final List<WaterColumn> waterColumnList = new ArrayList<>();
      while (iterator.hasNext()){
        final Geometry geo = iterator.next();
        final WaterColumn waterColumn = waterColumnMap.get(geo);
        final Point point = (Point) geo;
        final Point waterColumnPoint = new Point(point.getX(), point.getY(), center.getSpatialReference());
        final GeodeticDistanceResult geodeticDistanceResult = GeometryEngine.distanceGeodetic(center, waterColumnPoint, linearUnit, angularUnit, GeodeticCurveType.GEODESIC);
        final double calculatedDistance = geodeticDistanceResult.getDistance();
        waterColumn.setDistanceFrom(calculatedDistance);
        waterColumnList.add(waterColumn);
      }
      // Sort water columns
      Collections.sort(waterColumnList);
      closestWaterColumn = waterColumnList.get(0);
    }

    return closestWaterColumn;
  }

  /**
   * Obtain any additional information about given exception
   * @param e Exception
   * @return String
   */
  private static String getAdditionalInfo(Exception e){
    String additionalInfo =null;
    if (e.getCause()!= null && e.getCause().getMessage()!=null) {
      additionalInfo = e.getCause().getMessage();
    }
    return additionalInfo;
  }

}
