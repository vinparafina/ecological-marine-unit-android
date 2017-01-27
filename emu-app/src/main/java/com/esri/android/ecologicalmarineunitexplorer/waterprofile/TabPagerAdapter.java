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

package com.esri.android.ecologicalmarineunitexplorer.waterprofile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.github.mikephil.charting.data.CombinedData;

import java.util.ArrayList;
import java.util.List;
/**
 * The view holder for managing tabs of scatter line charts
 */
public class TabPagerAdapter extends FragmentPagerAdapter {

  private List<CombinedData> mChartDataList = new ArrayList<>();

  @Override public Fragment getItem(final int position) {
    switch (position){

      case  0:
        return ChartFragment.newInstance( mChartDataList.get(position));

      case 1:
        return ChartFragment.newInstance(mChartDataList.get(position));

      case 2:
        return ChartFragment.newInstance(mChartDataList.get(position));

      case 3:
        return ChartFragment.newInstance( mChartDataList.get(position));

      case 4:
        return ChartFragment.newInstance(mChartDataList.get(position));

      case 5:
        return ChartFragment.newInstance(mChartDataList.get(position));

      default:
        return new Fragment();
    }

  }

  public TabPagerAdapter(final FragmentManager fm, final List<CombinedData> data) {
    super(fm);
    mChartDataList = data;
  }
  @Override
  public int getCount() {
    return 6;
  }

  @Override
  public CharSequence getPageTitle(final int position) {
    super.getPageTitle(position);
    final CharSequence title;
    switch (position){
      case  0:
        title = "Temperature";
        break;
      case 1:
        title = "Salinity";
        break;
      case 2:
        title = "Oxygen";
        break;
      case 3:
        title = "Phosphate";
        break;
      case 4:
        title = "Silicate";
        break;
      case 5:
        title = "Nitrate";
        break;
      default:
        title = "Temperature";
    }
    return title;
  }
}
