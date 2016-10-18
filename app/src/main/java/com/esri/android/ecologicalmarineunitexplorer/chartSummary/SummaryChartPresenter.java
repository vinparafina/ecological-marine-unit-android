package com.esri.android.ecologicalmarineunitexplorer.chartSummary;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import com.esri.android.ecologicalmarineunitexplorer.data.*;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

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

public class SummaryChartPresenter implements SummaryChartContract.Presenter {

  SummaryChartContract.View mView;
  DataManager mDataManager;
  List<CombinedData> mDataList = null;
  int currentEmuName = 0;

  public SummaryChartPresenter( int emuName, @NonNull SummaryChartContract.View view, @NonNull DataManager dataManager){
    mView = view;
    mDataManager = dataManager;
    mView.setPresenter(this);
    currentEmuName = emuName;
  }

  public void setEmuName(int nameId){
    currentEmuName = nameId;
  }
  @Override public void start() {
      getDetailForSummary(currentEmuName);
  }


  @Override public void getDetailForSummary(final int emuName) {
    EMUStat stat = mDataManager.getStatForEMU(emuName);
    final WaterColumn currentWaterColumn  = mDataManager.getCurrentWaterColumn();
    if (stat == null){
      mDataManager.getStatisticsForEMUs(new ServiceApi.StatCallback() {
        @Override public void onStatsLoaded() {
          // Get the EMU statistic for this EMU.  The EMU statistic
          // contains the stats for all locations with this EMU
          EMUStat emuStat =  mDataManager.getStatForEMU(emuName);

          // Prep the data for the charts
          prepareDataForCharts(emuStat, currentWaterColumn, emuName);
        }
      });
    }else{
      prepareDataForCharts(stat, currentWaterColumn, emuName);
    }
  }


  @Override public void prepareDataForCharts(@NonNull EMUStat stat, @NonNull WaterColumn waterColumn, int emuName) {
    checkNotNull(stat);
    checkNotNull(waterColumn);

    // Get the EMUs specific to this location from the water column
    List<EMUObservation> list = waterColumn.getEMUObservations(emuName);
    // The observation list should contain at least one EMUObservation!!
    // Grab the first one for now..
    EMUObservation observation = list.get(0);



    List<CombinedData> dataList = new ArrayList<>();

    dataList.add(0, buildTempData(observation, stat));
    dataList.add(1, buildSalinityData(observation,stat));

    mDataList = dataList;
    double tempOfCurrentEMu = observation.getTemperature();
    double salinityOfcurrentEMu = observation.getSalinity();
    mView.setTemperatureText(tempOfCurrentEMu);
    mView.setSalinityText(salinityOfcurrentEMu);
    mView.showChartData(mDataList);
  }

  private CombinedData buildTempData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    float xIndex = 1.5f;
    float close = (float) stat.getTemp_min().doubleValue();
    float open = (float) stat.getTemp_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxTemperatureFromSummary().doubleValue();   // 30.33f; // Greatest max temp from summary table
    float shadowL = (float) mDataManager.getMinTemperatureFromSummary().doubleValue();  // -2.05f; // Lowest min temp from summary table

    float averageTemp = (float)observation.getTemperature().doubleValue();
    Log.i("SummaryChartPreseter", "Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageTemp);
    //entries.add(new CandleEntry(1.5f , 90, 70, 85, 75f)); //index, shadowH, shadowL, open, close


    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close ));
    combinedData.setData(generateScatterData(averageTemp));
    return  combinedData;
  }
  private CombinedData buildSalinityData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    float xIndex = 1.5f;
    float close = (float) stat.getSalinity_min().doubleValue();
    float open = (float) stat.getSalinity_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxSalinityFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinSalinityFromSummary().doubleValue();

    float averageTemp = (float)observation.getSalinity().doubleValue();
    Log.i("SummaryChartPreseter", "Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageTemp);
    //entries.add(new CandleEntry(1.5f , 90, 70, 85, 75f)); //index, shadowH, shadowL, open, close


    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close ));
    combinedData.setData(generateScatterData(averageTemp));
    return  combinedData;
  }

  private CandleData generateCandleData(float xIndex, float shadowH, float shadowL, float open, float close){
    CandleData d = new CandleData();
    ArrayList<CandleEntry> entries = new ArrayList<>();
    entries.add(new CandleEntry(xIndex, shadowH, shadowL, open, close));
    CandleDataSet set = new CandleDataSet(entries, "Candle DataSet");
    set.setDecreasingColor(Color.rgb(142, 150, 175));
    set.setShadowColor(Color.DKGRAY);
    set.setBarSpace(0.3f);
    set.setValueTextSize(10f);
    set.setDrawValues(false);
    d.addDataSet(set);
    return d;
  }

  private ScatterData generateScatterData(float averageValue){
    ScatterData d = new ScatterData();
    ArrayList<Entry> entries = new ArrayList<>();
    entries.add(new Entry(1.5f, averageValue));
    ScatterDataSet set = new ScatterDataSet(entries, "Scatter DataSet");
    set.setColors(ColorTemplate.MATERIAL_COLORS);
    set.setScatterShape(ScatterChart.ScatterShape.SQUARE);

    set.setScatterShapeSize(9f);
    set.setDrawValues(false);
    set.setValueTextSize(10f);
    d.addDataSet(set);
    return  d;

  }

}
