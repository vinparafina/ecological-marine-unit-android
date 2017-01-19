package com.esri.android.ecologicalmarineunitexplorer.bottomsheet;



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

import android.support.annotation.NonNull;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.arcgisruntime.geometry.Point;

import java.text.DecimalFormat;

public class BottomSheetPresenter implements BottomSheetContract.Presenter {

  private BottomSheetFragment mBottomSheetView;
  private WaterColumn mWaterColumn;

  public BottomSheetPresenter(@NonNull BottomSheetFragment fragment){
    mBottomSheetView = fragment;
    mBottomSheetView.setPresenter(this);
  }
  @Override public void setWaterColumn(WaterColumn waterColumn) {
    mBottomSheetView.showWaterColumn(waterColumn);
    final Point p = waterColumn.getLocation();
    setLocationSummary(p);
  }

  private void setLocationSummary(Point p){
    String x = new DecimalFormat("#.##").format(p.getX());
    String y = new DecimalFormat("#.##").format(p.getY());
    mBottomSheetView.showLocationSummary(x,y);
  }
  @Override public void start() {

  }

}
