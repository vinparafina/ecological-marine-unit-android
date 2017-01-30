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

/**
 * This fragment is responsible for building and displaying scatter plot charts for
 * physical properties.  Each scatter plot represents how a specific physical
 * property changes with ocean depth.
 * It's the View in the MVP pattern and the concrete implementation of the
 * WaterProfileContract.View interface.
 */

package com.esri.android.ecologicalmarineunitexplorer.waterprofile;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.esri.android.ecologicalmarineunitexplorer.R;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.utils.Utils;

import java.util.List;

public class WaterProfileFragment extends Fragment implements WaterProfileContract.View {

  private WaterProfileContract.Presenter mPresenter;
  private ProgressDialog mProgressDialog;

  private ViewPager mViewPager;

  public static WaterProfileFragment newInstance() {
    return new WaterProfileFragment();
  }

  /**
   * Initialize charts
   * @param savedInstance
   */
  @Override
  public final void onCreate(@NonNull final Bundle savedInstance){
    super.onCreate(savedInstance);
    // MP Android chart
    Utils.init(getContext());
  }

  /**
   * Inflate the view pager and start the presenter
   * @param layoutInflater LayoutInflater
   * @param container ViewGroup
   * @param savedInstance Bundle
   * @return View
   */
  @Override
  @Nullable
  public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    super.onCreateView(layoutInflater, container, savedInstance);
    mViewPager = (ViewPager) layoutInflater.inflate(R.layout.water_profile_view_pager,container,false) ;
    mPresenter.start();

    return  mViewPager;
  }

  /**
   * Set the data for the adapter
   * @param dataList - List<CombinedData> containing data points
   */
  @Override public void showWaterProfiles(final List<CombinedData> dataList ) {
    //
    // Because this FragmentPagerAdapter is being used within a nested
    // fragment, we don't want to use the shared fragment manager.
    // USE A NEW INSTANCE OF THE FRAGMENT MANAGER rather than
    // using the fragment manager belonging to the activity.
    //
    TabPagerAdapter mTabAdapter = new TabPagerAdapter(getChildFragmentManager(), dataList);
    mViewPager.setAdapter(mTabAdapter);
  }

  @Override public void showMessage(final String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  @Override public void showProgressBar(final String message, final String title) {
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

  @Override public void setPresenter(final WaterProfileContract.Presenter presenter) {
    mPresenter = presenter;
  }


}
