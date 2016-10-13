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

public class DataManager {
  private ServiceFeatureTable mMeshClusterTable;

  private ServiceFeatureTable mMeshPointTable;

  private ServiceFeatureTable mClusterPolygonTable;

  private Context mContext;

  private static DataManager instance = null;

  private Map<Point, WaterColumn> cache = new HashMap<>();


  private DataManager(Context applicationContext){

    mContext = applicationContext;

    mClusterPolygonTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_polygon));

    mMeshClusterTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_mesh_cluster));

    mMeshPointTable = new ServiceFeatureTable(mContext.getString(R.string.service_emu_point_mesh)
    );
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
   * Process the listenable future by creating a WaterColumn for
   * any returned data.
   * @param envelope - an Envelope representing the search area
   * @param futureResult - a ListenableFuture<FeatureQueryResult> to process
   * @param callback  - a SummaryCallback called when query processing is complete
   * @return - a WaterColumn representing returned features.
   */
  private void processQueryForEMuAtLocation(final Envelope envelope, final ListenableFuture<FeatureQueryResult> futureResult, final ServiceApi.SummaryCallback callback){

    futureResult.addDoneListener(new Runnable() {
      @Override public void run() {
        try{
          Log.i("ProcessQuery", "Query done...");
          FeatureQueryResult fqr = futureResult.get();

          Map<Geometry,WaterColumn> pointWaterColumnMap = new HashMap<Geometry, WaterColumn>();

          if (fqr != null){
            Log.i("ProcessQuery", "Processing features...");

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
          WaterColumn closestWaterColumn = findClosestWaterColumn(envelope, pointWaterColumnMap);


          // Processing is complete, notify the callback
          callback.onWaterColumnsLoaded(closestWaterColumn);


        }catch (Exception e){
            e.printStackTrace();
        }
      }
    });
  }

  public void addToWaterColumnCache(WaterColumn waterColumn){
    cache.put(waterColumn.getLocation(), waterColumn);
  }
  public WaterColumn getFromWaterColumnCache(Point location){
    WaterColumn waterColumn = null;
    if (cache.containsKey(location)){
      waterColumn = cache.get(location);
    }
    return waterColumn;
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

   // Log.i("Observation", "observation: " + observation.toString());
    return observation;
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
