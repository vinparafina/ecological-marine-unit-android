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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.CombinedData;

import java.text.DecimalFormat;
import java.util.List;

/**
 * This fragment is responsible for building and displaying candlestick charts for a EMU at a specific geographic
 * location. It's the View in the MVP pattern and the concrete implementation of the
 * SummaryChartContract.View interface.
 */

public class SummaryChartFragment extends Fragment implements SummaryChartContract.View {

  private View mRoot = null;
  private SummaryChartContract.Presenter mPresenter = null;
  private ProgressDialog mProgressDialog = null;
  private String mUnits = null;

  public static SummaryChartFragment newInstance(){
    return new SummaryChartFragment();
  }

  /**
   *
   * @param layoutInflater - LayoutInflater
   * @param container - ViewGroup
   * @param savedInstance - Bundle
   * @return View
   */
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    super.onCreateView(layoutInflater, container, savedInstance);
    mRoot = layoutInflater.inflate(R.layout.summary_charts, container,false);
    mPresenter.start();
    return mRoot;
  }

  /**
   * Display chart data
   * @param dataList - List<CombinedData> for populating charts
   */
  @Override public void showChartData(final List<CombinedData> dataList) {
    if (dataList != null){
      final int size = dataList.size();
      for (int x = 0; x < (size - 1); x++){
        final CombinedData data = dataList.get(x);
        final int viewId = getIdForChartView(x);
        prepareChartView(viewId, data);
      }
      prepareLegend(dataList.get(size -1));
    }
  }

  /**
   * Set temperature label
   * @param temperatureText - Double value representing temperature level
   */
  @Override public void setTemperatureText(final double temperatureText) {
    mUnits = " \u2103";
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtTemp);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(temperatureText))+mUnits);
  }

  /**
   * Set salinity label
   * @param salinityText - Double value representing salinity level
   */
  @Override public void setSalinityText(final double salinityText) {
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtSalinity);
    mUnits = getString(R.string.ppm);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(salinityText)) + mUnits);
  }

  /**
   * Set oxygen label
   * @param oxygenText - Double value representing oxygen level
   */
  @Override public void setOxygenText(final double oxygenText) {
    mUnits = " \u00b5" + getString(R.string.ml);
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtOxygen);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(oxygenText)) + mUnits);
  }

  /**
   * Set phosphate label
   * @param phosphateText - Double value representing phosphate level
   */
  @Override public void setPhosphateText(final double phosphateText) {
    mUnits = " \u00b5" + getString(R.string.ml);
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtPhosphate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(phosphateText)) + mUnits);
  }

  /**
   * Set silicate label
   * @param silicateText - Double value representing silicate level
   */
  @Override public void setSilicateText(final double silicateText) {
    mUnits = " \u00b5" + getString(R.string.ml);
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtSilicate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(silicateText)) + mUnits);
  }

  /**
   * Set the nitrate label
   * @param nitrateText - Double value representing nitrate level
   */
  @Override public void setNitrateText(final double nitrateText) {
    mUnits = " \u00b5" + getString(R.string.ml);
    final TextView textView = (TextView) mRoot.findViewById(R.id.txtNitrate);
    textView.setText(Double.valueOf(new DecimalFormat("#.##").format(nitrateText)) + mUnits);
  }

  /**
   * Build out the charts for given dataset
   * @param id - int representing chart id
   * @param data - CombinedData displayed in the chart
   */
  private void prepareChartView(final int id, final CombinedData data){
    final CombinedChart chart = (CombinedChart) mRoot.findViewById(id);
    chart.getPaint(Chart.PAINT_DESCRIPTION).setTextAlign(Paint.Align.CENTER);
    chart.getXAxis().setEnabled(false);
    chart.getAxisRight().setEnabled(false);
    chart.getAxisLeft().setDrawGridLines(false);
    chart.getDescription().setEnabled(false);
    chart.getLegend().setEnabled(false);
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
  private void prepareLegend(final CombinedData data){
    //The dummy chart is never shown, but it's legend is.
    final CombinedChart dummyChart = (CombinedChart) mRoot.findViewById(R.id.legend);
    dummyChart.getPaint(Chart.PAINT_DESCRIPTION).setTextAlign(Paint.Align.CENTER);
    dummyChart.getXAxis().setEnabled(false);
    dummyChart.getAxisRight().setEnabled(false);
    dummyChart.getAxisLeft().setEnabled(false);
    final Description description = new Description();
    description.setText("");
    description.setTextSize(10f);
    dummyChart.setDescription(description);
    dummyChart.setBackgroundColor(Color.WHITE);
    dummyChart.setDrawGridBackground(false);
    dummyChart.setData(data);

    final Legend l = dummyChart.getLegend();
    l.setEnabled(true);
    // The positioning of the legend effectively
    // hides the dummy chart from view.
    l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
    dummyChart.invalidate();
  }

  @Override public void setPresenter(final SummaryChartContract.Presenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Determine chart id given index
   * @param index - int representing index
   * @return int representing chart id
   */
  private static int getIdForChartView(final int index){
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
      default:
        id = R.id.chart1;
    }
    return id;
  }
  /**
   * Display progress bar with given message and title
   * @param message - String representing message to display
   * @param title - String progress window title
   */
  @Override public void showProgressBar(final String message, final String title) {
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

  /**
   * Show a toast message using the provided message string.
   * @param message
   */
  @Override public void showMessage(final String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }
}
