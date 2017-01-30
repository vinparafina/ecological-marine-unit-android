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


import com.esri.android.ecologicalmarineunitexplorer.BasePresenter;
import com.esri.android.ecologicalmarineunitexplorer.BaseView;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.ArcGISMap;

/**
 * This is the contract between the Presenter and View components of the MVP pattern.
 * It defines methods to display the map and EMU features.
 */

public interface MapContract {
  interface View extends BaseView<Presenter> {

    /**
     * Return the x,y coordinate for given screen location
     * @param mapPoint
     * @return Point
     */
    Point getScreenToLocation(android.graphics.Point mapPoint);

    /**
     * Show a message in the view
     * @param message - String
     */
    void showMessage(String message);

    /**
     * Add a feature layer to the map
     * @param layer - Layer
     */
    void addLayer(Layer layer);

    /**
     * Set up the map
     * @param map - ArcGISMap
     */
    void setUpMap(ArcGISMap map );

    /**
     * Show the water column summary
     * @param column - WaterColumn
     */
    void showSummary(WaterColumn column);

    /**
     * Add a marker to the map for the given Point
     * @param point - Point
     */
    void showClickedLocation(Point point);

    /**
     * Show a progress bar with given message and title
     * @param message - String
     * @param title - String
     */
    void showProgressBar(String message, String title);

    /**
     * Hide progress bar
     */
    void hideProgressBar();

    /**
     * Set viewpoint based on selected location
     */
    void setViewpoint();

    /**
     *  Set clicked point
     * @param p - Point
     */
    void setSelectedPoint(Point p);

    /**
     * Return spatial reference for map
     * @return - SpatialReference
     */
    SpatialReference getSpatialReference();

    /**
     * Show snackbar
     */
    void showSnackbar();

    /**
     * Callback to activity
     */
    void onNoEmusFound();
  }
  interface Presenter extends BasePresenter {

    /**
     * When a user clicks a location in the map, show the progress bar and
     * create a buffered polygon around the point and query for EMU data.
     * @param point - A geolocation representing the
     *              place a user clicked on the map
     */
    void setSelectedPoint(Point point);

    /**
     * Create a polygon representing a buffered region around a
     * a given point
     * @param point - A geolocation representing the
     *              place a user clicked on the map
     * @param distance - size of the buffer to build around the point
     * @return - a polygon representing the buffered region with the point as its center
     */
    Polygon getBufferPolygonForPoint(Point point, double distance);

    /**
     * Once map has been loaded, hide the progress bar
     */
    void mapLoaded();

    /**
     * Geocode the given string and search for EMUs.
     * @param addresss - The string entered into the search view
     */
    void geocodeAddress(String addresss);

    /**
     * Determine what expression to apply in the definition expression
     * for retrieving polygons at a certain depth
     * @param depth - Integer
     */
    void retrieveEMUPolygonByDepth(Integer depth);
  }
}
