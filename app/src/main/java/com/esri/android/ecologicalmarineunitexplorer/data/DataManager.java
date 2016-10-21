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
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.*;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.math.DoubleMath;

import java.util.*;

public class DataManager {
  private ServiceFeatureTable mMeshClusterTable;

  private ServiceFeatureTable mMeshPointTable;

  private ServiceFeatureTable mClusterPolygonTable;

  private ServiceFeatureTable mSummaryStats;

  private Context mContext;

  private static DataManager instance = null;

  private WaterColumn mCurrentWaterColumn = null;

  private Map<Integer, EMUStat> summary_table = new HashMap<>();

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


  private DataManager(Context applicationContext){

    mContext = applicationContext;

    mClusterPolygonTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_polygon));

    mMeshClusterTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_mesh_cluster));

    mMeshPointTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_point_mesh));

    mSummaryStats = new ServiceFeatureTable(mContext.getString((R.string.service_emu_summary)));

  }
  /**
   * A singleton that provides access to data services
   * @param applicationContext - Context
   */
  public static DataManager getDataManagerInstance(Context applicationContext){
    if ( instance == null){
      instance = new DataManager(applicationContext);
    }
    return  instance;
  }

  /**
   * Query for water column data at the given geometry
   * @param envelope - represents a buffered geometry around selected point in map
   * @param callback - SummaryCallback used when query is completed
   * @return a WaterColumn at the location within the geometry
   */
  public void queryForEmuAtLocation(Envelope envelope, ServiceApi.SummaryCallback callback){
    QueryParameters queryParameters = new QueryParameters();
    queryParameters.setGeometry(envelope);
    ListenableFuture<FeatureQueryResult> futureResult = mMeshClusterTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
    processQueryForEMuAtLocation(envelope, futureResult, callback);
  }

  /**
   * Query for all EMU summary statistics (~40 rows of data).
   * This is done once and the results cached locally.
   * @param callback - The StatCallback called when query is completed
   */
  public void queryEMUSummaryStatistics(final ServiceApi.StatCallback callback){
    if (summary_table.size() > 0){
      callback.onStatsLoaded();
    }else{
      mSummaryStats.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
      mSummaryStats.loadAsync();
      mSummaryStats.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          QueryParameters queryParameters = new QueryParameters();
          // Get all the rows in the table
          queryParameters.setWhereClause("1 = 1");
          List<String> outFields = new ArrayList<String>();
          // Get all the fields in the table
          outFields.add("*");
          ListenableFuture<FeatureQueryResult> futureResult = mSummaryStats.populateFromServiceAsync(queryParameters,true,outFields);
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
    QueryParameters queryParameters = new QueryParameters();
    queryParameters.setGeometry(point);
    ListenableFuture<FeatureQueryResult> futureResult = mMeshPointTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
    WaterProfile profile = new WaterProfile(point);
    processQueryForEMUColumnProfile(futureResult, callback, profile);
  }


  private void processQueryForEMUColumnProfile(final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.ColumnProfileCallback callback, final WaterProfile profile) {
    futureResult.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          Map<String,Object> map = null;

          FeatureQueryResult fqr = futureResult.get();
          if (fqr != null){
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              Feature feature = iterator.next();
              Geometry geometry = feature.getGeometry();
              map =  feature.getAttributes();
              Measurement measurement = createMeasurement(map);
              profile.addMeasurement(measurement);
            }
          }
          callback.onProfileLoaded(profile);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private Measurement createMeasurement(Map<String, Object> map) {
    Measurement m = new Measurement();

    // EMU name
    Integer name = Integer.parseInt(extractValueFromMap("Cluster37", map));
    m.setEmu(name);

    // Depth
    Double depth = Double.parseDouble(extractValueFromMap("UnitTop", map));
    m.setDepth(depth);

    // Dissolved oxygen
    Double dissolvedOx = Double.parseDouble(extractValueFromMap("dissO2", map));
    m.setDissolvedOxygen(dissolvedOx != null ? dissolvedOx : 0d);

    // Salinity
    Double salinity = Double.parseDouble(extractValueFromMap("salinity", map));
    m.setSalinity(salinity);

    // Temperature
    Double temp = Double.parseDouble(extractValueFromMap("temp", map));
    m.setTemperature(temp);

    // Silicate
    Double silicate = Double.parseDouble(extractValueFromMap("silicate", map));
    m.setSilicate(silicate);

    // Nitrate
    Double nitrate = Double.parseDouble(extractValueFromMap("nitrate", map));
    m.setNitrate(nitrate);

    // Phosphate
    Double phoshpate = Double.parseDouble(extractValueFromMap("phosphate", map));
    m.setPhosphate(phoshpate);

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
        //  Log.i("ProcessQuery", "Query done...");
          FeatureQueryResult fqr = futureResult.get();

          Map<Geometry,WaterColumn> pointWaterColumnMap = new HashMap<Geometry, WaterColumn>();

          if (fqr != null){
        //    Log.i("ProcessQuery", "Processing features...");

            List<EMUObservation> emuObservations = new ArrayList<EMUObservation>();
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              Feature feature = iterator.next();
              Geometry geometry = feature.getGeometry();
              Map<String,Object> map = feature.getAttributes();
              EMUObservation observation = createEMUObservation(map);
              emuObservations.add(createEMUObservation(map));
            }
            // Now we have a list with zero or more EMUObservations
            // 1.  Create a map of WaterColumn keyed on location
            // 2.  Determine the closest WaterColumn to the envelope.

            ImmutableSet<EMUObservation> immutableSet = ImmutableSet.copyOf(emuObservations);
            Function<EMUObservation, Point> locationFunction = new Function<EMUObservation, Point>() {
              @Nullable @Override public Point apply(EMUObservation observation) {
                return observation.getLocation();
              }
            };
            ImmutableListMultimap< Point, EMUObservation> observationsByLocation = Multimaps.index(immutableSet, locationFunction);
            ImmutableMap<Point,Collection<EMUObservation>> map = observationsByLocation.asMap();
            Set<Point> keys = map.keySet();
            Iterator<Point> pointIterator = keys.iterator();
            while (pointIterator.hasNext()){
              Point p = pointIterator.next();
              WaterColumn waterColumn = new WaterColumn();
              waterColumn.setLocation(p);;
              Collection<EMUObservation> observations = map.get(p);
              for (EMUObservation o : observations){
                waterColumn.addObservation(o);
              }
              pointWaterColumnMap.put(p, waterColumn);
            }
          }

          // If there is more than one water column, we only care about the
          // one closest to the point clicked in the map.
          mCurrentWaterColumn = findClosestWaterColumn(envelope, pointWaterColumnMap);


          // Processing is complete, notify the callback
          callback.onWaterColumnsLoaded(mCurrentWaterColumn);


        }catch (Exception e){
            e.printStackTrace();
        }
      }
    });
  }
  public EMUStat getStatForEMU(int emuName){
    EMUStat stat = null;
    if (summary_table.size() > 0){
      stat = summary_table.get(emuName);
    }
    return stat;
  }

  public Double getMaxTemperatureFromSummary(){
    if (MAX_TEMPATURE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_TEMPATURE == null){
          MAX_TEMPATURE = stat.getTemp_max();
        }else{
          if (stat.getTemp_max() > MAX_TEMPATURE){
            MAX_TEMPATURE = stat.getTemp_max();
          }
        }
      }
    }
    return MAX_TEMPATURE;
  }

  public Double getMinTemperatureFromSummary(){
    if (MIN_TEMPATURE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_TEMPATURE == null){
          MIN_TEMPATURE = stat.getTemp_min();
        }else{
          if (stat.getTemp_min() < MIN_TEMPATURE){
            MIN_TEMPATURE = stat.getTemp_min();
          }
        }
      }
    }
    return MIN_TEMPATURE;
  }

  public Double getMaxSalinityFromSummary(){
    if (MAX_SALINITY  == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_SALINITY == null){
          MAX_SALINITY = stat.getSalinity_max();
        }else{
          if (stat.getSalinity_max() > MAX_SALINITY){
            MAX_SALINITY = stat.getSalinity_max();
          }
        }
      }
    }
    return MAX_SALINITY;
  }

  public Double getMinSalinityFromSummary(){
    if (MIN_SALINITY == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_SALINITY == null){
          MIN_SALINITY = stat.getSalinity_min();
        }else{
          if (stat.getSalinity_min() < MIN_SALINITY){
            MIN_SALINITY = stat.getSalinity_min();
          }
        }
      }
    }
    return MIN_SALINITY;
  }

  public Double getMaxOxygenFromSummary(){
    if (MAX_OXYGEN == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_OXYGEN == null){
          MAX_OXYGEN = stat.getDisso2_max();
        }else{
          if (stat.getDisso2_max() > MAX_OXYGEN){
            MAX_OXYGEN = stat.getDisso2_max();
          }
        }
      }
    }
    return MAX_OXYGEN;

  }
  public Double getMinOxygenFromSummary(){
    if (MIN_OXYGEN == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_OXYGEN == null){
          MIN_OXYGEN = stat.getDisso2_min();
        }else{
          if (stat.getDisso2_min() < MIN_OXYGEN){
            MIN_OXYGEN = stat.getDisso2_min();
          }
        }
      }
    }
    return MIN_OXYGEN;

  }
  public Double getMaxPhosphateFromSummary(){
    if (MAX_PHOSPHATE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_PHOSPHATE == null){
          MAX_PHOSPHATE = stat.getPhosphate_max();
        }else{
          if (stat.getPhosphate_max() > MAX_PHOSPHATE){
            MAX_PHOSPHATE = stat.getPhosphate_max();
          }
        }
      }
    }
    return MAX_PHOSPHATE;
  }

  public Double getMinPhosphateFromSummary(){
    if (MIN_PHOSPHATE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_PHOSPHATE == null){
          MIN_PHOSPHATE = stat.getPhosphate_min();
        }else{
          if (stat.getPhosphate_min() < MIN_PHOSPHATE){
            MIN_PHOSPHATE = stat.getPhosphate_min();
          }
        }
      }
    }
    return MIN_PHOSPHATE;
  }

  public Double getMaxNitrateFromSummary(){
    if (MAX_NITRATE == null) {
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_NITRATE == null){
          MAX_NITRATE = stat.getNitrate_max();
        }else{
          if (stat.getNitrate_max() > MAX_NITRATE){
            MAX_NITRATE = stat.getNitrate_max();
          }
        }
      }
    }
    return MAX_NITRATE;

  }
  public Double getMinNitrateFromSummary(){
    if (MIN_NITRATE == null ){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_NITRATE == null){
          MIN_NITRATE = stat.getNitrate_min();
        }else{
          if (stat.getNitrate_min() < MIN_NITRATE){
            MIN_NITRATE = stat.getNitrate_min();
          }
        }
      }
    }
    return MIN_NITRATE;

  }
  public Double getMaxSilicateFromSummary(){
    if (MAX_SILICATE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MAX_SILICATE == null){
          MAX_SILICATE = stat.getSilicate_max();
        }else{
          if (stat.getSilicate_max() > MAX_SILICATE){
            MAX_SILICATE = stat.getSilicate_max();
          }
        }
      }
    }
    return MAX_SILICATE;

  }
  public Double getMinSilicateFromSummary(){
    if (MIN_SILICATE == null){
      Collection stats = summary_table.values();
      Iterator<EMUStat> iter = stats.iterator();

      while (iter.hasNext()){
        EMUStat stat = iter.next();
        if (MIN_SILICATE == null){
          MIN_SILICATE = stat.getSilicate_min();
        }else{
          if (stat.getSilicate_min() < MIN_SILICATE){
            MIN_SILICATE = stat.getSilicate_min();
          }
        }
      }
    }
    return MIN_SILICATE;

  }
  public WaterColumn getCurrentWaterColumn(){
    return mCurrentWaterColumn;
  }

  /**
   * Parse returned data and create EMUStat items for each returned row
   * @param futureResults - a ListenableFuture<FeatureQueryResult> to process
   * @param callback - a StatCallback called when query processing is complete
   *
   */
  private void processQueryForEmuStats(final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.StatCallback callback){
    futureResult.addDoneListener(new Runnable() {

      @Override public void run() {
        try {
          FeatureQueryResult fqr = futureResult.get();
          if (fqr != null){
            final Iterator<Feature> iterator = fqr.iterator();
            while (iterator.hasNext()){
              Feature feature = iterator.next();
              Map<String,Object> map = feature.getAttributes();
              EMUStat stat = createEMUStat(map);
              summary_table.put(stat.getEmu_name(), stat);
            }
          }
          callback.onStatsLoaded();
        } catch (Exception e) {
          e.printStackTrace();
          // TO DO: Handle errors
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
  private EMUObservation createEMUObservation(Map<String,Object> map){
    EMUObservation observation = new EMUObservation();

    EMU emu = new EMU();
    observation.setEmu(emu);

    // Set emu number
    Integer emuNumber = Integer.parseInt( extractValueFromMap(mContext.getString(R.string.emu_number),map));
    emu.setName(emuNumber);

    // Set descriptive name
    String emuName = extractValueFromMap(mContext.getString(R.string.name_emu),map);

    // Get the physical and nutrient summaries which are concatenated in the emuName
    String [] results = emuName.split(" with ");
    if (results.length == 2){
      emu.setPhysicalSummary(results[0]);
      emu.setNutrientSummary(results[1]);
    }else{
      emu.setPhysicalSummary("not found");
      emu.setNutrientSummary("not found");
    }

    // Set geomorphology base for emu
    String geoBase = extractValueFromMap(mContext.getString(R.string.geo_base),map);
    emu.setGeomorphologyBase(geoBase);

    // Set geomorphology features
    String geoFeatures = extractValueFromMap(mContext.getString(R.string.geo_features),map);
    emu.setGeomorphologyFeatures(geoFeatures);

    // Set the top
    String tString = extractValueFromMap("UnitTop", map);
    observation.setTop(Integer.parseInt(tString));

    // Set the geoLocation
    double x = Double.parseDouble(extractValueFromMap(mContext.getString(R.string.point_x),map));
    double y = Double.parseDouble(extractValueFromMap(mContext.getString(R.string.point_y),map));
    Point point = new Point(x,y);
    observation.setLocation(point);

    // Set the thickness
    observation.setThickness(Integer.parseInt(extractValueFromMap(mContext.getString(R.string.thickness),map)));

    String temp = extractValueFromMap("temp", map);
    if (temp != null && temp.length() > 0){
      observation.setTemperature(Double.parseDouble(temp));
    }

    String salinity = extractValueFromMap("salinity", map);
    if (salinity != null && salinity.length() > 0){
      observation.setSalinity(Double.parseDouble(salinity));
    }

    String dissolvedOx = extractValueFromMap("dissO2", map);
    if (dissolvedOx != null  && dissolvedOx.length() > 0 ){
      observation.setOxygen(Double.parseDouble(dissolvedOx));
    }

    String phosphate = extractValueFromMap("phosphate", map);
    if (phosphate != null && phosphate.length() > 0 ){
      observation.setPhosphate(Double.parseDouble(phosphate));
    }

    String silicate = extractValueFromMap("silicate", map);
    if (silicate !=  null  && silicate.length() > 0 ){
      observation.setSilicate(Double.parseDouble(silicate));
    }

    String nitrate = extractValueFromMap("nitrate", map);
    if (nitrate != null  && nitrate.length() > 0 ){
      observation.setNitrate(Double.parseDouble(nitrate));
    }

    //Log.i("Observation", "observation: " + observation.toString());
    return observation;
  }
  private EMUStat createEMUStat(Map<String,Object> map){
    EMUStat stat = new EMUStat();

    stat.setEmu_name(Integer.parseInt(extractValueFromMap("Cluster37", map)));

    String min_temp = extractValueFromMap("MIN_temp", map);
    if (min_temp != null && min_temp.length() > 0 ){
      stat.setTemp_min(Double.parseDouble(min_temp));
    }
    String max_temp = extractValueFromMap("MAX_temp", map);
    if (max_temp != null  && max_temp.length() > 0 ){
      stat.setTemp_max(Double.parseDouble(max_temp));
    }
    String mean_temp = extractValueFromMap("MEAN_temp", map);
    if (mean_temp != null  && mean_temp.length() > 0 ){
      stat.setTemp_mean(Double.parseDouble(mean_temp));
    }

    String min_salinity = extractValueFromMap("MIN_salinity", map);
    if (min_salinity != null  && min_salinity.length() > 0 ){
      stat.setSalinity_min(Double.parseDouble(min_salinity));
    }
    String max_salinity = extractValueFromMap("MAX_salinity", map);
    if (max_salinity != null && max_salinity.length() > 0 ){
      stat.setSalinity_max(Double.parseDouble(max_salinity));
    }
    String mean_salinity = extractValueFromMap("MEAN_salinity", map);
    if (mean_salinity != null && mean_salinity.length() > 0 ){
      stat.setSalinity_mean(Double.parseDouble(mean_salinity));
    }

    String min_disso2 = extractValueFromMap("MIN_dissO2", map);
    if (min_disso2 != null  && min_disso2.length() > 0){
      stat.setDisso2_min(Double.parseDouble(min_disso2));
    }
    String max_disso2 = extractValueFromMap("MAX_dissO2", map);
    if (max_disso2 != null && max_disso2.length() > 0 ){
      stat.setDisso2_max(Double.parseDouble(max_disso2));
    }
    String mean_disso2 = extractValueFromMap("MEAN_dissO2", map);
    if (mean_disso2 != null && mean_disso2.length() > 0 ){
      stat.setDisso2_mean(Double.parseDouble(mean_disso2));
    }

    String min_phosphate = extractValueFromMap("MIN_phosphate", map);
    if (min_phosphate != null && min_phosphate.length() > 0 ){
      stat.setPhosphate_min(Double.parseDouble(min_phosphate));
    }
    String max_phosphate = extractValueFromMap("MAX_phosphate", map);
    if (max_disso2 != null && max_disso2.length() > 0 ){
      stat.setPhosphate_max(Double.parseDouble(max_phosphate));
    }
    String mean_phosphate = extractValueFromMap("MEAN_phosphate", map);
    if (mean_phosphate != null && mean_phosphate.length() > 0){
      stat.setPhosphate_mean(Double.parseDouble(mean_phosphate));
    }

    String min_silicate = extractValueFromMap("MIN_silicate", map);
    if (min_silicate != null && min_silicate.length() > 0){
      stat.setSilicate_min(Double.parseDouble(min_silicate));
    }
    String max_silicate = extractValueFromMap("MAX_silicate", map);
    if (max_silicate != null && max_silicate.length() > 0 ){
      stat.setSilicate_max(Double.parseDouble(max_silicate));
    }
    String mean_silicate = extractValueFromMap("MEAN_silicate", map);
    if (mean_silicate != null && mean_silicate.length() > 0 ){
      stat.setSilicate_mean(Double.parseDouble(mean_silicate));
    }

    String min_nitrate = extractValueFromMap("MIN_nitrate", map);
    if (min_nitrate != null && min_nitrate.length() > 0 ){
      stat.setNitrate_min(Double.parseDouble(min_nitrate));
    }
    String max_nitrate = extractValueFromMap("MAX_nitrate", map);
    if (max_nitrate != null && max_nitrate.length() > 0 ){
      stat.setNitrate_max(Double.parseDouble(max_nitrate));
    }
    String mean_nitrate = extractValueFromMap("MEAN_nitrate", map);
    if (mean_nitrate != null && mean_nitrate.length()  > 0){
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
  private String extractValueFromMap(@NonNull String columnName, @NonNull Map<String,Object> map){
    String value = "";
    if (map.containsKey(columnName) && map.get(columnName) != null){
      value = map.get(columnName).toString();
    }
    return value;
  }

  private WaterColumn findClosestWaterColumn(final Envelope envelope, final Map<Geometry,WaterColumn> waterColumnMap){
    WaterColumn closestWaterColumn = null;
    if (waterColumnMap.size() == 1){
      WaterColumn[] columns = waterColumnMap.values().toArray(new WaterColumn[1]);
      closestWaterColumn = columns[0];
    }
    if (waterColumnMap.size() > 1){
      Point center = envelope.getCenter();
      LinearUnit linearUnit = new LinearUnit(LinearUnitId.METERS);
      AngularUnit angularUnit = new AngularUnit(AngularUnitId.DEGREES);
      Set<Geometry> geometries = waterColumnMap.keySet();
      Iterator<Geometry> iterator = geometries.iterator();
      double distance = 0;
      List<WaterColumn> waterColumnList = new ArrayList<>();
      while (iterator.hasNext()){
        Geometry geo = iterator.next();
        WaterColumn waterColumn = waterColumnMap.get(geo);
        Point point = (Point) geo;
        Point waterColumnPoint = new Point(point.getX(), point.getY(), center.getSpatialReference());
        GeodeticDistanceResult geodeticDistanceResult = GeometryEngine.distanceGeodetic(center, waterColumnPoint, linearUnit, angularUnit, GeodeticCurveType.GEODESIC);
        double calculatedDistance = geodeticDistanceResult.getDistance();
        waterColumn.setDistanceFrom(calculatedDistance);
        waterColumnList.add(waterColumn);
      //  Log.i("DistanceFrom", "Distance = " + calculatedDistance);
      }
      // Sort water columns
      Collections.sort(waterColumnList);
      closestWaterColumn = waterColumnList.get(0);
      WaterColumn furthers = waterColumnList.get(waterColumnList.size()-1);
     // Log.i("Distances", "Closest = " + closestWaterColumn.getDistanceFrom()+ " furthest =" + furthers.getDistanceFrom() );
    }

    return closestWaterColumn;
  }

}
