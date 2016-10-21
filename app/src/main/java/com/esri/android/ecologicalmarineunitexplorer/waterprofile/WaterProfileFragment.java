package com.esri.android.ecologicalmarineunitexplorer.waterprofile;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterProfile;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ScatterData;

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

public class WaterProfileFragment extends Fragment implements WaterProfileContract.View {

  private View mRoot;
  private WaterProfileContract.Presenter mPresenter;
  private ProgressDialog mProgressDialog;

  public static WaterProfileFragment newInstance() {
    WaterProfileFragment fragment = new WaterProfileFragment();
    return fragment;
  }
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    mRoot = layoutInflater.inflate(R.layout.water_profile, container, false);
    mPresenter.start();
    return  mRoot;
  }

  @Override public void showWaterProfile(ScatterData scatterData) {
    ScatterChart chart = (ScatterChart) mRoot.findViewById(R.id.propertyChart);

    chart.getAxisLeft().setInverted(false);
    chart.getXAxis().setEnabled(true);
    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    chart.getXAxis().setAxisMinValue(scatterData.getXMin()-1);
    chart.getAxisRight().setEnabled(false);
    chart.getAxisLeft().setDrawGridLines(true);
    chart.setDrawGridBackground(true);
    chart.setDescription("");
    chart.getLegend().setEnabled(false);
    chart.setData(scatterData);
    chart.invalidate();
  }

  @Override public void showMessage(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  @Override public void showProgressBar(String message, String title) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.setTitle(title);
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }

  @Override public void hideProgressBar() {
    mProgressDialog.dismiss();
  }

  @Override public void setPresenter(WaterProfileContract.Presenter presenter) {
    mPresenter = presenter;
  }
}
