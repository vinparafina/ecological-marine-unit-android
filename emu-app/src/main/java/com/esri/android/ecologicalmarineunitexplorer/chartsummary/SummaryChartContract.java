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

package com.esri.android.ecologicalmarineunitexplorer.chartsummary;

import com.esri.android.ecologicalmarineunitexplorer.BasePresenter;
import com.esri.android.ecologicalmarineunitexplorer.BaseView;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUStat;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.github.mikephil.charting.data.CombinedData;

import java.util.List;

/**
 * This is the contract between the Presenter and View
 * components of the MVP pattern. It defines methods to
 * display physical properties for specific EMU layers.
 */

public interface SummaryChartContract {
  interface View extends BaseView<Presenter> {
    /**
     * Display the charts
     * @param dataList List<CombinedData></CombinedData>
     */
    void showChartData(List<CombinedData> dataList);

    /**
     * Set the label for the temperature chart
     * @param temperatureText - double
     */
    void setTemperatureText(double temperatureText);

    /**
     * Set the label for salinity chart
     * @param salinityText - double
     */
      void setSalinityText(double salinityText);

    /**
     * Set the label for oxygen chart
     * @param oxygenText - double
     */
    void setOxygenText(double oxygenText);

    /**
     * Set the label for the phosphate chart
     * @param phosphateText - double
     */
      void setPhosphateText(double phosphateText);

    /**
     * Set the label for the silicate chart
     * @param silicateText - double
     */
    void setSilicateText(double silicateText);

    /**
     * Set the label for the nitrate text
     * @param nitrateText - double
     */
      void setNitrateText(double nitrateText);

    /**
     * Display a progress bar with given message and title
     * @param message - String
     * @param title - String
     */
      void showProgressBar(String message, String title);

    /**
     * Hide the progress bar
     */
    void hideProgressBar();

    /**
     * Show a message
     * @param message - String
     */
      void showMessage(String message);
  }

  interface Presenter extends BasePresenter {
    /**
     * Provision data for charts
     * @param stat - EMUStat
     * @param waterColumn - WaterColumn
     * @param emuName - int representing EMU name
     */
      void prepareDataForCharts(EMUStat stat, WaterColumn waterColumn, int emuName);

    /**
     * Retrieve chart data for given EMU
     * @param emuName - int representing EMU name
     */
      void getDetailForSummary(int emuName);
  }
}

