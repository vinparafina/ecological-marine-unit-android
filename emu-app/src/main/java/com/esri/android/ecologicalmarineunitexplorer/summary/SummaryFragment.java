package com.esri.android.ecologicalmarineunitexplorer.summary;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUObservation;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.util.EmuHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

public class SummaryFragment extends Fragment implements SummaryContract.View {


  private List<EMUObservation> emuObservations = new ArrayList<>();
  private RecyclerView mEmuObsView;
  private WaterColumn mWaterColumn;
  private EMUAdapter mEmuAdapter;
  private SummaryContract.Presenter mPresenter;
  private OnViewIndexChange mCallback;
  private OnDetailClickedListener mButtonListener;

  public static SummaryFragment newInstance() {
    SummaryFragment fragment = new SummaryFragment();
    return fragment;
  }

  // Listen for changes in index position of the recycler view
  public interface OnViewIndexChange {
    public void onChange(int position);
  }

  // Define the behavior for DETAIL button
  // clicks
  public interface OnDetailClickedListener{
    public void onButtonClick(int emuName);
  }
  @Override
  public final void onCreate(@NonNull final Bundle savedInstance) {
    super.onCreate(savedInstance);
    mEmuAdapter = new EMUAdapter(getContext(), R.id.summary_container, emuObservations);
  }

  @Override
  @Nullable
  public  View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){

    mEmuObsView = (RecyclerView) layoutInflater.inflate(R.layout.summary_recycler_view, container,false) ;

    mEmuObsView.setLayoutManager(new LinearLayoutManager(mEmuObsView.getContext() ));
    mEmuObsView.setAdapter(mEmuAdapter);

    return mEmuObsView;
  }

  @Override
  public void onAttach(Context activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (OnViewIndexChange) activity;
      mButtonListener = (OnDetailClickedListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnRectangleTappedListener and the OnDetailClickedListener");
    }
  }

  /**
   * Set the data for this view
   * @param waterColumn - A WaterColumn object containing EMUObservations to display
   */
  @Override public void showWaterColumn(WaterColumn waterColumn) {
    mWaterColumn = waterColumn;
    Set<EMUObservation> emuSet = waterColumn.getEmuSet();
    emuObservations.clear();
    for (EMUObservation observation : emuSet){
      emuObservations.add(observation);
    }
    if (mEmuAdapter != null){
      mEmuAdapter.notifyDataSetChanged();
    }
  }

  @Override public void scrollToSummary(int position) {
    mEmuObsView.scrollToPosition(position);
  }

  @Override public void setPresenter(SummaryContract.Presenter presenter) {
    mPresenter = presenter;
  }

  public class EMUAdapter extends RecyclerView.Adapter<RecycleViewHolder>{

    private List<EMUObservation> emuObservations = Collections.emptyList();
    private Context mContext;
    private int size;


    public EMUAdapter(final Context context, final int resource, final List<EMUObservation> observations){
      emuObservations = observations;
      mContext = context;
      size = emuObservations.size();
    }

    @Override public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View emuView = inflater.inflate(R.layout.summary_layout, parent, false);

      return new RecycleViewHolder(emuView);
    }

    /**
     * Bind data to the view
     * @param holder - the recycle view holder
     * @param position - position of the item in the data provider
     */
    @Override public void onBindViewHolder(RecycleViewHolder holder, final int position) {
      final EMUObservation observation = emuObservations.get(position);
      holder.txtThickness.setText(getString(R.string.layer_thickness_desc) + observation.getThickness() + getString(R.string.meters));
      holder.txtName.setText(observation.getEmu().getName().toString());
      holder.txtNutrients.setText(observation.getEmu().getNutrientSummary());
      holder.txtSummary.setText(observation.getEmu().getPhysicalSummary());
      int top = observation.getTop();
      holder.txtTop.setText(getString(R.string.below_surface_description) + top + getString(R.string.meters));
      GradientDrawable drawable = (GradientDrawable) holder.rectangle.getDrawable();
      drawable.setColor(Color.parseColor(EmuHelper.getColorForEMUCluster(observation.getEmu().getName())));
      holder.details.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
           mButtonListener.onButtonClick(observation.getEmu().getName());
         }
       });

      // Show/hide arrows
      if (position == (size -1)){
        holder.arrowDown.setVisibility(View.INVISIBLE);
      }else{
        holder.arrowDown.setVisibility(View.VISIBLE);
      }
      holder.bind(observation);
      // View index has changed, notify.
      mCallback.onChange(position);
    }


    @Override public int getItemCount() {
      return emuObservations.size();
    }
  }
  public class RecycleViewHolder extends RecyclerView.ViewHolder{

    public final TextView txtSummary ;
    public final TextView txtNutrients ;
    public final TextView txtName;
    public final TextView txtThickness;
    public final TextView txtTop;
    public final ImageView rectangle;
    public final Button details;
    public final ImageView arrowDown;

    public RecycleViewHolder(final View emuView){
      super(emuView);
      txtSummary = (TextView) emuView.findViewById(R.id.physical_summary);
      txtNutrients = (TextView) emuView.findViewById(R.id.nutrient_summary);
      txtName = (TextView) emuView.findViewById(R.id.txtName);
      txtThickness = (TextView) emuView.findViewById(R.id.txt_thickness);
      txtTop = (TextView) emuView.findViewById(R.id.txt_top);
      rectangle = (ImageView) emuView.findViewById(R.id.emu_rectangle);
      details = (Button) emuView.findViewById(R.id.btnDetail);
      arrowDown = (ImageView) emuView.findViewById(R.id.arrowDown);

    }
    public final void bind(final EMUObservation observation){
      //no op for now
    }

  }
}
