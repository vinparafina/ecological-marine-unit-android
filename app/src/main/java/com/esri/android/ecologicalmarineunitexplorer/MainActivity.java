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


package com.esri.android.ecologicalmarineunitexplorer;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartFragment;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartPresenter;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.ServiceApi;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.map.MapFragment;
import com.esri.android.ecologicalmarineunitexplorer.map.MapPresenter;
import com.esri.android.ecologicalmarineunitexplorer.summary.SummaryFragment;
import com.esri.android.ecologicalmarineunitexplorer.summary.SummaryPresenter;
import com.esri.android.ecologicalmarineunitexplorer.util.ActivityUtils;
import com.esri.android.ecologicalmarineunitexplorer.watercolumn.WaterColumnFragment;
import com.esri.android.ecologicalmarineunitexplorer.watercolumn.WaterColumnPresenter;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfileFragment;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfilePresenter;
import com.esri.arcgisruntime.geometry.Point;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements WaterColumnFragment.OnWaterColumnSegmentClickedListener,
    SummaryFragment.OnViewIndexChange, SummaryFragment.OnDetailClickedListener {


  private SummaryPresenter mSummaryPresenter;
  private DataManager mDataManager;
  private WaterColumnPresenter mWaterColumnPresenter;
  private SummaryChartPresenter mSummaryChartPresenter;
  private MapPresenter mMapPresenter;
  private BottomSheetBehavior mBottomSheetBehavior;
  private LinearLayout mBottomSheet;
  public MainActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // Check for internet connectivity
    if (!checkForInternetConnectivity()){
      Toast.makeText(this, "Internet connectivity is required for this application", Toast.LENGTH_LONG).show();
    }else{
      // Get data access setup
      mDataManager = DataManager.getDataManagerInstance(getApplicationContext());

      // Set up fragments
      setUpMagFragment();

      // Attach listener to the profile
      // button in the bottom sheet
      Button button = (Button) findViewById(R.id.btnProfile);
      button.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          // Hide the bototm sheet
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          // Grab the location from the water column
          // and show the WaterColumnProfile fragment
          Point point = mDataManager.getCurrentWaterColumn().getLocation();
          showWaterColumnProfile(point);
        }
      });

      // Attach a listener to the view layer
      // button in the bottom sheet
      Button layerBtn = (Button) findViewById(R.id.btnLayers) ;
      layerBtn.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          // Hide the bototm sheet
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          showLayers(true);
        }
      });
      //Set up behavior for the bottom sheet
      setUpBottomSheet();
    }

  }

  /**
   * Attach display logic to bottom sheet behavior.
   */
  private void setUpBottomSheet(){
    mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_view));

    mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(final View bottomSheet, final int newState) {

      }

      @Override
      public void onSlide(final View bottomSheet, final float slideOffset) {
      }
    });

    mBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet_view);
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    // Retrieve the SearchView and plug it into SearchManager
    final SearchView searchView= (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
    searchView.setQueryHint("Address or latitude/longitude");
    SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        Log.i("MainActivity", "Query = "+ query);
        mMapPresenter.geocodeAddress(query);
        searchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    return true;
  }
  private void hideSearchView(Toolbar toolbar){
    int childCount = toolbar.getChildCount();
    for (int x=0 ; x < childCount; x++){
      View v = toolbar.getChildAt(x);
      if (v instanceof ActionMenuView){
        v.setVisibility(View.INVISIBLE);
      }
    }
  }
  private void showSearchView(Toolbar toolbar){
    int childCount = toolbar.getChildCount();
    for (int x=0 ; x < childCount; x++){
      View v = toolbar.getChildAt(x);
      if (v instanceof ActionMenuView){
        v.setVisibility(View.VISIBLE);
      }
    }
  }

  private void showWaterColumnProfile(Point point) {
    // Remove water column, summary, text and button
    removeSummaryAndWaterColumnViews();
    hideMapView();

    setUpWaterProfileToolbar();

    FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    layout.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    layout.requestLayout();

    final FragmentManager fm = getSupportFragmentManager();
    WaterProfileFragment waterProfileFragment = null;
    Fragment f  =  fm.findFragmentById(R.id.chartContainer);
    if (f instanceof WaterProfileFragment){
      waterProfileFragment = (WaterProfileFragment)f;
    }else{
      waterProfileFragment = WaterProfileFragment.newInstance();
    }

    WaterProfilePresenter presenter = new WaterProfilePresenter(point, waterProfileFragment, mDataManager);

    FragmentTransaction transaction = fm.beginTransaction();
    transaction.replace(R.id.chartContainer, waterProfileFragment);
    transaction.commit();
  }

  /**
   * Configure the map fragment
   */
  private void setUpMagFragment(){

    // Set up the toolbar for the map fragment
    setUpMapToolbar();

    final FragmentManager fm = getSupportFragmentManager();

    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_container);

    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      mMapPresenter = new MapPresenter(mapFragment, mDataManager);
      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), mapFragment, R.id.map_container, "map fragment");
    }
  }

  /**
   * Override the application label used for the toolbar title
   */
  private void setUpMapToolbar() {

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();
    actionBar.setTitle("Explore An Ocean Location");
    toolbar.setNavigationIcon(null);
    showSearchView(toolbar);
  }
  /**
   * Set up toolbar for chart detail
   * @param EMUid - integer representing the name of the EMU
   */
  private void setUpChartSummaryToolbar(int EMUid){
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();
    hideSearchView(toolbar);
    actionBar.setTitle("Detail for EMU " + EMUid);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        // Remove the chart fragment
        removeChartSummaryDetail();

        // Show summary
        showLayers(false);

        // Add back the map
        showMapImage();
      }
    });
  }

  private void removeChartSummaryDetail(){
    final FragmentManager fm = getSupportFragmentManager();
    SummaryChartFragment summaryChartFragment = (SummaryChartFragment) fm.findFragmentById(R.id.chartContainer);
    if (summaryChartFragment != null){
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(summaryChartFragment);
      transaction.commit();
    }
  }
  /**
   * Set the text for the summary toolbar and listen
   * for navigation requests
   */
  private void setUpSummaryToolbar() {
    final Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ((AppCompatActivity) this).getSupportActionBar().setTitle(R.string.ocean_summary_location_title);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
    hideSearchView(toolbar);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        //Remove summary and water column
        removeSummaryAndWaterColumnViews();

        // Hide the map snapshot
        hideMapImage();

        //Reconstitute the large map
        expandMap();
      }
    });
  }

  private void setUpWaterProfileToolbar(){
    final Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ((AppCompatActivity) this).getSupportActionBar().setTitle("Water Column Profile");
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
    hideSearchView(toolbar);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        //Remove profile
        removeWaterColumnProfie();

        // Return to large map
        expandMap();
      }
    });
  }

  private void removeWaterColumnProfie(){
    final FragmentManager fm = getSupportFragmentManager();
    WaterProfileFragment fragment = (WaterProfileFragment) fm.findFragmentById(R.id.chartContainer);
    if (fragment != null){
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
  }
  private void removeSummaryAndWaterColumnViews(){
    final FragmentManager fm = getSupportFragmentManager();
    SummaryFragment summaryFragment = (SummaryFragment) fm.findFragmentById(R.id.summary_container) ;
    if (summaryFragment != null ) {
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(summaryFragment);
      transaction.commit();
    }
    WaterColumnFragment waterColumnFragment = (WaterColumnFragment) fm.findFragmentById(R.id.column_container);
    if (waterColumnFragment != null){
      FragmentTransaction waterTransaction = getSupportFragmentManager().beginTransaction();
      waterTransaction.remove(waterColumnFragment);
      waterTransaction.commit();
    }
  }

  private void expandMap(){

    FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    mapLayout.setLayoutParams(layoutParams);
    mapLayout.requestLayout();

    // Set the toolbar title and remove navigation
    setUpMapToolbar();

    // Show the bottom sheet
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
  }

  /**
   * Set up the map image in the upper half of the screen
   */
  private void showMapImage(){
    // Adjust the map image layout
    ImageView imageView = (ImageView) findViewById(R.id.imgMap) ;
    LinearLayout.LayoutParams  imageLayoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,5);
    imageLayoutParams.setMargins(0, 36,36,16);
    imageView.setLayoutParams(imageLayoutParams);
    imageView.requestLayout();
  }

  private void hideMapImage(){
    ImageView imageView = (ImageView) findViewById(R.id.imgMap) ;
    LinearLayout.LayoutParams  imageLayoutParams  =  new LinearLayout.LayoutParams(0,0);
    imageLayoutParams.setMargins(0, 0,0,0);
    imageView.setLayoutParams(imageLayoutParams);
    imageView.requestLayout();
  }

  private void hideMapView(){
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(0,0);
    layoutParams.setMargins(0, 0,0,0);
    mapLayout.setLayoutParams(layoutParams);
    mapLayout.requestLayout();
  }

  private void swapOutMapViewWithSnapshot(){
    // Adjust the map's layout
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,5);
    layoutParams.setMargins(0, 36,36,16);
    mapLayout.setLayoutParams(layoutParams);

    // Once we've shrunk the map view and layout has completed,
    // get a map snapshot and swap out the map with the map snapshot.
    mapLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
          int oldRight, int oldBottom) {
        // Stop listening to layout changes
        mapLayout.removeOnLayoutChangeListener(this);
        // Get the snapshot and replace the map vie with the snapshot
        final FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_container);
        mapFragment.getMapBitmap(new ServiceApi.BitmapCallback() {
          @Override public void onBitmapGenerated(Bitmap bitmap) {
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            ImageView imageView = (ImageView) findViewById(R.id.imgMap) ;
            imageView.setBackground(d);

            // Hide the map view
            hideMapView();

            // Now show the image with the map snapshot
            showMapImage();
          }
        });
      }
    });
    mapLayout.requestLayout();
  }
  /**
   * Add the WaterColumnFragment and SummaryFragment to
   * the view while shrinking the map view.
   */
  public void showSummaryBottomSheet(){

    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    // Show summary info about location and water column
    WaterColumn waterColumn = mDataManager.getCurrentWaterColumn();
    // Get the current location for the column
    final Point p = waterColumn.getLocation();
    TextView textView = (TextView) findViewById(R.id.txtSummary) ;
    String x = new DecimalFormat("#.##").format(p.getX());
    String y = new DecimalFormat("#.##").format(p.getY());
    textView.setText("The water column at " + y + ", "+ x +" (lat/lng) contains " + waterColumn.getEmuSet().size() + " EMU layers, extending to a depth of "+ waterColumn.getDepth()+" meters.");

  }

  /**
   * Show the water column and recycler views
   * @param takeSnapshot - a boolean flag indicating if snapshot is needed
   */
  public void showLayers( boolean takeSnapshot){

    WaterColumn waterColumn = mDataManager.getCurrentWaterColumn();
    final FragmentManager fm = getSupportFragmentManager();
    SummaryFragment summaryFragment = (SummaryFragment) fm.findFragmentById(R.id.summary_container) ;

    if (summaryFragment == null){
      summaryFragment = SummaryFragment.newInstance();
      mSummaryPresenter = new SummaryPresenter(summaryFragment);
    }
    // Set up the summary toolbar
    setUpSummaryToolbar();

    mSummaryPresenter.setWaterColumn(waterColumn);

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    if (takeSnapshot){
      // Swap out map with snapshot
      swapOutMapViewWithSnapshot();
    }else{
      showMapImage();
    }


    // Adjust the summary containing the recycler view
    FrameLayout summaryLayout = (FrameLayout) findViewById(R.id.summary_container);
    summaryLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,9));
    summaryLayout.requestLayout();


    // Replace whatever is in the summary_container view with this fragment,
    // and add the transaction to the back stack so the user can navigate back
    transaction.replace(R.id.summary_container, summaryFragment);
    transaction.addToBackStack("summary fragment");

    // Commit the transaction
    transaction.commit();

    WaterColumnFragment waterColumnFragment = (WaterColumnFragment) fm.findFragmentById(R.id.column_container);
    if (waterColumnFragment == null){
      waterColumnFragment = WaterColumnFragment.newInstance();
      mWaterColumnPresenter = new WaterColumnPresenter(waterColumnFragment);
    }


    FragmentTransaction wcTransaction = getSupportFragmentManager().beginTransaction();
    wcTransaction.replace(R.id.column_container, waterColumnFragment);
    wcTransaction.commit();
    mWaterColumnPresenter.setWaterColumn(waterColumn);
  }

  public void showSummaryDetail(int emuName){
    // Remove summary and water column
    removeSummaryAndWaterColumnViews();

    // Remove map
    hideMapImage();

    FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    layout.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    layout.requestLayout();

    // Add the chart view to the column container
    final FragmentManager fm = getSupportFragmentManager();
    SummaryChartFragment chartFragment = (SummaryChartFragment) fm.findFragmentById(R.id.chartContainer);
    if (chartFragment == null){
      chartFragment = SummaryChartFragment.newInstance();
      mSummaryChartPresenter = new SummaryChartPresenter(emuName, chartFragment, mDataManager);
    }

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.chartContainer, chartFragment);
    transaction.commit();

    setUpChartSummaryToolbar(emuName);
  }




  /**
   * When a water column segment is tapped, show the
   * associated item in the SummaryFragment
   * @param position - index of the recycler view to display
   */
  @Override public void onSegmentClicked(int position) {
    final FragmentManager fm = getSupportFragmentManager();
    SummaryFragment summaryFragment = (SummaryFragment) fm.findFragmentById(R.id.summary_container) ;
    if (summaryFragment != null){
      summaryFragment.scrollToSummary(position);
    }
  }

  @Override public void onChange(int position) {
    final FragmentManager fm = getSupportFragmentManager();
    WaterColumnFragment waterColumnFragment = (WaterColumnFragment) fm.findFragmentById(R.id.column_container);
    if (waterColumnFragment != null ){
      waterColumnFragment.highlightSegment(position);
    }
  }

  @Override public void onButtonClick(int emuName) {
      showSummaryDetail(emuName);
  }

  /**
   * Get the state of the network info
   * @return - boolean, false if network state is unavailable
   * and true if device is connected to a network.
   */
  private boolean checkForInternetConnectivity(){
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifi = connManager.getActiveNetworkInfo();
    if (wifi == null){
      return false;
    }else {
      return wifi.isConnected();
    }
  }
}
