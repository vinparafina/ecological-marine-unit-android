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

/**
 * Test performance may vary based on network latency.  Remember to add specific values to your app_settings.xml for
 * some of these tests and ensure your have internet access and location tracking on.  The first time these
 * tests are run you will be prompted by the app for WRITE_EXTERNAL permission.  Test screenshots are stored in the
 * Robotium folder of your device's SD card.
 *
 */

package com.esri.android.ecologicalmarineunitexplorer;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.github.mikephil.charting.charts.CombinedChart;
import com.robotium.solo.Solo;

import java.util.ArrayList;

public class EMUAppTest extends ActivityInstrumentationTestCase2 {

  private Solo solo;
  private static final int PERMISSION_WRITE_STORAGE = 4;
  private static boolean mPermissionsGranted = false;
  private static final String TAG = EMUAppTest.class.getSimpleName();


  public EMUAppTest() { super(MainActivity.class);}

  @Override
  public void setUp() throws Exception {
    //setUp() is run before a test case is started.
    //This is where the solo object is created.
    Solo.Config config = new Solo.Config();
    config.shouldScroll = false;

    solo = new Solo(getInstrumentation(), config);
    getActivity();
  }

  @Override
  public void tearDown() throws Exception {
    //tearDown() is run after a test case has finished.
    //finishOpenedActivities() will finish all
    // the activities that have been opened during the test execution.
    solo.finishOpenedActivities();
  }

  /**
   * Test that a progress dialog closes
   * and screen appears with toolbar title
   * and a map view.
   */
  public void testLoadMap(){
    // Progress dialog should show and
    // disappear after map loads
    assertTrue(solo.waitForDialogToClose());

    // Search for page title
    String toolbarTitle = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString();
    String title = getActivity().getString(R.string.toolbar_title);
    assertTrue(toolbarTitle.equalsIgnoreCase(title));

    // Map view present?
    MapView mapView = (MapView) solo.getView(R.id.map) ;
    assertNotNull(mapView);
  }

  /**
   * Verify that clicking on the map places
   * a marker if the location is over an ocean
   */
  public void testClickOnOcean(){
    clickOnOceanPoint();
  }

  /**
   * Verify that clicking on a land
   * location shows a toast message
   */
  public void testClickOnLand(){

    // Somewhere in the Sahara
    Point sahara = new Point(21.9741618,13.0648185, SpatialReferences.getWgs84());
    android.graphics.Point derivedScreenLocation = deriveScreenPointForLocation(sahara);
    assertTrue(solo.waitForDialogToClose());
    solo.clickOnScreen(derivedScreenLocation.x, derivedScreenLocation.y);
    boolean messageShows = solo.waitForText(getActivity().getString(R.string.no_emu_found));
    assertTrue(messageShows);
  }

  /**
   * Verifty that a recycler view
   * containing EMU observations is loaded
   * when a point in the ocean is clicked.
   */
  public void testSummaryShown(){
    clickOnOceanPoint();
    assertTrue(solo.searchButton("DETAILS"));
    ArrayList<TextView> items = solo.clickInRecyclerView(0);
    int count = recyclerCount();
    assertTrue(solo.scrollDownRecyclerView(count -1));
    assertTrue(solo.scrollUpRecyclerView(0));
  }

  /**
   * Verify that buttons in
   * the water column are displayed
   */
  public void testWaterColumnShown(){
    clickOnOceanPoint();
    assertTrue(solo.waitForDialogToClose());
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    boolean buttonFound = solo.searchButton("DETAILS");
    assertTrue(buttonFound);

    // There are as many buttons as there are
    // items in the recycler view

    int buttonCount = recyclerCount();
    for (int x= 0; x < buttonCount; x++){
      assertNotNull(getActivity().findViewById(x));
    }

  }

  /**
   * Validate that when a segment
   * in the water column is tapped,
   * the associated item is shown in
   * the recycler view.
   */
  public void testClickButtonSelectsSegment(){
    clickOnOceanPoint();
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    int count = recyclerCount();
    //Click on the second emu layer in the column
    Log.i("Test", "Adapter count = "+ count);
    Button button = (Button) solo.getView(count - 1);
    solo.clickOnButton(button.getId());
    // Since this scrolls the recycler view to the last item
    // there should be no down arrow indicating another item
    assertTrue(solo.getView(R.id.arrowDown).getVisibility() == View.INVISIBLE);
  }


  /**
   * Test that charts are drawn when
   * Detail button is clicked
   */
  public void testForDetailCharts(){
    clickOnOceanPoint();
    assertTrue(solo.searchButton("DETAILS"));
    solo.clickOnButton("DETAILS");
    assertTrue(solo.waitForDialogToClose());
    CombinedChart combinedChart = (CombinedChart) solo.getView(R.id.chart1);
    assertTrue(combinedChart.getData().getAllData().size() > 0);

    combinedChart = (CombinedChart) solo.getView(R.id.chart2);
    assertTrue(combinedChart.getData().getAllData().size() > 0);

    combinedChart = (CombinedChart) solo.getView(R.id.chart3);
    assertTrue(combinedChart.getData().getAllData().size() > 0);

    combinedChart = (CombinedChart) solo.getView(R.id.chart4);
    assertTrue(combinedChart.getData().getAllData().size() > 0);

    combinedChart = (CombinedChart) solo.getView(R.id.chart5);
    assertTrue(combinedChart.getData().getAllData().size() > 0);

    combinedChart = (CombinedChart) solo.getView(R.id.chart6);
    assertTrue(combinedChart.getData().getAllData().size() > 0);
  }

