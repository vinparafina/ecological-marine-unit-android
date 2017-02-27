/* Copyright 2017 Esri
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

package com.esri.android.ecologicalmarineunitexplorer.map;


import android.support.annotation.NonNull;
import android.util.Log;

import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.ServiceApi;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the concrete implementation of the Presenter defined in the MapContract.
 * It encapsulates business logic and drives the behavior of the View.
 */

public class MapPresenter implements MapContract.Presenter {

  private final MapContract.View mMapView;
  private DataManager mDataManager;

  private final String NO_EMU_FOUND = "Please select an ocean location";
  private final String NO_LOCATION_FOUND = "No location found for ";

  private ArcGISTiledLayer mSurfaceLayer = null;


  public MapPresenter(@NonNull final MapContract.View mapView, @NonNull final DataManager dataManager){
    mMapView = checkNotNull(mapView, "map view cannot be null");
    mDataManager = checkNotNull(dataManager);
    mDataManager = dataManager;
    mMapView.setPresenter(this);

  }

  /**
   * Start by setting the map up and
   * adding the tiled layer for the EMU polygons
   */
  @Override public void start() {
    // Show a dialog while the map loads
    String DIALOG_TITLE = "EMU Map Loading";
    String DIALOG_MESSAGE = "Rounding up the EMUs...";
    mMapView.showProgressBar(DIALOG_MESSAGE, DIALOG_TITLE);
    int ZOOM_LEVEL = 1;
    final ArcGISMap map =  new ArcGISMap(Basemap.Type.OCEANS, 0, 0, ZOOM_LEVEL);
    mMapView.setUpMap(map);

    // EMU Ocean Surface
    String TILED_LAYER_URL = "http://esri.maps.arcgis.com/home/item.html?id=d2db1dbd6d2742a38fe69506029b83ac";
    mSurfaceLayer = new ArcGISTiledLayer(TILED_LAYER_URL);
    mMapView.addLayer(mSurfaceLayer);

    cacheInitialDepthLayer();
  }

  /**
   * When a user clicks a location in the map, show the progress bar and
   * create a buffered polygon around the point and query for EMU data.
   * @param point - A geolocation representing the
   *              place a user clicked on the map
   */
  @Override public void setSelectedPoint(final Point point) {

    mMapView.showProgressBar("Fetching details about the location...", "Preparing Location Summary");
    mMapView.showClickedLocation(point);
    double BUFFER_SIZE = 32000;
    final Polygon polygon = getBufferPolygonForPoint(point, BUFFER_SIZE);
    final PolygonBuilder builder = new PolygonBuilder(polygon);
    final Envelope envelope = builder.getExtent();

    mDataManager.queryForEmuAtLocation(envelope, new ServiceApi.SummaryCallback() {
      @Override public void onWaterColumnsLoaded(final WaterColumn column) {

        mMapView.hideProgressBar();
        if (column == null){
          mMapView.showMessage(NO_EMU_FOUND);
          mMapView.onNoEmusFound();
        }else{
          mMapView.setSelectedPoint(point);
          mMapView.setViewpoint();
          mMapView.showClickedLocation(point);
          mMapView.showSummary(column);
        }
      }
    });
  }

  /**
   * Create a polygon representing a buffered region around a
   * a given point
   * @param point - A geolocation representing the
   *              place a user clicked on the map
   * @param distance - size of the buffer to build around the point
   * @return - a polygon representing the buffered region with the point as its center
   */
  @Override public Polygon getBufferPolygonForPoint(final Point point, final double distance) {
    return GeometryEngine.buffer(point, distance);
  }

  /**
   * Once map has been loaded, hide the progress bar
   */
  @Override public void mapLoaded() {

    mMapView.hideProgressBar();
    mMapView.showSnackbar();
  }

  /**
   * Geocode the given string and search for EMUs.
   * @param addresss - The string entered into the search view
   */
  @Override public void geocodeAddress(final String addresss) {
    mDataManager.queryForAddress(addresss, mMapView.getSpatialReference(),  new ServiceApi.GeocodingCallback() {
      @Override public void onGeocodeResult(final List<GeocodeResult> results) {
        if (results == null){
          mMapView.showMessage(NO_LOCATION_FOUND + addresss);
        }else{
          final GeocodeResult result = results.get(0);
          setSelectedPoint(result.getDisplayLocation());
        }
      }
    });
  }

  /**
   * Determine what expression to apply in the definition expression
   * for retrieving polygons at a certain depth
   * @param value - integer value from seekbar
   */
  @Override public void retrieveEMUPolygonByDepth(final Integer value) {
    Integer depth = 1;
    if(value <=10) {
      depth = 1;
    }else if (value > 10 && value < 20){
      depth = 10;
    } else if (value >= 20 && value < 30){
      depth = 20;
    } else if (value >= 30 && value < 40){
      depth = 30;
    } else if (value >= 40 && value < 50) {
      depth = 40;
    } else if (value >= 50 && value < 60) {
      depth = 50;
    } else if (value >= 60 && value < 70) {
      depth = 60;
    } else if (value >= 70 && value < 80) {
      depth = 70;
    } else if (value >= 80 && value < 90) {
      depth = 80;
    } else if (value >= 90 && value <=100) {
      depth = 90;
    }
    if (depth == 1){
      // Toggle the TiledLayer in the map to show
      mSurfaceLayer.setVisible(true);

    }else{
      mSurfaceLayer.setVisible(false);
    }
    mDataManager.manageEmuPolygonsByDepth(depth, new ServiceApi.EMUByDepthCallback() {
      @Override public void onPolygonsRetrieved(final FeatureLayer layer) {
        // Show progress bar
        mMapView.showProgressBar("Retrieving EMU polygons", "Data Download");
        if (layer != null){
          mMapView.hideProgressBar();
        }else{
          Log.i("MapPresenter", "*****Return layer is null!");
          mMapView.hideProgressBar();
          mMapView.showMessage("Unable to display EMU polygons");
        }
      }
    });
  }

  /**
   * Cache the first depth level below the ocean surface.
   * This is run only once.
   */
  private void cacheInitialDepthLayer(){
    mDataManager.queryEmuByDepth(10, new ServiceApi.EMUByDepthCallback() {
      @Override public void onPolygonsRetrieved(final FeatureLayer layer) {
        if (layer != null){
          Log.i("MapPresenter", "Initial depth layer downloaded");
          layer.setDefinitionExpression(" Depth = 0");
          mMapView.addLayer(layer);
        }
        mMapView.hideProgressBar();
      }
    });
  }
}
