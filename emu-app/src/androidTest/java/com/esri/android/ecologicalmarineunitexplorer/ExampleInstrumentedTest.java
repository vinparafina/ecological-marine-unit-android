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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.util.Log;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUStat;
import com.esri.android.ecologicalmarineunitexplorer.data.ServiceApi;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
  @Test
  public void useAppContext() throws Exception {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();

    assertEquals("com.esri.android.ecologicalmarineunitexplorer", appContext.getPackageName());
  }
  @Test
  public void testEMUSummaryService() throws Exception{
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();

    final DataManager dataManager = DataManager.getDataManagerInstance(appContext);

    dataManager.queryEMUSummaryStatistics(new ServiceApi.StatCallback() {
      @Override public void onStatsLoaded(boolean flag) {
        EMUStat stat = dataManager.getStatForEMU(24);
        Log.d("TEST", stat.toString());
        assertNotNull(stat);
      }
    });
  }
}
