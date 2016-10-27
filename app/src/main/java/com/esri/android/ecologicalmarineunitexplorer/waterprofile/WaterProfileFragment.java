package com.esri.android.ecologicalmarineunitexplorer.waterprofile;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterProfile;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
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

public class WaterProfileFragment extends Fragment implements WaterProfileContract.View {

  private RecyclerView mChartRecyclerView;
  private WaterProfileContract.Presenter mPresenter;
  private ChartAdapter mChartAdapter;
  private ProgressDialog mProgressDialog;
  private List<CombinedData> mChartDataList = new ArrayList<>();

  public static WaterProfileFragment newInstance() {
    WaterProfileFragment fragment = new WaterProfileFragment();
    return fragment;
  }
  @Override
  public final void onCreate(@NonNull final Bundle savedInstance){
    super.onCreate(savedInstance);
    mChartAdapter =  new ChartAdapter(getContext(), R.id.chartContainer, mChartDataList);
    // MP Android chart
    Utils.init(getContext());
  }
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    mChartRecyclerView = (RecyclerView) layoutInflater.inflate(R.layout.water_profile_recycle_view, container, false) ;
    mChartRecyclerView.setLayoutManager(new LinearLayoutManager(mChartRecyclerView.getContext()));
    mChartRecyclerView.setAdapter(mChartAdapter);
    mPresenter.start();
    return  mChartRecyclerView;
  }

  @Override public void showWaterProfiles(List<CombinedData> dataList ) {
    mChartDataList = dataList;
    ScatterChart chart = (ScatterChart) mChartRecyclerView.findViewById(R.id.propertyChart);
    if (mChartAdapter != null){
      mChartAdapter.notifyDataSetChanged();
    }
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

  public class ChartAdapter extends RecyclerView.Adapter<ChartViewHolder>{

    public ChartAdapter(final Context context, final int resource, final List<CombinedData> combinedData){
      mChartDataList = combinedData;
      // Determine the color for each of the
    }

    @Override public ChartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View view = inflater.inflate(R.layout.water_profile, parent,false);
      return new ChartViewHolder(view);
    }

    @Override public void onBindViewHolder(ChartViewHolder holder, int position) {
      final CombinedData scatterData = mChartDataList.get(position);
      String [] labels = scatterData.getDataSetLabels();
      String property = scatterData.getDataSetLabels()[labels.length -1];

      holder.txtChartTitle.setText( property + " vs. Ocean Depth");
      holder.chart.setData(scatterData);
      holder.chart.getAxisLeft().setInverted(false);
      holder.chart.getXAxis().setEnabled(true);
      holder.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
      holder.chart.getXAxis().setAxisMinValue(scatterData.getXMin()-1);
      holder.chart.getXAxis().setAxisMaxValue(scatterData.getXMax() + 1);
      holder.chart.getAxisRight().setEnabled(false);
      holder.chart.getAxisLeft().setDrawGridLines(true);
      holder.chart.setDrawGridBackground(true);
      holder.chart.setDescription("");
      holder.chart.getLegend().setEnabled(false);
      holder.chart.setData(scatterData);
      holder.chart.invalidate();
      if (property.equalsIgnoreCase("TEMPERATURE")){
        property = property + " C";
      }else if (property.equalsIgnoreCase("SALINITY")){
        property = property + " ppm";
      }else{
        property = property + " um/L";
      }
      holder.txtXAxisTitle.setText(property);

    }

    @Override public int getItemCount() {
      return mChartDataList.size();
    }
  }
  public class ChartViewHolder extends RecyclerView.ViewHolder{
    public final TextView txtChartTitle;
    public final TextView txtXAxisTitle;
    public final CombinedChart chart;

    public ChartViewHolder(final View view){
      super(view);
      txtChartTitle = (TextView) view.findViewById(R.id.txtChartTitle);
      txtXAxisTitle = (TextView) view.findViewById(R.id.txtXAxisTitle);
      chart = (CombinedChart) view.findViewById(R.id.propertyChart);
      chart.setDrawOrder(new CombinedChart.DrawOrder[]{ CombinedChart.DrawOrder.LINE,
           CombinedChart.DrawOrder.SCATTER
      });
    }

  }
}
