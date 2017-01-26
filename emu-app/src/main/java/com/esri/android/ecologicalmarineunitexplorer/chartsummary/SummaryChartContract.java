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

import com.esri.android.ecologicalmarineunitexplorer.BasePresenter;
import com.esri.android.ecologicalmarineunitexplorer.BaseView;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUStat;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.github.mikephil.charting.data.CombinedData;

import java.util.List;

public interface SummaryChartContract {
  interface View extends BaseView<Presenter> {
      void showChartData(List<CombinedData> dataList);
      void setTemperatureText(double temperatureText);
      void setSalinityText(double salinityText);
      void setOxygenText(double oxygenText);
      void setPhosphateText(double phosphateText);
      void setSilicateText(double silicateText);
      void setNitrateText(double nitrateText);
      void showProgressBar(String message, String title);
      void hideProgressBar();
      void showMessage(String message);
  }

  interface Presenter extends BasePresenter {
      void prepareDataForCharts(EMUStat stat, WaterColumn waterColumn, int emuName);
      void getDetailForSummary(int emuName);
  }
}

