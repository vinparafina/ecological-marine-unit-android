package com.esri.android.ecologicalmarineunitexplorer.chartSummary;
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

import com.esri.android.ecologicalmarineunitexplorer.BasePresenter;
import com.esri.android.ecologicalmarineunitexplorer.BaseView;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUStat;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.github.mikephil.charting.data.CombinedData;

import java.util.List;

public interface SummaryChartContract {
  interface View extends BaseView<Presenter> {
      void showChartsForEMU(List<CombinedData> dataList);
  }

  interface Presenter extends BasePresenter {
      void prepareDataForCharts(EMUStat stat, WaterColumn waterColumn);
      void getDetailForSummary(int emuName);
  }
}

