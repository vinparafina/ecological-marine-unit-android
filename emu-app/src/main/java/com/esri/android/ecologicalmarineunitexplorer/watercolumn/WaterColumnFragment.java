package com.esri.android.ecologicalmarineunitexplorer.watercolumn;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.esri.android.ecologicalmarineunitexplorer.data.EMUObservation;
import com.esri.android.ecologicalmarineunitexplorer.data.WaterColumn;
import com.esri.android.ecologicalmarineunitexplorer.map.MapContract;
import com.esri.android.ecologicalmarineunitexplorer.util.EmuHelper;

import java.util.Set;

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

public class WaterColumnFragment extends Fragment implements WaterColumnContract.View {

  private LinearLayout mRoot;
  private LinearLayout mButtonContainer;
  private WaterColumn mWaterColumn;
  private OnWaterColumnSegmentClickedListener mCallback;
  private Button mSelectedButton;
  private WaterColumnContract.Presenter mPresenter;


  // Define behavior for column clicking
  public interface OnWaterColumnSegmentClickedListener {
    public void onSegmentClicked(int position);
  }

  public static WaterColumnFragment newInstance(){
    return new WaterColumnFragment();
  }
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    mRoot = (LinearLayout) layoutInflater.inflate(R.layout.water_column, container,false);
    mButtonContainer = (LinearLayout) mRoot.findViewById(R.id.buttonContainer);
    if (mWaterColumn != null){
      showWaterColumn(mWaterColumn);
      Log.i("WaterColumnFragment", "Water column not null in onCreateView");
    }
    Log.i("WaterColumnFragment", "onCreateView");
    return  mRoot;
  }

  @Override
  public void onAttach(Context activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (OnWaterColumnSegmentClickedListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnWaterColumnSegmentClickedListener");
    }
  }

  public void setWaterColumn(WaterColumn waterColumn){

    mWaterColumn = waterColumn;
    if (mButtonContainer != null){
      showWaterColumn(waterColumn);
      Log.i("WaterColumnFragment", "Button container for water column is not null");
    }else{
      Log.i("WaterColumnFragment", "Button container for water column is null");
    }
    Log.i("WaterColumnFragment", "setWaterColumn");
  }
  /**
   * Dynamically add a button for each EMU represented
   * in the water column.
   * @param waterColumn
   */
  public void showWaterColumn(WaterColumn waterColumn){

    mButtonContainer.removeAllViews();

    // Each button will be added to layout with a layout_weight
    // relative to the ratio of the EUMObservation to
    // the depth of the water column
    Set<EMUObservation> emuObservationSet = waterColumn.getEmuSet();
    float depth = (float) waterColumn.getDepth();
    TextView tv = (TextView) mRoot.findViewById(R.id.txtBottom);
    tv.setText(waterColumn.getDepth()+" m");

    int buttonId = 0;
    for (EMUObservation observation: emuObservationSet){
      float relativeSize = (observation.getThickness()/depth) * 100;
      final Button button = new Button(getContext());
      LinearLayout.LayoutParams  layoutParams  =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0, relativeSize);
      button.setLayoutParams(layoutParams);
      // Enable the button background to be change color based on its state (pressed, selected, or enabled)
      button.setBackground(buildStateList(observation.getEmu().getName()));

      button.setId(buttonId);
      button.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mSelectedButton != null){
            mSelectedButton.setSelected(false);
          }
          button.setSelected(true);
          mSelectedButton = button;
          mCallback.onSegmentClicked(v.getId());
        }
      });
      mButtonContainer.addView(button);
      buttonId = buttonId + 1;
    }
  }

  @Override public void setPresenter(WaterColumnContract.Presenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Build a stateful drawable for a given EMU
   * @param emuName
   * @return StateListDrawable responsive to selected, pressed, and enabled states
   */
  private StateListDrawable buildStateList(int emuName){
    StateListDrawable stateListDrawable = new StateListDrawable();

    GradientDrawable defaultShape = new GradientDrawable();
    int color = Color.parseColor(EmuHelper.getColorForEMUCluster( emuName));
    defaultShape.setColor(color);

    GradientDrawable selectedPressShape = new GradientDrawable();
    selectedPressShape.setColor(color);
    selectedPressShape.setStroke(5,Color.parseColor("#f4f442"));

    stateListDrawable.addState(new int[] {android.R.attr.state_pressed}, selectedPressShape);
    stateListDrawable.addState(new int[] {android.R.attr.state_selected}, selectedPressShape);
    stateListDrawable.addState(new int[] {android.R.attr.state_enabled}, defaultShape);

    return stateListDrawable;
  }
  /**
   * Set selected state of a water column segment
   * @param position
   */
  public void highlightSegment(int position){
    Button button =(Button) mButtonContainer.getChildAt(position);
    if (mSelectedButton != null){
      mSelectedButton.setSelected(false);
    }
    button.setSelected(true);
    mSelectedButton = button;
  }


}
