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

import java.util.*;
import java.util.concurrent.ExecutionException;

public class DataManager {
  private ServiceFeatureTable mMeshClusterTable;

  private ServiceFeatureTable mMeshPointTable;

  private ServiceFeatureTable mClusterPolygonTable;

  private ServiceFeatureTable mSummaryStats;

  private Context mContext;

  private static DataManager instance = null;

  private WaterColumn mCurrentWaterColumn = null;

  private Map<Integer, EMUStat> summary_table = new HashMap<>();


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

  public void getStatisticsForEMUs(final ServiceApi.StatCallback callback){
    if (summary_table.size() > 0){
      callback.onStatsLoaded();
    }else{
      mSummaryStats.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
      mSummaryStats.loadAsync();
      mSummaryStats.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          QueryParameters queryParameters = new QueryParameters();
          queryParameters.setWhereClause("1 = 1");
          List<String> outFields = new ArrayList<String>();
          outFields.add("*");
          ListenableFuture<FeatureQueryResult> futureResult = mSummaryStats.populateFromServiceAsync(queryParameters,true,outFields);
          processQueryForEmuStats(futureResult, callback);
        }
      });
    }

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
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getTemp_max();
      }else{
        if (stat.getTemp_max() > max){
          max = stat.getTemp_max();
        }
      }
    }
    return max;
  }
  public Double getMinTemperatureFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getTemp_min();
      }else{
        if (stat.getTemp_min() < min){
          min = stat.getTemp_min();
        }
      }
    }
    return min;
  }
  public Double getMaxSalinityFromSummary(){
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getSalinity_max();
      }else{
        if (stat.getSalinity_max() > max){
          max = stat.getSalinity_max();
        }
      }
    }
    return max;
  }
  public Double getMinSalinityFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getSalinity_min();
      }else{
        if (stat.getSalinity_min() < min){
          min = stat.getSalinity_min();
        }
      }
    }
    return min;
  }
  public Double getMaxOxygenFromSummary(){
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getDisso2_max();
      }else{
        if (stat.getDisso2_max() > max){
          max = stat.getDisso2_max();
        }
      }
    }
    return max;
  }
  public Double getMinOxygenFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getDisso2_min();
      }else{
        if (stat.getDisso2_min() < min){
          min = stat.getDisso2_min();
        }
      }
    }
    return min;
  }
  public Double getMaxPhosphateFromSummary(){
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getPhosphate_max();
      }else{
        if (stat.getPhosphate_max() > max){
          max = stat.getPhosphate_max();
        }
      }
    }
    return max;
  }
  public Double getMinPhosphateFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getPhosphate_min();
      }else{
        if (stat.getPhosphate_min() < min){
          min = stat.getPhosphate_min();
        }
      }
    }
    return min;
  }
  public Double getMaxNitrateFromSummary(){
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getNitrate_max();
      }else{
        if (stat.getNitrate_max() > max){
          max = stat.getNitrate_max();
        }
      }
    }
    return max;
  }
  public Double getMinNitrateFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getNitrate_min();
      }else{
        if (stat.getNitrate_min() < min){
          min = stat.getNitrate_min();
        }
      }
    }
    return min;
  }
  public Double getMaxSilicateFromSummary(){
    Double max = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (max == null){
        max = stat.getSilicate_max();
      }else{
        if (stat.getSilicate_max() > max){
          max = stat.getSilicate_max();
        }
      }
    }
    return max;
  }
  public Double getMinSilicateFromSummary(){
    Double min = null;
    Collection stats = summary_table.values();
    Iterator<EMUStat> iter = stats.iterator();

    while (iter.hasNext()){
      EMUStat stat = iter.next();
      if (min == null){
        min = stat.getSilicate_min();
      }else{
        if (stat.getSilicate_min() < min){
          min = stat.getSilicate_min();
        }
      }
    }
    return min;
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
    if (temp != null){
      observation.setTemperature(Double.parseDouble(temp));
    }

    String salinity = extractValueFromMap("salinity", map);
    if (salinity != null){
      observation.setSalinity(Double.parseDouble(salinity));
    }

    String dissolvedOx = extractValueFromMap("dissO2", map);
    if (dissolvedOx != null){
      observation.setOxygen(Double.parseDouble(dissolvedOx));
    }

    String phosphate = extractValueFromMap("phosphate", map);
    if (phosphate != null){
      observation.setPhosphate(Double.parseDouble(phosphate));
    }

    String silicate = extractValueFromMap("silicate", map);
    if (silicate !=  null){
      observation.setSilicate(Double.parseDouble(silicate));
    }

    String nitrate = extractValueFromMap("nitrate", map);
    if (nitrate != null){
      observation.setNitrate(Double.parseDouble(nitrate));
    }

    Log.i("Observation", "observation: " + observation.toString());
    return observation;
  }
  private EMUStat createEMUStat(Map<String,Object> map){
    EMUStat stat = new EMUStat();

    stat.setEmu_name(Integer.parseInt(extractValueFromMap("Cluster37", map)));

    String min_temp = extractValueFromMap("MIN_temp", map);
    if (min_temp != null){
      stat.setTemp_min(Double.parseDouble(min_temp));
    }
    String max_temp = extractValueFromMap("MAX_temp", map);
    if (max_temp != null){
      stat.setTemp_max(Double.parseDouble(max_temp));
    }
    String mean_temp = extractValueFromMap("MEAN_temp", map);
    if (mean_temp != null){
      stat.setTemp_mean(Double.parseDouble(mean_temp));
    }

    String min_salinity = extractValueFromMap("MIN_salinity", map);
    if (min_salinity != null){
      stat.setSalinity_min(Double.parseDouble(min_salinity));
    }
    String max_salinity = extractValueFromMap("MAX_salinity", map);
    if (max_salinity != null){
      stat.setSalinity_max(Double.parseDouble(max_salinity));
    }
    String mean_salinity = extractValueFromMap("MEAN_salinity", map);
    if (mean_salinity != null){
      stat.setSalinity_mean(Double.parseDouble(mean_salinity));
    }

    String min_disso2 = extractValueFromMap("MIN_dissO2", map);
    if (min_disso2 != null){
      stat.setDisso2_min(Double.parseDouble(min_disso2));
    }
    String max_disso2 = extractValueFromMap("MAX_dissO2", map);
    if (max_disso2 != null){
      stat.setDisso2_max(Double.parseDouble(max_disso2));
    }
    String mean_disso2 = extractValueFromMap("MEAN_dissO2", map);
    if (mean_disso2 != null){
      stat.setDisso2_mean(Double.parseDouble(mean_disso2));
    }

    String min_phosphate = extractValueFromMap("MIN_phosphate", map);
    if (min_phosphate != null){
      stat.setPhosphate_min(Double.parseDouble(min_phosphate));
    }
    String max_phosphate = extractValueFromMap("MAX_phosphate", map);
    if (max_disso2 != null){
      stat.setPhosphate_max(Double.parseDouble(max_phosphate));
    }
    String mean_phosphate = extractValueFromMap("MEAN_phosphate", map);
    if (mean_phosphate != null){
      stat.setPhosphate_mean(Double.parseDouble(mean_phosphate));
    }

    String min_silicate = extractValueFromMap("MIN_silicate", map);
    if (min_silicate != null){
      stat.setSilicate_min(Double.parseDouble(min_silicate));
    }
    String max_silicate = extractValueFromMap("MAX_silicate", map);
    if (max_silicate != null){
      stat.setSilicate_max(Double.parseDouble(max_silicate));
    }
    String mean_silicate = extractValueFromMap("MEAN_silicate", map);
    if (mean_silicate != null){
      stat.setSilicate_mean(Double.parseDouble(mean_silicate));
    }

    String min_nitrate = extractValueFromMap("MIN_nitrate", map);
    if (min_nitrate != null){
      stat.setNitrate_min(Double.parseDouble(min_nitrate));
    }
    String max_nitrate = extractValueFromMap("MAX_nitrate", map);
    if (max_nitrate != null){
      stat.setNitrate_max(Double.parseDouble(max_nitrate));
    }
    String mean_nitrate = extractValueFromMap("MEAN_nitrate", map);
    if (mean_nitrate != null){
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
