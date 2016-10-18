package com.esri.android.ecologicalmarineunitexplorer.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;

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

public class MapFragment extends Fragment implements MapContract.View {

  private GraphicsOverlay mGraphicOverlay;
  private MapView mMapView;
  private View mRoot;
  private MapContract.Presenter mPresenter;
  private ProgressDialog mProgressDialog;
  private Point mSelectedPoint;
  private ArcGISMap mMap;
  private Viewpoint mInitialViewpoint;
  private final double MAP_SCALE = 5000000;



  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  /**
   * Delegate view logic to the presenter once the view has been created
   */
  @Override
  @Nullable
  public final View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){

    mRoot = layoutInflater.inflate(R.layout.map_view, container,false);


    if (mSelectedPoint != null){
      showClickedLocation(mSelectedPoint);
      Log.i("MapFragment", "Reconstituting state in 'onCreateView'");
    }

    mPresenter.start();
    return mRoot;
  }


  @Override
  public void setUpMap(ArcGISMap map){
    mMapView = (MapView) mRoot.findViewById(R.id.map);
    mMap  = map;
    mMapView.setMap(mMap);

    final MapTouchListener mapTouchListener = new MapTouchListener(getActivity().getApplicationContext(), mMapView);

    // Once map has loaded enable touch listener
    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
          mInitialViewpoint = mMap.getInitialViewpoint();
          // Stop listening to any more draw status changes
          mMapView.removeDrawStatusChangedListener(this);
          // Start listening to touch interactions on the map
          mMapView.setOnTouchListener(mapTouchListener);
          // Notify presenter
          mPresenter.mapLoaded();
        }
      }
    });

    // When map's layout is changed, re-center map on selected point
    mMapView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
          int oldRight, int oldBottom) {
        if (mSelectedPoint != null){
          mMapView.setViewpointCenterAsync(mSelectedPoint, MAP_SCALE);
        }
      }
    });

  }

  /**
   * Add an operational layer to the map
   * @param layer - A Layer to add
   */
  @Override public void addLayer(Layer layer) {
    // Create and add layers that need to be visible in the map
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);
    mMap.getOperationalLayers().add(layer);
  }

  @Override
  public final void onResume(){
    super.onResume();
    mMapView.resume();

  }

  @Override
  public final void onPause() {
    super.onPause();
    mMapView.pause();
  }
  /**
   * Obtain the geo location for a given point
   * on the screen
   * @param mapPoint - An Android screen location
   * @return  - A geometry point representing the screen location
   */
  public Point getScreenToLocation(android.graphics.Point mapPoint){
    return mMapView.screenToLocation(mapPoint);
  }

  /**
   * Zoom to initial map view and clear out the graphical layers
   */
  @Override public void resetMap() {
    mSelectedPoint = null;
    mGraphicOverlay.getGraphics().clear();
    if (mInitialViewpoint != null){
      mMapView.setViewpoint(mInitialViewpoint);
    }
  }

  @Override public void showMessage(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
  }


  @Override public void setPresenter(MapContract.Presenter presenter) {
      mPresenter = presenter;
  }

  /**
   * Delegate showing of water column to activity
   * @param column
   */
  @Override public void showSummary(WaterColumn column) {
    ((com.esri.android.ecologicalmarineunitexplorer.MainActivity) getActivity()).showSummary();
  }

  /**
   * Create and add a marker to the map representing
   * the clicked location.
   * @param point - A com.esri.arcgisruntime.geometry.Point item
   */
  @Override public void showClickedLocation(Point point) {
    Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.blue_pin);
    BitmapDrawable drawable = new BitmapDrawable(getResources(), icon);
    PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(drawable);
    Graphic marker = new Graphic(point, markerSymbol);
    mGraphicOverlay.getGraphics().clear();
    mGraphicOverlay.getGraphics().add(marker);
  }

  /**
   * Display progress bar with given message and title
   * @param message - String representing message to display
   * @param title - String progress window title
   */
  @Override public void showProgressBar(String message, String title) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.setTitle(title);
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }

  /**
   * Hide progress bar
   */
  @Override public void hideProgressBar() {
    mProgressDialog.hide();
  }

  public class MapTouchListener extends DefaultMapViewOnTouchListener {
    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified
     * context and MapView.
     *
     * @param context the application context from which to get the display
     *                metrics
     * @param mapView the MapView on which to control touch events
     */
    public MapTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
      android.graphics.Point mapPoint = new android.graphics.Point((int) motionEvent.getX(),
          (int) motionEvent.getY());
      //Log.i("ScreenLocation", "screen position = x= " + mapPoint.x + " y= "+ mapPoint.y);
      mSelectedPoint = getScreenToLocation(mapPoint);
      mPresenter.setSelectedPoint(mSelectedPoint);
      return true;
    }
  }
}
