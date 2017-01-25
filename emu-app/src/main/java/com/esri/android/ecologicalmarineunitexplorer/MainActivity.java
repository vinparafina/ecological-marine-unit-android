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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartFragment;
import com.esri.android.ecologicalmarineunitexplorer.chartsummary.SummaryChartPresenter;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.map.MapFragment;
import com.esri.android.ecologicalmarineunitexplorer.map.MapPresenter;
import com.esri.android.ecologicalmarineunitexplorer.bottomsheet.BottomSheetFragment;
import com.esri.android.ecologicalmarineunitexplorer.bottomsheet.BottomSheetPresenter;
import com.esri.android.ecologicalmarineunitexplorer.util.ActivityUtils;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfileFragment;
import com.esri.android.ecologicalmarineunitexplorer.waterprofile.WaterProfilePresenter;
import com.esri.arcgisruntime.geometry.Point;

/**
 * The single activity in the application that orchestrates fragments,
 * adjusts toolbar behavior, and checks for internet connectivity.
 */
public class MainActivity extends AppCompatActivity
    implements BottomSheetFragment.OnDetailClickedListener, MapFragment.NoEmuFound{


  private BottomSheetPresenter mBottomSheetPresenter = null;

  private DataManager mDataManager = null;
  private MapPresenter mMapPresenter = null;
  private BottomSheetBehavior mBottomSheetBehavior = null;
  private WaterColumn mWaterColumn = null;
  private boolean mInMapState = false;
  private FloatingActionButton mFab = null;

  public MainActivity() {}

  /**
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    // Set license key
    //ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE_KEY);

    // Initially hide the FAB
    mFab = (FloatingActionButton) findViewById(R.id.fab);
    if (mFab != null){
      mFab.setVisibility(View.INVISIBLE);
    }


    // Check for internet connectivity
    if (!checkForInternetConnectivity()){
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setMessage(getString(R.string.internet_connectivity));
      progressDialog.setTitle(getString(R.string.wireless_problem));
      progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
        @Override public void onClick(final DialogInterface dialog, final int which) {
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
    if (mBottomSheetBehavior != null){
      mBottomSheetBehavior.setHideable(true);
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

      mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
        @Override public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
          if (newState == BottomSheetBehavior.STATE_COLLAPSED){

            showBottomSheetContent();
            if (mFab != null){
              mFab.setVisibility(View.VISIBLE);
            }


          }else if (newState == BottomSheetBehavior.STATE_HIDDEN){
            if (mFab != null){
              mFab.setVisibility(View.INVISIBLE);
            }

          }
        }

        @Override public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
          final float scaleFactor = 1 - slideOffset;
          if (mFab != null){
            if (scaleFactor <= 1){
              mFab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
          }
        }
      });
    }
  }

  /**
   *
   * @param menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu, menu);
    // Retrieve the SearchView and plug it into SearchManager
    final SearchView searchView= (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
    searchView.setQueryHint(getString(R.string.query_hint));
    final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(final String query) {
        mMapPresenter.geocodeAddress(query);
        searchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(final String newText) {
        return false;
      }
    });
    return true;
  }

  /**
   *
   * @param menu
   * @return
   */
  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    final MenuItem profile = menu.findItem(R.id.action_profile);
    final MenuItem search = menu.findItem(R.id.action_search);
    final int state = mBottomSheetBehavior.getState();
    if ((state == BottomSheetBehavior.STATE_COLLAPSED) || (state == BottomSheetBehavior.STATE_EXPANDED)) {
      profile.setVisible(true);
      search.setVisible(false);
    }else if(state == BottomSheetBehavior.STATE_HIDDEN || mInMapState) {
      profile.setVisible(false);
      search.setVisible(true);
    }else{
      profile.setVisible(false);
      search.setVisible(false);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  /**
   *
   * @param point
   */
  private void showWaterColumnProfile(final Point point) {
    // Remove water column, summary, text and button
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    hideMapView();

    setUpWaterProfileToolbar();

    final FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    if (layout != null){
      layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
      layout.requestLayout();
    }

    final FragmentManager fm = getSupportFragmentManager();
    WaterProfileFragment waterProfileFragment;
    final Fragment f  =  fm.findFragmentById(R.id.chartContainer);
    waterProfileFragment = f instanceof WaterProfileFragment ?
        (WaterProfileFragment) f :
        WaterProfileFragment.newInstance();

    final WaterProfilePresenter presenter = new WaterProfilePresenter(point, waterProfileFragment, mDataManager);

    final FragmentTransaction transaction = fm.beginTransaction();
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
  }

  /**
   * Override the application label used for the toolbar title
   */
  private void setUpMapToolbar() {
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      setSupportActionBar(toolbar);
      final ActionBar actionBar = (this).getSupportActionBar();
      if (actionBar != null){
        actionBar.setTitle(R.string.explore_ocean);
      }
      toolbar.setNavigationIcon(null);
    }
    mInMapState = true;
  }
  /**
   * Set up toolbar for chart detail
   * @param EMUid - integer representing the name of the EMU
   */
  private void setUpChartSummaryToolbar(final int EMUid){
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      setSupportActionBar(toolbar);
      final ActionBar actionBar = (this).getSupportActionBar();
      if (actionBar != null){
        actionBar.setTitle(getString(R.string.detail_emu) + EMUid);
      }

      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {

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
    }

  }

  /**
   * Remove the fragment for the summary charts
   */
  private void removeChartSummaryDetail(){
    final FragmentManager fm = getSupportFragmentManager();
    final SummaryChartFragment summaryChartFragment = (SummaryChartFragment) fm.findFragmentById(R.id.chartContainer);
    if (summaryChartFragment != null){
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(summaryChartFragment);
      transaction.commit();
    }
  }
  /**
   * Set the text for the summary toolbar and listen
   * for navigation requests
   */
  private void setUpBottomSheetToolbar() {
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      setSupportActionBar(toolbar);
      if (getSupportActionBar() != null){
        getSupportActionBar().setTitle(R.string.ocean_summary_location_title);
      }
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {

          // Hide the bottom sheet
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

          // Set the map toolbar title and remove navigation
          setUpMapToolbar();
        }
      });

      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override public boolean onMenuItemClick(final MenuItem item) {
          showWaterColumnProfile(mWaterColumn.getLocation());
          return false;
        }
      });
    }
  }

  /**
   * Customize toolbar for water profile view
   */
  private void setUpWaterProfileToolbar(){
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      setSupportActionBar(toolbar);
      if (getSupportActionBar() != null){
        getSupportActionBar().setTitle("Water Column Profile");
      }
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);

      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {

          //Remove profile
          removeWaterColumnProfile();

          // Shrink the parent container
          shrinkChartContainer();

          // Return to large map
          expandMap();
        }
      });
    }
  }

  /**
   * Remove the water column profile fragment
   */
  private void removeWaterColumnProfile(){
    final FragmentManager fm = getSupportFragmentManager();
    final WaterProfileFragment fragment = (WaterProfileFragment) fm.findFragmentById(R.id.chartContainer);
    if (fragment != null){
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
    }
  }

  /**
   * Expand the layout for the map
   */
  private void expandMap(){
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    if (mapLayout != null){
      final LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      mapLayout.setLayoutParams(layoutParams);
      mapLayout.requestLayout();
    }

    // Show part of the bottom sheet
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
  }

  /**
   * Shrink the layout for the map
   */
  private void hideMapView(){
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_container);
    if (mapLayout != null){
      final LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(0,0);
      layoutParams.setMargins(0, 0,0,0);
      mapLayout.setLayoutParams(layoutParams);
      mapLayout.requestLayout();
    }

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
  }

  /**
   * Populate bottom sheet with water column details
   */
  private void showBottomSheetContent(){
    // Show summary info about location and water column
    mWaterColumn = mDataManager.getCurrentWaterColumn();

    mBottomSheetPresenter.setWaterColumn(mWaterColumn);

    // Set up the summary toolbar
    setUpBottomSheetToolbar();
  }

  /**
   * Show the candlestick charts for a specific EMU layer
   * @param emuName - int representing an EMU layer name
   */
  private void showSummaryDetail(final int emuName){
    // Hide the bottom sheet containing the summary view
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    // Expand the layout for the charts
    final FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    if (layout != null){
      layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
      layout.requestLayout();
    }

    // Add the chart view to the column container
    final FragmentManager fm = getSupportFragmentManager();
    SummaryChartFragment chartFragment = (SummaryChartFragment) fm.findFragmentById(R.id.chartContainer);
    if (chartFragment == null){
      chartFragment = SummaryChartFragment.newInstance();
      SummaryChartPresenter mSummaryChartPresenter = new SummaryChartPresenter(emuName, chartFragment, mDataManager);
    }

    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.chartContainer, chartFragment);
    transaction.commit();

    setUpChartSummaryToolbar(emuName);
  }

  /**
   * Shrink layout for candlestick charts
   */
  private void shrinkChartContainer(){
    final FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    if (layout != null){
      layout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
      layout.requestLayout();
    }
  }

  /**
   * When a DETAILS button is clicked for a particular EMU, show
   * the candlestick charts.
   * @param emuName - int representing an EMU name (id).
   */
  @Override public void onButtonClick(final int emuName) {
      showSummaryDetail(emuName);
  }

  /**
   * Get the state of the network info
   * @return - boolean, false if network state is unavailable
   * and true if device is connected to a network.
   */
  private boolean checkForInternetConnectivity(){
    final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo wifi = connManager.getActiveNetworkInfo();
    return  wifi != null && wifi.isConnected();
  }

  /**
   * Show a message at the bottom of the screen prompting user to action
   */
  public void showSnackbar(){
    final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout) ;
    if (coordinatorLayout != null){
      final Snackbar snackbar = Snackbar
          .make(coordinatorLayout, R.string.tap_location, Snackbar.LENGTH_LONG);

      snackbar.show();
    }
  }

  /**
   * Adjust the view when user clicks on an
   * area with no EMU data.
   */
  @Override public void handleNoEmu() {
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    mInMapState = true;
    setUpMapToolbar();
    // Trigger the onPrepareOptionsMenu
    invalidateOptionsMenu();
  }
}
