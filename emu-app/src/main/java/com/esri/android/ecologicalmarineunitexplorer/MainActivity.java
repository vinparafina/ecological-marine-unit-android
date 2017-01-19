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

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartFragment;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartPresenter;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.map.MapFragment;
import com.esri.android.ecologicalmarineunitexplorer.map.MapPresenter;
import com.esri.android.ecologicalmarineunitexplorer.bottomsheet.BottomSheetFragment;
import com.esri.android.ecologicalmarineunitexplorer.bottomsheet.BottomSheetPresenter;
import com.esri.android.ecologicalmarineunitexplorer.util.ActivityUtils;
import com.esri.android.ecologicalmarineunitexplorer.watercolumn.WaterColumnFragment;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfileFragment;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfilePresenter;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;

public class MainActivity extends AppCompatActivity
    implements BottomSheetFragment.OnDetailClickedListener{


  private BottomSheetPresenter mBottomSheetPresenter = null;
  private SummaryChartPresenter mSummaryChartPresenter = null;
  private DataManager mDataManager = null;
  private MapPresenter mMapPresenter = null;
  private BottomSheetBehavior mBottomSheetBehavior = null;
  private WaterColumn mWaterColumn = null;
  private String TAG = MainActivity.class.getSimpleName();
  private boolean mInMapState = false;
  private FloatingActionButton mFab = null;

  public MainActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "ENTERING onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // Set license key
    ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE_KEY);

    // Initally hide the FAB
    mFab = (FloatingActionButton) findViewById(R.id.fab);
    mFab.setVisibility(View.INVISIBLE);

    // Check for internet connectivity
    if (!checkForInternetConnectivity()){
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setMessage(getString(R.string.internet_connectivity));
      progressDialog.setTitle(getString(R.string.wireless_problem));
      progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialog, int which) {
          progressDialog.dismiss();
          finish();
        }
      });
      progressDialog.show();

    }else{
      // Get data access setup
      mDataManager = DataManager.getDataManagerInstance(getApplicationContext());

      // Set up fragments
      setUpMagFragment();

      setUpBottomSheetFragment();
    }
    Log.i(TAG, "LEAVING onCreate");
  }

  /**
   * Attach display logic to bottom sheet behavior.
   */
  private void setUpBottomSheetFragment(){
    final FragmentManager fm = getSupportFragmentManager();
    BottomSheetFragment bottomSheetFragment = (BottomSheetFragment) fm.findFragmentById(R.id.bottom_sheet_view) ;

    if (bottomSheetFragment == null) {
      bottomSheetFragment = BottomSheetFragment.newInstance();
      ActivityUtils.addFragmentToActivity(fm, bottomSheetFragment, R.id.bottom_sheet_view, "summary fragment");
      mBottomSheetPresenter = new BottomSheetPresenter(bottomSheetFragment);
    }

    mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_view));
    mBottomSheetBehavior.setHideable(true);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED){

          showBottomSheetContent();
          mFab.setVisibility(View.VISIBLE);

        }else if (newState == BottomSheetBehavior.STATE_HIDDEN){
          mFab.setVisibility(View.INVISIBLE);
        }
      }

      @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        float scaleFactor = 1 - slideOffset;
        if (scaleFactor <= 1){
          mFab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
        }

      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "ENTERING onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.menu, menu);
    // Retrieve the SearchView and plug it into SearchManager
    final SearchView searchView= (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
    searchView.setQueryHint(getString(R.string.query_hint));
    SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        mMapPresenter.geocodeAddress(query);
        searchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    Log.i(TAG, " LEAVING onCreateOptionsMenu");
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    Log.i(TAG, "ENTERING onPrepareOptionsMenu");
    final MenuItem profile = menu.findItem(R.id.action_profile);
    final MenuItem search = menu.findItem(R.id.action_search);
    int state = mBottomSheetBehavior.getState();
    Log.i(TAG, "Bottom sheet state"   + " = " + state);
    if ((mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) || (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)) {
      profile.setVisible(true);
      search.setVisible(false);
    }else if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || mInMapState) {
      profile.setVisible(false);
      search.setVisible(true);
    }else{
      profile.setVisible(false);
      search.setVisible(false);
    }
    Log.i(TAG, "LEAVING onPrepareOptionsMenu");

    return super.onPrepareOptionsMenu(menu);
  }


  private void showWaterColumnProfile(Point point) {
    // Remove water column, summary, text and button
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    hideMapView();

    setUpWaterProfileToolbar();

    FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
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

    mInMapState = true;

    Log.i("MainActivity", "setUpMapFragment");
  }

  /**
   * Override the application label used for the toolbar title
   */
  private void setUpMapToolbar() {

    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();
    actionBar.setTitle(R.string.explore_ocean);
    toolbar.setNavigationIcon(null);
    mInMapState = true;

    Log.i("MainActivity", "setUpMapToolbar");
  }
  /**
   * Set up toolbar for chart detail
   * @param EMUid - integer representing the name of the EMU
   */
  private void setUpChartSummaryToolbar(int EMUid){
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();
    // Hide both menu items

    actionBar.setTitle(getString(R.string.detail_emu) + EMUid);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        // Remove the chart fragment
        removeChartSummaryDetail();

        // Shrink parent container
        shrinkChartContainer();


        // Restore the bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Restore the bottom sheet toolbar
        setUpBottomSheetToolbar();

      }
    });
    Log.i("MainActivity", "setUpChartSummary");
  }

  private void removeChartSummaryDetail(){
    final FragmentManager fm = getSupportFragmentManager();
    SummaryChartFragment summaryChartFragment = (SummaryChartFragment) fm.findFragmentById(R.id.chartContainer);
    if (summaryChartFragment != null){
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(summaryChartFragment);
      transaction.commit();
    }
    Log.i("MainActivity", "removeChartSummaryDetail");
  }
  /**
   * Set the text for the summary toolbar and listen
   * for navigation requests
   */
  private void setUpBottomSheetToolbar() {
    final Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ((AppCompatActivity) this).getSupportActionBar().setTitle(R.string.ocean_summary_location_title);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        // Hide the bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Set the map toolbar title and remove navigation
        setUpMapToolbar();
      }
    });

    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
          showWaterColumnProfile(mWaterColumn.getLocation());
        return false;
      }
    });
    Log.i("MainActivity", "setUpBottomSheetToolbar");
  }

  private void setUpWaterProfileToolbar(){
    final Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
    ((AppCompatActivity) this).setSupportActionBar(toolbar);
    ((AppCompatActivity) this).getSupportActionBar().setTitle("Water Column Profile");
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        //Remove profile
        removeWaterColumnProfie();

        // Shrink the parent container
        shrinkChartContainer();

        // Return to large map
        expandMap();
      }
    });
    Log.i("MainActivity", "setUpWaterProfileToolbar");
  }

  private void removeWaterColumnProfie(){
    final FragmentManager fm = getSupportFragmentManager();
    WaterProfileFragment fragment = (WaterProfileFragment) fm.findFragmentById(R.id.chartContainer);
    if (fragment != null){
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
    Log.i("MainActivity", "removeWaterColumnProfile");
  }
  private void removeSummaryAndWaterColumnViews(){
    final FragmentManager fm = getSupportFragmentManager();
    BottomSheetFragment summaryFragment = (BottomSheetFragment) fm.findFragmentById(R.id.summary_container) ;
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
    Log.i("MainActivity", "removeSummaryAndWaterColumnViews");
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
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    Log.i("MainActivity", "expandMap");
  }


  private void hideMapView(){
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(0,0);
    layoutParams.setMargins(0, 0,0,0);
    mapLayout.setLayoutParams(layoutParams);
    mapLayout.requestLayout();

    Log.i("MainActivity", "hideMapView");
  }

  /**
   * Show the bottom sheet
   */
  public void showBottomSheet(){
    // Change the state of bottom sheet
    if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED){
      mBottomSheetBehavior.setState( BottomSheetBehavior.STATE_COLLAPSED);
    }else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
      showBottomSheetContent();
    }

    mInMapState = false;
    Log.i("MainActivity", "showBottomSheet");
  }

  public void showBottomSheetContent(){
    // Show summary info about location and water column
    mWaterColumn = mDataManager.getCurrentWaterColumn();

    mBottomSheetPresenter.setWaterColumn(mWaterColumn);

    // Set up the summary toolbar
    setUpBottomSheetToolbar();
  }


  public void showSummaryDetail(int emuName){
    // Remove summary and water column
   // removeSummaryAndWaterColumnViews();

    // Hide the bottom sheet containing the summary view
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
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

    Log.i("MainActivity", "showSummaryDetail");
  }
  private void shrinkChartContainer(){
    FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    layout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
    layout.requestLayout();
    Log.i("MainActivity", "shrinkChartContainer");
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
  public void showSnackbar(){
    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout) ;
    Snackbar snackbar = Snackbar
        .make(coordinatorLayout, "Please tap a on ocean location", Snackbar.LENGTH_LONG);

    snackbar.show();
  }

  @Override
  public void onResume(){
    Log.i(TAG, "ENTERING onResume");
    super.onResume();

    Log.i(TAG, "LEAVING onResume");
  }
}