  /**
   * Test the water column profiles are drawn
   * when clicking on "VIEW COLUMN PROFILE"
   */
  public void testForWaterProfileChartsOnExpandedBottomSheet(){
    clickOnOceanPoint();

    View v = solo.getView(R.id.action_profile);
    ActionMenuItemView actionMenu = (ActionMenuItemView) v;
    boolean listeners = actionMenu.hasOnClickListeners();

    solo.clickOnView(solo.getView(R.id.action_profile));

    solo.sleep(3000);

    assertTrue(solo.waitForText("Temperature"));
    CombinedChart chart = (CombinedChart) solo.getView(R.id.propertyChart) ;
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);


    assertTrue(solo.waitForText("Salinity"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);


    assertTrue(solo.waitForText("Oxygen"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Phosphate"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Silicate"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Nitrate"));
    checkForChartData();
  }
  /**
   * Test the water column profiles are drawn
   * when clicking on "VIEW COLUMN PROFILE"
   */
  public void testForWaterProfileChartsOnCollapsedBottomSheet(){
    assertTrue(solo.waitForDialogToClose());
    // Near the Galapagos Islands
    Point start = new Point(-95.0974397, -0.05932, SpatialReferences.getWgs84());
    android.graphics.Point screenPoint = deriveScreenPointForLocation(start);

    solo.clickOnScreen(screenPoint.x, screenPoint.y );
    assertTrue(solo.waitForText("Location Summary"));

    android.graphics.Point p = new android.graphics.Point();
    getActivity().getWindowManager().getDefaultDisplay().getSize(p);
    int fromX, toX, fromY, toY = 0;
    fromX = p.x/2;
    toX = p.x/2;
    fromY = (p.y/2) + (p.y/3);
    toY = (p.y/2) - (p.y/3);
    solo.sleep(3000);
    solo.clickOnView(solo.getView(R.id.action_profile));
    solo.sleep(3000);

    assertTrue(solo.waitForText("Temperature"));
    CombinedChart chart = (CombinedChart) solo.getView(R.id.propertyChart) ;
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);


    assertTrue(solo.waitForText("Salinity"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);


    assertTrue(solo.waitForText("Oxygen"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Phosphate"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Silicate"));
    checkForChartData();
    solo.scrollToSide(Solo.RIGHT);

    assertTrue(solo.waitForText("Nitrate"));
    checkForChartData();
  }
  /**
   * Test that the seekbar results
   * in a change in the map view.  This
   * is a visual test that we cannot really
   * make any programatic assertions against.
   */
  public void testSeekBar(){
    assertTrue(solo.waitForDialogToClose());
    solo.sleep(3000);
    solo.setProgressBar((SeekBar)solo.getView(R.id.seekBar), 15);
    solo.sleep(10000);
    solo.setProgressBar((SeekBar)solo.getView(R.id.seekBar), 40);
    solo.sleep(10000);
    solo.setProgressBar((SeekBar)solo.getView(R.id.seekBar), 95);
    solo.sleep(10000);
  }
  /**
   * Helper method that clicks on
   * an ocean location
   */
  private void clickOnOceanPoint(){
    assertTrue(solo.waitForDialogToClose());
    // Near the Galapagos Islands
    Point start = new Point(-95.0974397, -0.05932, SpatialReferences.getWgs84());
    android.graphics.Point screenPoint = deriveScreenPointForLocation(start);

    solo.clickOnScreen(screenPoint.x, screenPoint.y );
    assertTrue(solo.waitForText("Location Summary"));

    android.graphics.Point p = new android.graphics.Point();
    getActivity().getWindowManager().getDefaultDisplay().getSize(p);
    int fromX, toX, fromY, toY = 0;
    fromX = p.x/2;
    toX = p.x/2;
    fromY = (p.y/2) + (p.y/3);
    toY = (p.y/2) - (p.y/3);
    solo.sleep(3000);
    // Drag UP
   solo.drag(fromX, toX, fromY, toY, 40);
  }

  private void checkForChartData(){
    CombinedChart chart = (CombinedChart) solo.getView(R.id.propertyChart) ;
    assertTrue(chart.getData().getAllData().size() > 0);
    chart = null;
  }
  private int recyclerCount(){
    RecyclerView view = (RecyclerView) solo.getView(R.id.summary_recycler_view) ;
    return view.getAdapter().getItemCount();
  }
  private android.graphics.Point deriveScreenPointForLocation(Point location){
    MapView mapView = (MapView) solo.getView(R.id.map) ;
    DisplayMetrics metrics = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);;
    float screenHeight = metrics.heightPixels;
    float mapViewHeight = mapView.getHeight();
    float buffer = screenHeight - mapViewHeight;
    Point projectedPoint = (Point) GeometryEngine.project(location, SpatialReference.create(3857));

    android.graphics.Point derivedPoint =  mapView.locationToScreen(projectedPoint);

    return new android.graphics.Point(derivedPoint.x,derivedPoint.y+Math.round(buffer));

  }
}
