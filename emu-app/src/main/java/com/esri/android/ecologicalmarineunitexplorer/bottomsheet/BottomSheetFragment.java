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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUObservation;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.util.EmuHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ObjectAllocationInLoop") public class BottomSheetFragment extends Fragment implements BottomSheetContract.View {

  private LinearLayout mRoot = null;
  private LinearLayout mButtonContainer = null;
  private RecyclerView mEmuObsView = null;
  private Button mSelectedButton = null;
  private WaterColumn mWaterColumn = null;
  private BottomSheetFragment.EMUAdapter mEmuAdapter = null;

  private BottomSheetFragment.OnDetailClickedListener mButtonListener = null;

  public static BottomSheetFragment newInstance() {
    return new BottomSheetFragment();
  }

  // Define the behavior for DETAIL button
  // clicks
  public interface OnDetailClickedListener{
    void onButtonClick(int emuName);
  }

  /**
   *
   * @param savedInstance Bundle
   */
  @Override
  public final void onCreate(@NonNull final Bundle savedInstance) {
    super.onCreate(savedInstance);

    final List<EMUObservation> emuObservations = new ArrayList<>();
    mEmuAdapter = new BottomSheetFragment.EMUAdapter(getContext(), emuObservations);
  }

  /**
   *
   * @param layoutInflater LayoutInflater
   * @param container ViewGroup
   * @param savedInstance Bundle
   * @return
   */
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    super.onCreateView(layoutInflater, container, savedInstance);
    mRoot = (LinearLayout) container;
    mButtonContainer = (LinearLayout) mRoot.findViewById(R.id.buttonContainer);

    mEmuObsView = (RecyclerView) mRoot.findViewById(R.id.summary_recycler_view);
    mEmuObsView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mEmuObsView.setAdapter(mEmuAdapter);
    return null;
  }

  /**
   *
   * @param activity Context
   */
  @Override
  public void onAttach(final Context activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mButtonListener = (BottomSheetFragment.OnDetailClickedListener) activity;
    } catch (final ClassCastException e) {
      throw new ClassCastException(activity
          + " must implement OnRectangleTappedListener and the OnDetailClickedListener.");
    }
  }

  /**
   * Set the data for this view
   * @param waterColumn - A WaterColumn object containing EMUObservations to display
   */
  @Override public void showWaterColumn(final WaterColumn waterColumn) {
    if (waterColumn != null){
      // Scroll to top of recycler view
      scrollToSummary(0);
      mWaterColumn = waterColumn;
      final Set<EMUObservation> emuSet = waterColumn.getEmuSet();
      final List<EMUObservation> list = new ArrayList<>();
      for (final EMUObservation observation : emuSet){
        list.add(observation);
      }
      mEmuAdapter.setObservations(list);
      showWaterColumnButtons(waterColumn);
    }
  }
  /**
   * Dynamically add a button for each EMU represented
   * in the water column.
   * @param waterColumn - WaterColumn data object
   */
  private void showWaterColumnButtons(final WaterColumn waterColumn){
    mButtonContainer.removeAllViews();

    // Each button will be added to layout with a layout_weight
    // relative to the ratio of the EUMObservation to
    // the depth of the water column
    final Set<EMUObservation> emuObservationSet = waterColumn.getEmuSet();
    final float depth = waterColumn.getDepth();
    final TextView tv = (TextView) mRoot.findViewById(R.id.txtBottom);
    tv.setText(waterColumn.getDepth()+" m");

    int buttonId = 0;
    for (final EMUObservation observation: emuObservationSet){
      final float relativeSize = observation.getThickness() / depth * 100;
      final Button button = new Button(getContext());
      final LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          0, relativeSize);
      button.setLayoutParams(layoutParams);
      // Enable the button background to be change color based on its state (pressed, selected, or enabled)
      button.setBackground(buildStateList(observation.getEmu().getName()));

      button.setId(buttonId);
      button.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {
          if (mSelectedButton != null){
            mSelectedButton.setSelected(false);
          }
          button.setSelected(true);
          mSelectedButton = button;
          scrollToSummary(v.getId());
        }
      });
      mButtonContainer.addView(button);
      buttonId = buttonId + 1;
    }
  }
  /**
   * Build a stateful drawable for a given EMU
   * @param emuName String representing name
   * @return StateListDrawable responsive to selected, pressed, and enabled states
   */
  private static StateListDrawable buildStateList(final int emuName){
    final StateListDrawable stateListDrawable = new StateListDrawable();

    final GradientDrawable defaultShape = new GradientDrawable();
    final int color = Color.parseColor(EmuHelper.getColorForEMUCluster( emuName));
    defaultShape.setColor(color);

    final GradientDrawable selectedPressShape = new GradientDrawable();
    selectedPressShape.setColor(color);
    selectedPressShape.setStroke(5,Color.parseColor("#f4f442"));

    stateListDrawable.addState(new int[] { android.R.attr.state_pressed}, selectedPressShape);
    stateListDrawable.addState(new int[] { android.R.attr.state_selected}, selectedPressShape);
    stateListDrawable.addState(new int[] { android.R.attr.state_enabled}, defaultShape);


    return stateListDrawable;
  }

  /**
   * Display general text summary about the water column
   * @param x String representing longitude of clicked location
   * @param y String representing latitude of clicked location
   */
  @Override public void showLocationSummary(final String x, final String y) {
    final TextView textView = (TextView) getActivity().findViewById(R.id.txtSummary) ;
    textView.setText(getString(R.string.water_column_at) + y + ", "+ x +getString(R.string.lat_lng) +
        mWaterColumn.getEmuSet().size() + getString(
        R.string.extending_to)+ mWaterColumn.getDepth()+getString(R.string.meters_period));

  }

  /**
   *
   * @param position - int representing index of list item
   */
  @Override public void scrollToSummary(final int position) {

    mEmuObsView.scrollToPosition(position);
  }

  /**
   *
   * @param presenter - BottomSheetPresenter
   */
  @Override public void setPresenter(final BottomSheetContract.Presenter presenter) {
    BottomSheetContract.Presenter mPresenter = presenter;
  }

  /**
   * Set selected state of a water column segment
   * @param position
   */
  private void highlightSegment(final int position){
    final Button button =(Button) mButtonContainer.getChildAt(position);
    if (mSelectedButton != null){
      mSelectedButton.setSelected(false);
    }
    button.setSelected(true);
    mSelectedButton = button;
  }

  public class EMUAdapter extends RecyclerView.Adapter<BottomSheetFragment.RecycleViewHolder>{

    private List<EMUObservation> emuObservations = Collections.emptyList();
    private final Context mContext;

    /**
     *
     * @param context Context
     * @param observations List<EMUObservation> items
     */
    public EMUAdapter(final Context context, final List<EMUObservation> observations){
      emuObservations = observations;
      mContext = context;

    }

    /**
     * Set the observations to display
     * @param obs - List<EMUObservation> items
     */
    public void setObservations(final List<EMUObservation> obs){
      emuObservations = obs;
      notifyDataSetChanged();
    }

    /**
     *
     * @param parent ViewGroup
     * @param viewType int
     * @return an inflated RecycleViewHolder
     */
    @Override public BottomSheetFragment.RecycleViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
      final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View emuView = inflater.inflate(R.layout.summary_layout, parent, false);

      return new BottomSheetFragment.RecycleViewHolder(emuView);
    }

    /**
     * Bind data to the view
     * @param holder - the recycle view holder
     * @param position - position of the item in the data provider
     */
    @Override public void onBindViewHolder(final BottomSheetFragment.RecycleViewHolder holder, final int position) {
      final EMUObservation observation = emuObservations.get(position);
      holder.txtThickness.setText(getString(R.string.layer_thickness_desc) + observation.getThickness() + getString(
          R.string.meters));
      holder.txtName.setText(observation.getEmu().getName().toString());
      holder.txtNutrients.setText(observation.getEmu().getNutrientSummary());
      holder.txtSummary.setText(observation.getEmu().getPhysicalSummary());
      final int top = observation.getTop();
      holder.txtTop.setText(getString(R.string.below_surface_description) + top + getString(R.string.meters));
      final GradientDrawable drawable = (GradientDrawable) holder.rectangle.getDrawable();
      drawable.setColor(Color.parseColor(EmuHelper.getColorForEMUCluster(observation.getEmu().getName())));
      holder.details.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {
           mButtonListener.onButtonClick(observation.getEmu().getName());
         }
       });



      // Show/hide arrows
      if (position == getItemCount() - 1){
        holder.arrowDown.setVisibility(View.INVISIBLE);
      }else{
        holder.arrowDown.setVisibility(View.VISIBLE);
      }
      // View index has changed, notify.
      highlightSegment(position);
    }

    /**
     *
     * @return an int representing size of observation list
     */
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

    /**
     *
     * @param emuView View
     * @return RecycleViewHolder
     */
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
  }

}
