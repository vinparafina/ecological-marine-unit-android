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

package com.esri.android.ecologicalmarineunitexplorer.chartsummary;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import com.esri.android.ecologicalmarineunitexplorer.data.*;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the concrete implementation of the Presenter defined in the SummaryChartContract.
 * It encapsulates business logic and drives the behavior of the View.
 */

public class SummaryChartPresenter implements SummaryChartContract.Presenter {

  SummaryChartContract.View mView;
  DataManager mDataManager;
  List<CombinedData> mDataList = null;
  int currentEmuName = 0;

  private final String TEMPERATURE = "TEMPERATURE";
  private final String SALINITY = "SALINITY";
  private final String OXYGEN = "OXYGEN";
  private final String NITRATE = "NITRATE";
  private final String SILICATE = "SILICATE";
  private final String PHOSPHATE = "PHOSPHATE";

  public SummaryChartPresenter( int emuName, @NonNull SummaryChartContract.View view, @NonNull DataManager dataManager){
    mView = view;
    mDataManager = dataManager;
    mView.setPresenter(this);
    currentEmuName = emuName;
  }

  /**
   * Start by kicking off the process of
   * retrieving chart data for an EMU
   */
  @Override public void start() {
    mView.showProgressBar("Building charts...", "Preparing Detail View");
    getDetailForSummary(currentEmuName);
  }

  /**
   * Retrieve and provision chart data for given EMU
   * @param emuName - int representing EMU name
   */
  @Override public void getDetailForSummary(final int emuName) {
    EMUStat stat = mDataManager.getStatForEmu(emuName);
    final WaterColumn currentWaterColumn  = mDataManager.getCurrentWaterColumn();
    if (stat == null){
      mDataManager.queryEmuSummaryStatistics(new ServiceApi.StatCallback() {
        @Override public void onStatsLoaded(boolean successFlag) {
          if (successFlag){
            // Get the EMU statistic for this EMU.  The EMU statistic
            // contains the stats for all locations with this EMU
            EMUStat emuStat =  mDataManager.getStatForEmu(emuName);

            // Prep the data for the charts
            prepareDataForCharts(emuStat, currentWaterColumn, emuName);
          }else{
            mView.showMessage("There was a problem getting details for the EMU");
          }
        }
      });
    }else{
      prepareDataForCharts(stat, currentWaterColumn, emuName);
    }
  }

  /**
   * Prepare data for displaying in charts
   * @param stat - EMUStat
   * @param waterColumn - WaterColumn
   * @param emuName - int representing EMU name
   */
  @Override public void prepareDataForCharts(@NonNull EMUStat stat, @NonNull WaterColumn waterColumn, int emuName) {
    checkNotNull(stat);
    checkNotNull(waterColumn);

    // Get the EMUs specific to this location from the water column
    List<EMUObservation> list = waterColumn.getEMUObservations(emuName);
    // The observation list should contain at least one EMUObservation!!
    // Grab the first one for now..
    if (list.size() > 0){
      EMUObservation observation = list.get(0);

      List<CombinedData> dataList = new ArrayList<>();

      dataList.add(0, buildTempData(observation, stat));
      dataList.add(1, buildSalinityData(observation,stat));
      dataList.add(2, buildOxygenData(observation,stat));
      dataList.add(3, buildNitrateData(observation,stat));
      dataList.add(4, buildPhosphateData(observation,stat));
      dataList.add(5, buildSilicateData(observation,stat));
      dataList.add(6, buildDummyDataForLegend());

      mDataList = dataList;
      double tempOfCurrentEMu = observation.getTemperature() != null ? observation.getTemperature(): 0d;
      double salinityOfcurrentEMu = observation.getSalinity() != null ? observation.getSalinity() : 0d;
      double oxygenOfCurrentEmu = observation.getOxygen() != null ? observation.getOxygen() : 0d;
      double phosphateOfCurrentEmu = observation.getPhosphate() != null ? observation.getPhosphate(): 0d;
      double silicateOfCurrentEmu = observation.getSilicate() != null ? observation.getSilicate() : 0d;
      double nitrateOfCurrentEmu = observation.getNitrate() != null ? observation.getNitrate() : 0d;

      mView.setTemperatureText(tempOfCurrentEMu);
      mView.setSalinityText(salinityOfcurrentEMu);
      mView.setOxygenText(oxygenOfCurrentEmu);
      mView.setPhosphateText(phosphateOfCurrentEmu);
      mView.setSilicateText(silicateOfCurrentEmu);
      mView.setNitrateText(nitrateOfCurrentEmu);

      mView.showChartData(mDataList);
      mView.hideProgressBar();
    }else{
      mView.hideProgressBar();
      mView.showMessage("No chart data found for layer");
    }
  }

