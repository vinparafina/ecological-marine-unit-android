package com.esri.android.ecologicalmarineunitexplorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.robotium.solo.Solo;
import com.esri.android.ecologicalmarineunitexplorer.map.MapActivity;

import java.io.File;

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

/**
 * Test performance may vary based on network latency.  Remember to add specific values to your app_settings.xml for
 * some of these tests and ensure your have internet access and location tracking on.  The first time these
 * tests are run you will be prompted by the app for WRITE_EXTERNAL permission.  Test screenshots are stored in the
 * Robotium folder of your device's SD card.
 *
 */

public class EMUAppTest extends ActivityInstrumentationTestCase2
    implements ActivityCompat.OnRequestPermissionsResultCallback {

  private Solo solo;
  private static final int PERMISSION_WRITE_STORAGE = 4;
  private static boolean mPermissionsGranted = false;
  private static final String TAG = EMUAppTest.class.getSimpleName();


  public EMUAppTest() { super(MapActivity.class);}

  @Override
  public void setUp() throws Exception {
    //setUp() is run before a test case is started.
    //This is where the solo object is created.


    Solo.Config config = new Solo.Config();
    config.screenshotFileType = Solo.Config.ScreenshotFileType.JPEG;
    File sdcard = Environment.getExternalStorageDirectory();
    File data = new File(sdcard, "/Data");
    config.screenshotSavePath = data.getAbsolutePath() + "/Robotium/";
    Log.i(TAG, config.screenshotSavePath);
    config.shouldScroll = false;
    solo = new Solo(getInstrumentation(), config);
    if (!mPermissionsGranted){
      Log.i(TAG, "Seeking permissions");
      requestWritePermission();
    }
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
    assertTrue(solo.waitForView(getActivity().findViewById(R.id.map)));

    // Take a picture and store it


  }

  /**
   * Verify that clicking on the map places
   * a marker if the location is over an ocean
   */
  public void testClickOnOcean(){
    // This assumes viewpoint at start up hasn't
    // changed since map was initialized
    // x = 973.0 y =  891.0
    solo.clickOnScreen(1002,937,1);
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    boolean buttonFound = solo.searchButton("DETAILS");
    assertTrue(buttonFound);
  }

  /**
   * Verify that clicking on a land
   * location shows a toast message
   */
  public void testClickOnLand(){
    // This assumes viewpoint at start up hasn't
    // changed since map was initialized
    assertTrue(solo.waitForDialogToClose());
    solo.clickOnScreen(586,231,2);
    boolean messageShows = solo.waitForText(getActivity().getString(R.string.no_emu_found));
    assertTrue(messageShows);
  }

  /**
   * Verifty that a recycler view
   * containing EMU observations is loaded
   * when a point in the ocean is clicked.
   */
  public void testSummaryShown(){
    solo.clickOnScreen(400,1300,1);
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    boolean buttonFound = solo.searchButton("DETAILS");
    assertTrue(buttonFound);
    boolean scrollSuccess = solo.scrollDownRecyclerView(2);
    assertTrue(scrollSuccess);
    scrollSuccess = solo.scrollDownRecyclerView(3);
    assertTrue(scrollSuccess);
    scrollSuccess = solo.scrollUpRecyclerView(0);
    assertTrue(scrollSuccess);
  }

  /**
   * Verify that buttons in
   * the water column are displayed
   */
  public void testWaterColumnShown(){
    solo.clickOnScreen(400,1300,1);
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    boolean buttonFound = solo.searchButton("DETAILS");
    assertTrue(buttonFound);
    Button button = (Button) getActivity().findViewById(0);
    assertTrue(button != null);
    button = (Button) getActivity().findViewById(2);
    assertTrue(button != null);
  }

  /**
   * Validate that water column segments
   * are selected when the colored
   * rectangle in each recycler view item
   * is clicked.
   */
  public void testRectangleClickSelectsSegment(){
    solo.clickOnScreen(400,1300,1);
    boolean emuTextFound = solo.waitForText("EMU ");
    assertTrue(emuTextFound);
    boolean scrollsuccess = solo.scrollDownRecyclerView(2);
    assertTrue(scrollsuccess);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.emu_rectangle);
    imageView.performClick();
    Button button = (Button) getActivity().findViewById(2);
    int c = button.getPaint().getColor();
    String hexColor = String.format("#%06X", (0xFFFFFF & c));
    Log.i("color", hexColor);
  }
  private void requestWritePermission() {

    if (ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
      // Request the permission
      ActivityCompat.requestPermissions(getActivity(), new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
          PERMISSION_WRITE_STORAGE);
    }else{
      mPermissionsGranted = true;
    }
  }

  /**
   * Once the app has prompted for permission to write to external storage, the response
   * from the user is handled here.
   *
   * @param requestCode
   *            int: The request code passed into requestPermissions
   * @param permissions
   *            String: The requested permission(s).
   * @param grantResults
   *            int: The grant results for the permission(s). This will be
   *            either PERMISSION_GRANTED or PERMISSION_DENIED
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_WRITE_STORAGE) {
      // Request for write permission.
      if (grantResults.length != 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(getActivity(), "Permission to write to external storage required for screenshots",
            Toast.LENGTH_LONG).show();
      } else {
        mPermissionsGranted = true;
      }
    }
  }
}
