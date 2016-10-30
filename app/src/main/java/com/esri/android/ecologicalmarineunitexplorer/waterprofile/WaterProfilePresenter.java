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
import android.util.Log;
import com.esri.android.ecologicalmarineunitexplorer.data.*;
import com.esri.android.ecologicalmarineunitexplorer.util.EmuHelper;
import com.esri.arcgisruntime.geometry.Point;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

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

          List<CombinedData> combinedDataList = new ArrayList<CombinedData>();

          combinedDataList.add(buildCombinedData(waterProfile,"TEMPERATURE"));
          combinedDataList.add(buildCombinedData(waterProfile,"SALINITY"));
          combinedDataList.add(buildCombinedData(waterProfile,"DISSOLVED_OXYGEN"));
          combinedDataList.add(buildCombinedData(waterProfile,"PHOSPHATE"));
          combinedDataList.add(buildCombinedData(waterProfile,"SILICATE"));
          combinedDataList.add(buildCombinedData(waterProfile,"NITRATE"));

          mView.showWaterProfiles(combinedDataList);
        }else{
          // Notify user
          mView.showMessage("No profile data found");
        }
        mView.hideProgressBar();
      }
    });
  }

  private CombinedData buildCombinedData(WaterProfile waterProfile, String property){
    CombinedData data = new CombinedData();
    ScatterData scatterData = buildScatterDataForProperty(waterProfile, property);
    data.setData(scatterData);
    LineData emuLayerData = buildEMULayers(data.getXMin() - 1, data.getXMax() + 1);
    data.setData(emuLayerData);
    return data;

  }
  @Override public void start() {
    getWaterProfiles(mColumnLocation);

  }
  private ScatterData buildScatterDataForProperty(WaterProfile profile, String property){
    ScatterData data = new ScatterData();
    // Get all the measurements for the property
    Map<Double,Double> propertyMeasurementByDepth = profile.getMeasurementsForProperty(property);
    ArrayList<Entry> entries = new ArrayList<>();
    Set<Double> depths = propertyMeasurementByDepth.keySet();
    for (Double depth : depths){
      float y = (float) Math.abs(depth.doubleValue());
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
  private LineData buildEMULayers(float xmin, float xmax){
    LineData data = new LineData();
    WaterColumn column = mDataManager.getCurrentWaterColumn();


    Set<EMUObservation> observations = column.getEmuSet();
    for (final EMUObservation observation : observations){
      Log.i("WaterProfile", observation.toString());
      ArrayList<Entry> entries = new ArrayList<Entry>();

      for (float index = xmin; index <= xmax; index++) {
        entries.add(new Entry(index, Math.abs(observation.getTop())));
      }

      LineDataSet set = new LineDataSet(entries, "Line DataSet");
      set.setAxisDependency(YAxis.AxisDependency.LEFT);
      set.setFillColor(Color.parseColor(EmuHelper.getColorForEMUCluster(observation.getEmu().getName())));

      set.setFillAlpha(255);
      set.setDrawCircles(false);
      set.setDrawValues(false);
      set.setDrawFilled(true);
      set.setHighLightColor(Color.rgb(244, 117, 117));
      set.setDrawCircleHole(false);
      set.setFillFormatter(new FillFormatter() {
        @Override
        public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
          return Math.abs(observation.getTop()) + observation.getThickness();
        }
      });
      data.addDataSet(set);
    }

    return data;
  }

}