  /**
   * Create dataset to be displayed in chart legend
   * @return CombinedData representing dummy data for a legend
   */
  private CombinedData buildDummyDataForLegend(){
    CombinedData combinedData = new CombinedData();
    float xIndex = 1.5f;
    float close = 13;
    float open = 26f;
    float shadowH = 30.33f;
    float shadowL = -2.05f;
    float average = 20f;
    CandleData candleData = generateCandleData(xIndex, shadowH, shadowL, open, close, "EMU HI/LO" );
    ScatterData scatterData = generateScatterData(average, "EMU Mean");

    LineData s1 = generateOceanHiLo(close, open, "Ocean HI/LO");
    combinedData.setData(s1);
    combinedData.setData(candleData);
    combinedData.setData(scatterData);
    return combinedData;
  }

  /**
   * Build a CombinedData object containing temperature data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildTempData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    if (stat.getTemp_max() == null || stat.getTemp_min() == null || stat.getTemp_mean() == null || observation.getTemperature() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getTemp_min().doubleValue();
    float open = (float) stat.getTemp_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxTemperatureFromSummary().doubleValue();   // 30.33f; // Greatest max temp from summary table
    float shadowL = (float) mDataManager.getMinTemperatureFromSummary().doubleValue();  // -2.05f; // Lowest min temp from summary table

    float averageTemp = (float)observation.getTemperature().doubleValue();
    Log.i("SummaryChartPresenter", "Temperature: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageTemp);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, TEMPERATURE ));
    ScatterData sdata = generateScatterData(averageTemp, TEMPERATURE);
    combinedData.setData(sdata);
    return  combinedData;
  }

  /**
   * Build a CombinedData object containing salinity data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildSalinityData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    if (stat.getSalinity_max() == null || stat.getSalinity_min() == null || stat.getSalinity_mean() == null || observation.getSalinity() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getSalinity_min().doubleValue();
    float open = (float) stat.getSalinity_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxSalinityFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinSalinityFromSummary().doubleValue();

    float avgSalinity = (float)observation.getSalinity().doubleValue();
    Log.i("SummaryChartPreseter", "Salinity: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ avgSalinity);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, SALINITY ));
    combinedData.setData(generateScatterData(avgSalinity, SALINITY));
    return  combinedData;
  }

  /**
   * Build a CombinedData object containing oxygen data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildOxygenData(EMUObservation observation, EMUStat stat){

    CombinedData combinedData = new CombinedData();
    if (stat.getDisso2_max() == null || stat.getDisso2_min() == null || stat.getDisso2_mean() == null || observation.getOxygen() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getDisso2_min().doubleValue();
    float open = (float) stat.getDisso2_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxOxygenFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinOxygenFromSummary().doubleValue();

    float averageOx = (float)observation.getOxygen().doubleValue();
    Log.i("SummaryChartPreseter", "Oxygen: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageOx);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, OXYGEN ));
    combinedData.setData(generateScatterData(averageOx, OXYGEN));
    return  combinedData;
  }

  /**
   * Build a CombinedData object containing phosphate data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildPhosphateData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    if (stat.getPhosphate_max() == null || stat.getPhosphate_min() == null || stat.getPhosphate_mean() == null || observation.getPhosphate() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getPhosphate_min().doubleValue();
    float open = (float) stat.getPhosphate_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxPhosphateFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinPhosphateFromSummary().doubleValue();

    float averagePhos = (float)observation.getPhosphate().doubleValue();
    Log.i("SummaryChartPreseter", "Phosphate: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averagePhos);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, PHOSPHATE ));
    combinedData.setData(generateScatterData(averagePhos, PHOSPHATE));
    return  combinedData;
  }

  /**
   * Build a CombinedData object containing silicate data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildSilicateData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    if (stat.getSilicate_max() == null || stat.getSilicate_min() == null || stat.getSilicate_mean() == null || observation.getSilicate() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getSilicate_min().doubleValue();
    float open = (float) stat.getSilicate_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxSilicateFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinSilicateFromSummary().doubleValue();

    float averageSil = (float)observation.getSilicate().doubleValue();
    Log.i("SummaryChartPreseter", "Silicate: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageSil);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, SILICATE ));
    combinedData.setData(generateScatterData(averageSil, SILICATE));
    return  combinedData;
  }

  /**
   * Build a CombinedData object containing nitrate data
   * @param observation - EMUObservation
   * @param stat - EMUStat
   * @return - CombinedData
   */
  private CombinedData buildNitrateData(EMUObservation observation, EMUStat stat){
    CombinedData combinedData = new CombinedData();
    if (stat.getNitrate_min() == null || stat.getNitrate_max() == null || stat.getNitrate_mean() == null || observation.getNitrate() == null){
      return combinedData;
    }
    float xIndex = 1.5f;
    float close = (float) stat.getNitrate_min().doubleValue();
    float open = (float) stat.getNitrate_max().doubleValue();
    float shadowH = (float)mDataManager.getMaxNitrateFromSummary().doubleValue();
    float shadowL = (float) mDataManager.getMinNitrateFromSummary().doubleValue();

    float averageN = (float)observation.getNitrate().doubleValue();
    Log.i("SummaryChartPreseter", "Nitrate: Ocean high = " + shadowH + " ocean low = "+ shadowL + " emu min = " + close + " emu max = "+ open + " emu mean for location = "+ averageN);

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close, NITRATE ));
    combinedData.setData(generateScatterData(averageN, NITRATE));
    return combinedData;
  }

  /**
   * Prepare CandleData for candlestick chart
   * @param xIndex - float
   * @param shadowH - float
   * @param shadowL - float
   * @param open - float
   * @param close - float
   * @param seriesName - String
   * @return CandleData
   */
  private CandleData generateCandleData(float xIndex, float shadowH, float shadowL, float open, float close, String seriesName){
    CandleData d = new CandleData();
    ArrayList<CandleEntry> entries = new ArrayList<>();
    entries.add(new CandleEntry(xIndex, shadowH, shadowL, open, close));
    CandleDataSet set = new CandleDataSet(entries, seriesName);
    set.setDecreasingColor(Color.rgb(142, 150, 175));
    set.setShadowColor(Color.DKGRAY);
    set.setDecreasingColor(Color.parseColor("#2196F3"));
    set.setBarSpace(0.3f);
    set.setValueTextSize(10f);
    set.setShadowWidth(2f);
    set.setDrawValues(false);
    d.addDataSet(set);
    return d;
  }

  /**
   * Prepare ScatterData object
   * @param averageValue
   * @param seriesName
   * @return ScatterData
   */
  private ScatterData generateScatterData(float averageValue,  String seriesName){
    ScatterData d = new ScatterData();
    ArrayList<Entry> entries = new ArrayList<>();
    entries.add(new Entry(1.5f, averageValue));
    ScatterDataSet set = new ScatterDataSet(entries, seriesName);
    set.setColor(Color.parseColor("#FF4081"));
    set.setScatterShape(ScatterChart.ScatterShape.SQUARE);
    set.setScatterShapeSize(15f);
    set.setDrawValues(false);
    set.setValueTextSize(10f);
    d.addDataSet(set);
    return  d;
  }

  /**
   * Prepare LineData object
   * @param open - float
   * @param close - float
   * @param seriesName - String
   * @return LineData
   */
  private LineData generateOceanHiLo(float open, float close, String seriesName){
    LineData d = new LineData();
    ArrayList<Entry> entries = new ArrayList<>();
    entries.add(new Entry(1.5f, open));
    LineDataSet set = new LineDataSet(entries, seriesName);
    set.setDrawValues(false);
    set.setColor(Color.BLACK);
    set.setValueTextSize(10f);
    d.addDataSet(set);
    return  d;
  }
}
