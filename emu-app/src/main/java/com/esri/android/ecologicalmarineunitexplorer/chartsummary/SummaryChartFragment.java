package com.esri.android.ecologicalmarineunitexplorer.chartsummary;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.CombinedData;

import java.text.DecimalFormat;
import java.util.List;

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

public class SummaryChartFragment extends Fragment implements SummaryChartContract.View {

  private View mRoot;
  private SummaryChartContract.Presenter mPresenter;
  private ProgressDialog mProgressDialog;
  private String mUnits = null;

  public static SummaryChartFragment newInstance(){
    return new SummaryChartFragment();
  }


  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){

    mRoot = layoutInflater.inflate(R.layout.summary_charts, container,false);
    mPresenter.start();
    return mRoot;
  }

  @Override public void showChartData(List<CombinedData> dataList) {
    if (dataList != null){
      int size = dataList.size();
      for (int x=0; x < size - 1 ; x++){
        CombinedData data = dataList.get(x);
        int viewId = getIdForChartView(x);
        prepareChartView(viewId, data);
      }
      prepareLegend(dataList.get(size -1));
    }

  }

  @Override public void setTemperatureText(double temperatureText) {
    mUnits = " \u2103";
    TextView textView = (TextView) mRoot.findViewById(R.id.txtTemp);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(temperatureText))+mUnits);
  }

  public void setSalinityText(double salinityText) {
    TextView textView = (TextView) mRoot.findViewById(R.id.txtSalinity);
    mUnits = " ppm";
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(salinityText)) + mUnits);
  }

  @Override public void setOxygenText(double oxygenText) {
    mUnits = " \u00b5" + "m/L";
    TextView textView = (TextView) mRoot.findViewById(R.id.txtOxygen);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(oxygenText)) + mUnits);
  }

  @Override public void setPhosphateText(double phosphateText) {
    mUnits = " \u00b5" + "m/L";
    TextView textView = (TextView) mRoot.findViewById(R.id.txtPhosphate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(phosphateText)) + mUnits);
  }

  @Override public void setSilicateText(double silicateText) {
    mUnits = " \u00b5" + "m/L";
    TextView textView = (TextView) mRoot.findViewById(R.id.txtSilicate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(silicateText)) + mUnits);
  }

  @Override public void setNitrateText(double nitrateText) {
    mUnits = " \u00b5" + "m/L";
    TextView textView = (TextView) mRoot.findViewById(R.id.txtNitrate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(nitrateText)) + mUnits);
  }

  private void prepareChartView(int id, CombinedData data){
    CombinedChart chart = (CombinedChart) mRoot.findViewById(id);
    chart.getPaint(Chart.PAINT_DESCRIPTION).setTextAlign(Paint.Align.CENTER);
    chart.getXAxis().setEnabled(false);
    chart.getAxisRight().setEnabled(false);
    chart.getAxisLeft().setDrawGridLines(false);
    chart.setDescription("");
    chart.setDescriptionTextSize(10f);
    chart.setBackgroundColor(Color.WHITE);
    chart.setDrawGridBackground(false);

    chart.setData(data);
    chart.invalidate();

  }

  /**
   * Create a legend based on a dummy chart.  The legend
   * is used by all charts and is positioned
   * across the top of the screen.
   * @param data - CombinedData used to generate the legend
   */
  private void prepareLegend(CombinedData data){
    //The dummy chart is never shown, but it's legend is.
    CombinedChart dummyChart = (CombinedChart) mRoot.findViewById(R.id.legend);
    dummyChart.getPaint(Chart.PAINT_DESCRIPTION).setTextAlign(Paint.Align.CENTER);
    dummyChart.getXAxis().setEnabled(false);
    dummyChart.getAxisRight().setEnabled(false);
    dummyChart.getAxisLeft().setEnabled(false);
    dummyChart.setDescription("");
    dummyChart.setDescriptionTextSize(10f);
    dummyChart.setBackgroundColor(Color.WHITE);
    dummyChart.setDrawGridBackground(false);
    dummyChart.setData(data);

    Legend l = dummyChart.getLegend();
    l.setEnabled(true);
    // The positioning of the legend effectively
    // hides the dummy chart from view.
    l.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
    dummyChart.invalidate();
  }

  @Override public void setPresenter(SummaryChartContract.Presenter presenter) {
    mPresenter = presenter;
  }

  private int getIdForChartView(int index){
    int id = 0;
    switch (index){
      case 0:
        id = R.id.chart1;
        break;
      case 1:
        id = R.id.chart2;
        break;
      case 2:
        id = R.id.chart3;
        break;
      case 3:
        id = R.id.chart4;
        break;
      case 4:
        id = R.id.chart5;
        break;
      case 5:
        id = R.id.chart6;
        break;
    }
    return id;
  }
  /**
   * Display progress bar with given message and title
   * @param message - String representing message to display
   * @param title - String progress window title
   */
  @Override public void showProgressBar(String message, String title) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.setTitle(title);
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }

  /**
   * Hide progress bar
   */
  @Override public void hideProgressBar() {
    mProgressDialog.dismiss();
  }

  @Override public void showMessage(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }
}
