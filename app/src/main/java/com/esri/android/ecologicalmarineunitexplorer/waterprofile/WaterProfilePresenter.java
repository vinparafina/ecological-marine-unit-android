package com.esri.android.ecologicalmarineunitexplorer.waterprofile;
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

import android.graphics.Color;
import android.support.annotation.NonNull;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.ServiceApi;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterProfile;
import com.esri.arcgisruntime.geometry.Point;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

public class WaterProfilePresenter implements WaterProfileContract.Presenter {
  private final Point mColumnLocation;
  private final WaterProfileContract.View mView;
  private final DataManager mDataManager;
  private final Map<String, ScatterData> mChartData = new HashMap<>();

  public WaterProfilePresenter(@NonNull Point p, @NonNull WaterProfileContract.View view, @NonNull DataManager dataManager) {
    mColumnLocation = p;
    mView = view;
    mView.setPresenter(this);
    mDataManager = dataManager;
  }

  @Override public void prepareDataForCharts(WaterProfile profile) {

  }

  @Override public void getWaterProfiles(Point point) {
    mView.showProgressBar("Building scatter plots", "Preparing Water Profile");
    mDataManager.queryForEMUColumnProfile(mColumnLocation, new ServiceApi.ColumnProfileCallback() {
      @Override public void onProfileLoaded(WaterProfile waterProfile) {
        if (waterProfile.measurementCount() > 0){
          List<ScatterData> dataList = new ArrayList<ScatterData>();
          dataList.add( buildChartDataForProperty(waterProfile, "TEMPERATURE"));
          dataList.add( buildChartDataForProperty(waterProfile, "SALINITY"));
          dataList.add( buildChartDataForProperty(waterProfile, "DISSOLVED_OXYGEN"));
          dataList.add( buildChartDataForProperty(waterProfile, "PHOSPHATE"));
          dataList.add( buildChartDataForProperty(waterProfile, "SILICATE"));
          dataList.add( buildChartDataForProperty(waterProfile, "NITRATE"));
          mView.showWaterProfiles(dataList);
        }else{
          // Notify user
          mView.showMessage("No profile data found");
        }
        mView.hideProgressBar();
      }
    });
  }

  @Override public void start() {
    getWaterProfiles(mColumnLocation);

  }
  private ScatterData buildChartDataForProperty(WaterProfile profile, String property){
    ScatterData data = new ScatterData();
    // Get all the measurements for the property
    Map<Double,Double> propertyMeasurementByDepth = profile.getMeasurementsForProperty(property);
    ArrayList<Entry> entries = new ArrayList<>();
    Set<Double> depths = propertyMeasurementByDepth.keySet();
    for (Double depth : depths){
      float y = (float) depth.doubleValue();
      float x = (float) propertyMeasurementByDepth.get(depth).doubleValue();
      entries.add(new Entry(x, y));
    }

    ScatterDataSet set = new ScatterDataSet(entries, property);
    set.setColor(Color.BLACK);
    set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
    set.setScatterShapeSize(5f);
    set.setDrawValues(false);
    data.addDataSet(set);
    return  data;
  }

}
