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

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.esri.android.ecologicalmarineunitexplorer.MainActivity;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;


public class MapFragment extends Fragment implements MapContract.View {

  private GraphicsOverlay mGraphicOverlay = null;
  private MapView mMapView = null;
  private View mRoot = null;
  private MapContract.Presenter mPresenter = null;
  private ProgressDialog mProgressDialog = null;
  private Point mSelectedPoint = null;
  private ArcGISMap mMap = null;

  private NoEmuFound mNoEmuFoundCallback = null;

  public interface NoEmuFound{
    void handleNoEmu();
  }
  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  /**
   * Delegate view logic to the presenter once the view has been created
   * @param layoutInflater - LayoutInflater
   * @param container - ViewGroup
   * @param savedInstance - Bundle
   * @return View
   */
  @Override
  @Nullable
  public final View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    super.onCreateView(layoutInflater, container, savedInstance);
   // mRoot = layoutInflater.inflate(R.layout.map_view, container,false);
    mRoot = container;
    // Listen for seekbar changes
    final SeekBar seekBar = (SeekBar) getActivity().findViewById(R.id.seekBar) ;
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        mPresenter.retrieveEMUPolygonByDepth(seekBar.getProgress());
      }

      @Override public void onStartTrackingTouch(final SeekBar seekBar) {
        // No-op
      }

      @Override public void onStopTrackingTouch(final SeekBar seekBar) {
        // No-op
      }
    });

    mPresenter.start();
    return null;
  }

  /**
   * Check to make sure activity has implemented required interfaces
   * for handling no EMU found
   * @param activity - Context
   */
  @Override
  public void onAttach(final Context activity) {
    super.onAttach(activity);
    try{
      mNoEmuFoundCallback = (NoEmuFound) activity;
    }catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement NoEmuFound.");
    }

  }

  /**
   * Set up the ArcGISMap
   * @param map - ArcGISMap
   */
  @Override
  public void setUpMap(final ArcGISMap map){
    mMapView = (MapView) mRoot.findViewById(R.id.map);
    mMapView.setAttributionTextVisible(false);
    mMap  = map;
    mMapView.setMap(mMap);

    // Start listening to touch interactions on the map
    final View.OnTouchListener mapTouchListener = new MapTouchListener(getActivity().getApplicationContext(), mMapView);
    mMapView.setOnTouchListener(mapTouchListener);

    // Once map has loaded enable touch listener
    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(final DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
          // Stop listening to any more draw status changes
          mMapView.removeDrawStatusChangedListener(this);
          // Notify presenter
          mPresenter.mapLoaded();
        }
      }
    });

    // When map's layout is changed, re-center map on selected point
    mMapView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override public void onLayoutChange(final View v, final int left, final int top, final int right, final int bottom, final int oldLeft, final int oldTop,
          final int oldRight, final int oldBottom) {
        setViewpoint();
      }
    });

  }

  /**
   * Set viewpoint based on selected location
   */
  @Override public void setViewpoint(){
    if (mSelectedPoint != null){
      final double MAP_SCALE = 25000000;
      mMapView.setViewpointCenterAsync(mSelectedPoint, MAP_SCALE);
      mSelectedPoint = null;
    }
  }

  /**
   * Set the selected location
   * @param p - Point
   */
  @Override public void setSelectedPoint(final Point p) {
    mSelectedPoint = p;
  }

  /**
   * Get the spatial reference of the map
   * @return SpatialReference
   */
  @Override public SpatialReference getSpatialReference() {
    SpatialReference sr = null;
    if (mMap != null && mMap.getLoadStatus() == LoadStatus.LOADED){
      sr = mMap.getSpatialReference();
    }
    return sr;
  }


  /**
   * Add an operational layer to the map
   * @param layer - A Layer to add
   */
  @Override public void addLayer(final Layer layer) {
    // Create and add layers that need to be visible in the map
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);
    mMap.getOperationalLayers().add(layer);
  }

  /**
   * Resume map view
   */
  @Override
  public final void onResume(){
    super.onResume();
    mMapView.resume();

  }

  /**
   * Pause map view
   */
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
  public Point getScreenToLocation(final android.graphics.Point mapPoint){
    return mMapView.screenToLocation(mapPoint);
  }


  /**
   * Show a toast displaying message
   * @param message - String representing the message to display
   */
  @Override public void showMessage(final String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
  }

  /**
   * Assign the presenter
   * @param presenter MapContract.Presenter
   */
  @Override public void setPresenter(final MapContract.Presenter presenter) {
      mPresenter = presenter;
  }

  /**
   * Delegate showing of water column to activity
   * @param column - WaterColumn
   */
  @Override public void showSummary(final WaterColumn column) {
    ((MainActivity) getActivity()).showBottomSheet();
  }

  /**
   * Create and add a marker to the map representing
   * the clicked location.
   * @param point - A com.esri.arcgisruntime.geometry.Point item
   */
  @Override public void showClickedLocation(final Point point) {
    final Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.blue_pin);
    final BitmapDrawable drawable = new BitmapDrawable(getResources(), icon);
    final PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(drawable);
    markerSymbol.setHeight(40);
    markerSymbol.setWidth(40);
    markerSymbol.setOffsetY(markerSymbol.getHeight()/2);
    markerSymbol.loadAsync();
    markerSymbol.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        final Graphic marker = new Graphic(point, markerSymbol);
        mGraphicOverlay.getGraphics().clear();
        mGraphicOverlay.getGraphics().add(marker);
      }
    });

  }

  /**
   * Display progress bar with given message and title
   * @param message - String representing message to display
   * @param title - String progress window title
   */
  @Override public void showProgressBar(final String message, final String title) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.dismiss();
    mProgressDialog.setTitle(title);
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }


  /**
   * Show a snackbar prompting user to action
   */
  @Override  public void showSnackbar(){
    ((MainActivity)getActivity()).showSnackbar();
  }

  /**
   * Callback to activity
   */
  @Override public void onNoEmusFound() {
    mNoEmuFoundCallback.handleNoEmu();
  }

  /**
   * Hide progress bar
   */
  @Override public void hideProgressBar() {
    mProgressDialog.dismiss();
  }


  public class MapTouchListener extends DefaultMapViewOnTouchListener{
    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified
     * context and MapView.
     *
     * @param context the application context from which to get the display
     *                metrics
     * @param mapView the MapView on which to control touch events
     */
    public MapTouchListener(final Context context, final MapView mapView) {
      super(context, mapView);
    }
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent motionEvent) {
      super.onSingleTapConfirmed(motionEvent);
      final android.graphics.Point mapPoint = new android.graphics.Point((int) motionEvent.getX(),
          (int) motionEvent.getY());
      mPresenter.setSelectedPoint(getScreenToLocation(mapPoint));
      return true;
    }
  }

}
