package com.esri.android.ecologicalmarineunitexplorer.chartSummary;

import android.graphics.Color;
import android.support.annotation.NonNull;
import com.esri.android.ecologicalmarineunitexplorer.data.DataManager;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUStat;
import com.esri.android.ecologicalmarineunitexplorer.data.ServiceApi;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.summary.SummaryContract;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;

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
  public SummaryChartPresenter(@NonNull SummaryChartContract.View view, @NonNull DataManager dataManager){
    mView = view;
    mDataManager = dataManager;
  }

  @Override public void start() {

  }


  @Override public void getDetailForSummary(final int emuName) {
    EMUStat stat = mDataManager.getStatForEMU(emuName);
    if (stat == null){
      mDataManager.getStatisticsForEMUs(new ServiceApi.StatCallback() {
        @Override public void onStatsLoaded() {
          EMUStat emuStat =  mDataManager.getStatForEMU(emuName);
          WaterColumn currentWaterColumn  = mDataManager.getCurrentWaterColumn();
          // Prep the data for the charts
          prepareDataForCharts(emuStat, currentWaterColumn);
        }
      });
    }
  }
  @Override public void prepareDataForCharts(@NonNull EMUStat stat, @NonNull WaterColumn waterColumn) {
    checkNotNull(stat);
    checkNotNull(waterColumn);
    List<CombinedData> dataList = new ArrayList<>();

    CombinedData combinedData = new CombinedData();
    float xIndex = 1.5f;
    float close = (float) stat.getTemp_min().doubleValue();
    float open = (float) stat.getTemp_max().doubleValue();
    float shadowH = open + 10f;
    float shadowL = close  - 10f;

    combinedData.setData(generateCandleData(xIndex, shadowH, shadowL, open, close ));
    dataList.add(0, combinedData);

    mView.showChartsForEMU(dataList);


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
}
