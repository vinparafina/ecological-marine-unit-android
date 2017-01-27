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
package com.esri.android.ecologicalmarineunitexplorer.bottomsheet;

import com.esri.android.ecologicalmarineunitexplorer.BasePresenter;
import com.esri.android.ecologicalmarineunitexplorer.BaseView;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;

/**
 * This is the contract between the Presenter and View
 * components of the MVP pattern. It defines methods
 * for displaying EMU layer summary data.
 */

public interface BottomSheetContract {

  interface View extends BaseView<BottomSheetContract.Presenter> {

    /**
     * Display EMU layers and data in the view
     * @param waterColumn - WaterColumn encapsulating EMU data
     */
    void showWaterColumn(WaterColumn waterColumn);

    /**
     * Show summary information about the selected location
     * @param x - String representing a longitude position
     * @param y - String representing a latitude position.
     */
    void showLocationSummary(String x, String y);

  }
  interface Presenter extends BasePresenter {
    /**
     * Set the WaterColumn model object in the presenter
     * @param waterColumn - WaterColumn encapsulating EMU data
     */
    void setWaterColumn(WaterColumn waterColumn);

  }
}
