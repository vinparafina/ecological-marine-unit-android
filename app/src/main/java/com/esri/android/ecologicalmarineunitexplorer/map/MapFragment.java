package com.esri.android.ecologicalmarineunitexplorer.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.LineSymbol;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

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

  private MapContract.Presenter mPresenter;

  private Point mSelectedPoint;
  private ArcGISMap mMap;
  private Viewpoint mInitialViewpoint;

  private final double GALAPAGOS_LAT = -0.3838312;
  private final double GALAPAGOS_LONG = -91.5727346;

  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  @Override
  public final void onCreate(@NonNull final Bundle savedInstance) {

    super.onCreate(savedInstance);

    if (savedInstance != null)
    {
      // Populate data from data manager given the point
      double x = savedInstance.getDouble("X");
      double y = savedInstance.getDouble("Y");
      ;
      if (savedInstance.containsKey("SR")){
        String sr = savedInstance.getString("SR");
        SpatialReference spatialReference = SpatialReference.create(sr);
        mSelectedPoint = new Point(x,y, spatialReference);
      }else{
        mSelectedPoint = new Point(x,y);
      }
      Log.i("MapFragment", "Reconstituting instance state in 'onCreate'");
      showClickedLocation(mSelectedPoint);
    }
    // retain this fragment
    setRetainInstance(true);
    mPresenter.start();
  }

  @Override
  @Nullable
  public final View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){

    final View root = layoutInflater.inflate(R.layout.map_view, container,false);

    setUpMap(root);

    if (mSelectedPoint != null){
      showClickedLocation(mSelectedPoint);
      Log.i("MapFragment", "Reconstituting state in 'onCreateView'");
    }
    return root;
  }


  public void onSaveInstanceState (Bundle outState){
    if (mSelectedPoint != null){
      outState.putDouble("X", mSelectedPoint.getX());
      outState.putDouble("Y" , mSelectedPoint.getY());
      if (mSelectedPoint.getSpatialReference() != null){
        outState.putString("SR", mSelectedPoint.getSpatialReference().getWKText());
      }
      Log.i("MapFragment", "Saving instance state");
    }
  }

  private final void setUpMap(View root){

    // Show a dialog while the map loads
    final ProgressDialog progressDialog = ProgressDialog
        .show(this.getActivity(), null, "Loading map data...", true);

    mMapView = (MapView) root.findViewById(R.id.map);

    // Create a map using Ocean basemap
    // centered on the Galapagos Islands
    mMap  = new ArcGISMap(Basemap.Type.OCEANS, GALAPAGOS_LAT, GALAPAGOS_LONG, 4  );

    mMapView.setMap(mMap);


    // Create and add layers that need to be visible in the map
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);

    // EMU Ocean Surface
    ArcGISTiledLayer tiledLayerBaseMap = new ArcGISTiledLayer(getString(R.string.emu_ocean_surface_layer));
    mMap.getOperationalLayers().add(tiledLayerBaseMap);

    final MapTouchListener mapTouchListener = new MapTouchListener(getActivity().getApplicationContext(), mMapView);

    // Once map has loaded, remove the progress dialog
    // and enable touch listener
    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
          mInitialViewpoint = mMap.getInitialViewpoint();
          // Stop listening to any more draw status changes
          mMapView.removeDrawStatusChangedListener(this);
          // Start listening to touch interactions on the map
          mMapView.setOnTouchListener(mapTouchListener);
          progressDialog.dismiss();
        }
      }
    });

    // When map's layout is changed, re-center map on selected point
    mMapView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
          int oldRight, int oldBottom) {
        if (mSelectedPoint != null){
          mMapView.setViewpointCenterAsync(mSelectedPoint, 5000000);
        }
      }
    });

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

  @Override public void setPresenter(MapContract.Presenter presenter) {
      mPresenter = presenter;
  }

  /**
   * Add a polygon representing an area around the clicked point
   * @param polygon - polygon representing a buffered area around the clicked point
   */
  public void showSelectedRegion(Polygon polygon){
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.BLUE,lineSymbol);
    mGraphicOverlay.getGraphics().add( new Graphic(polygon, fillSymbol));

  }

  /**
   * Remind the user that EMUs are only found in the ocean
   */
  @Override public void showDataNotFound() {
    Toast.makeText(getActivity(), R.string.no_emu_found, Toast.LENGTH_SHORT).show();
  }

  @Override public void showSummary(WaterColumn column) {
    ((MapActivity) getActivity()).showSummary(column);
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
      Log.i("ScreenLocation", "screen position = x= " + mapPoint.x + " y= "+ mapPoint.y);
      mSelectedPoint = getScreenToLocation(mapPoint);
      mPresenter.setSelectedPoint(mSelectedPoint);
      return true;
    }
  }
}
