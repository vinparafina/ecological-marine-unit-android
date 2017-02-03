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
  private SummaryChartFragment mSummaryChartFragment = null;
  private SummaryChartPresenter mSummaryChartPresenter = null;
  private BottomSheetFragment mBottomSheetFragment = null;

  public MainActivity() {}

  /**
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    /*********************************************************************
     * If you have a basic license key, uncomment line 100.
     * See directions in the README about how to Configure a Basic License
     *********************************************************************/

   // ArcGISRuntimeEnvironment.setLicense(BuildConfig.LICENSE_KEY);

    // Initially hide the FAB
    mFab = (FloatingActionButton) findViewById(R.id.fab);
    if (mFab != null){
      mFab.setVisibility(View.INVISIBLE);
      mFab.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
          }else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          }

        }
      });
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
    mBottomSheetFragment = (BottomSheetFragment) fm.findFragmentById(R.id.bottom_sheet_view) ;

    if (mBottomSheetFragment == null) {
      mBottomSheetFragment = BottomSheetFragment.newInstance();
      ActivityUtils.addFragmentToActivity(fm, mBottomSheetFragment, R.id.bottom_sheet_view, getString(R.string.fragment_summary));
      mBottomSheetPresenter = new BottomSheetPresenter(mBottomSheetFragment);
    }

    mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_view));
    if (mBottomSheetBehavior != null){
      mBottomSheetBehavior.setHideable(true);
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

      mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
        @Override public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
          if (newState == BottomSheetBehavior.STATE_COLLAPSED){
            showBottomSheetContent();
            mFab.setVisibility(View.VISIBLE);
            LinearLayout layout = (LinearLayout) findViewById(R.id.horizontalLinearLayout);
            LinearLayout.LayoutParams layoutParams =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0,0,0,0);
            layout.setLayoutParams(layoutParams);
            layout.requestLayout();

          }
          if (newState ==BottomSheetBehavior.STATE_EXPANDED){
            LinearLayout layout = (LinearLayout) findViewById(R.id.horizontalLinearLayout);
            LinearLayout.LayoutParams layoutParams =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0,155,0,0);
            layout.setLayoutParams(layoutParams);
            layout.requestLayout();
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
   * @param menu Menu
   * @return boolean
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
   * @param menu Menu
   * @return boolean
   */
  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    final MenuItem profile = menu.findItem(R.id.action_profile);
    final MenuItem search = menu.findItem(R.id.action_search);
    final int state = mBottomSheetBehavior.getState();
    if ((state == BottomSheetBehavior.STATE_COLLAPSED) || (state == BottomSheetBehavior.STATE_EXPANDED)) {
      profile.setVisible(true);
      search.setVisible(false);
    }else if(mInMapState) {
      profile.setVisible(false);
      search.setVisible(true);
    }else{
      profile.setVisible(false);
      search.setVisible(false);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  /**
   * Show the view with the water column profiles
   * @param point - Point representing clicked geo location
   */
  private void showWaterColumnProfile(final Point point) {
    // Remove water column, summary, text and button
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
   // hideMapView();

    setUpWaterProfileToolbar();

    final FrameLayout layout = (FrameLayout) findViewById(R.id.chartContainer);
    if (layout != null){
      layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
      layout.requestLayout();
    }

    WaterProfileFragment waterProfileFragment = WaterProfileFragment.newInstance();
    new WaterProfilePresenter(point, waterProfileFragment, mDataManager);

    // Add the chart view to the column container
    final FragmentManager fm = getSupportFragmentManager();
    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    final Fragment f  =  fm.findFragmentById(R.id.chartContainer);
    if (f == null){
      transaction.addToBackStack(getString(R.string.fragment_detail_chart));
    }
    transaction.replace(R.id.chartContainer, waterProfileFragment);
    transaction.commit();

    // Hide the FAB
    mFab.setVisibility(View.INVISIBLE);

    mInMapState = false;
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
          getSupportFragmentManager(), mapFragment, R.id.map_container, getString(R.string.fragment_map));
    }
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
            returnToSummary();
        }
      });
    }
  }
  /**
   * Logic for returning from chart detail to bottom sheet
   */
  private void returnToSummary(){
    // Remove the chart fragment
    removeChartContainer();

    // Shrink parent container
    shrinkChartContainer();

    // Show the FAB
    mFab.setVisibility(View.VISIBLE);

    // Restore the bottom sheet
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


    // Restore the bottom sheet toolbar
    setUpBottomSheetToolbar();
  }


  /**
   * Remove the water column profile fragment
   */
  private void removeChartContainer(){
    final FragmentManager fm = getSupportFragmentManager();
    final Fragment fragment = fm.findFragmentById(R.id.chartContainer);
    if (fragment != null){
      final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      transaction.remove(fragment);
      transaction.commit();
      fm.popBackStack();
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
          returnToMap();
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

  private void returnToMap(){
    // Hide the bottom sheet
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    // Set the map toolbar title and remove navigation
    setUpMapToolbar();
  }

  /**
   * Customize toolbar for water profile view
   */
  private void setUpWaterProfileToolbar(){
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (toolbar != null){
      setSupportActionBar(toolbar);
      if (getSupportActionBar() != null){
        getSupportActionBar().setTitle(R.string.water_column_profile);
      }
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);

      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {
          returnToSummary();
        }
      });
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

    if (mSummaryChartFragment == null){
      mSummaryChartFragment = SummaryChartFragment.newInstance();
      mSummaryChartPresenter = new SummaryChartPresenter(emuName, mSummaryChartFragment, mDataManager);
    }else{
      mSummaryChartPresenter.setEmuName(emuName);
    }

    // Add the chart view to the column container
    final FragmentManager fm = getSupportFragmentManager();
    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    final Fragment f  =  fm.findFragmentById(R.id.chartContainer);
    if (f == null){
      transaction.addToBackStack(getString(R.string.fragment_detail_chart));
    }
    transaction.replace(R.id.chartContainer, mSummaryChartFragment);
    transaction.commit();

    // Hide the FAB
    mFab.setVisibility(View.INVISIBLE);
    mInMapState = false;

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

  /**
   * Customize the behavior to hide/show
   * fragments as user navigates with back button.
   */
  @Override
  public void onBackPressed() {
    int count = getSupportFragmentManager().getBackStackEntryCount();
    if (count == 0) {
      super.onBackPressed();
    } else {
      FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(count-1);
      String fragmentName = entry.getName();

      if (fragmentName.equalsIgnoreCase(getString(R.string.fragment_detail_chart))){
        returnToSummary();
      }else if (fragmentName.equalsIgnoreCase(getString(R.string.fragment_summary))){
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
           setUpMapToolbar();
        }else{
          finish();
        }
      }
   }
  }
}
