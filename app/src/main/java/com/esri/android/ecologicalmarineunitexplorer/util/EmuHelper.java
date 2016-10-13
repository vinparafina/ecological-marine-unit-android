package com.esri.android.ecologicalmarineunitexplorer.util;
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

import android.content.Context;
import com.esri.android.ecologicalmarineunitexplorer.R;

public class EmuHelper {
  /**
   * Most EMU clusters have a color associated with them.
   * For those that don't return a bright neon green.
   * @param emuName - an int representing the EMU name
   * @return colorCode - a string representing a hex color code.
   */
  public static String getColorForEMUCluster(Context context, int emuName){
    String colorCode = null;
    switch (emuName){
      case 3:
        colorCode = context.getString(R.string.Cluster3);
        break;
      case 5:
        colorCode = context.getString(R.string.Cluster5);
        break;
      case 8:
        colorCode = context.getString(R.string.Cluster8);
        break;
      case 9:
        colorCode = context.getString(R.string.Cluster9);
        break;
      case 10:
        colorCode = context.getString(R.string.Cluster10);
        break;
      case 11:
        colorCode = context.getString(R.string.Cluster11);
        break;
      case 13:
        colorCode = context.getString(R.string.Cluster13);
        break;
      case 14:
        colorCode = context.getString(R.string.Cluster14);
        break;
      case 18:
        colorCode = context.getString(R.string.Cluster18);
        break;
      case 19:
        colorCode = context.getString(R.string.Cluster19);
        break;
      case 21:
        colorCode = context.getString(R.string.Cluster21);
        break;
      case 23:
        colorCode = context.getString(R.string.Cluster23);
        break;
      case 24:
        colorCode = context.getString(R.string.Cluster24);
        break;
      case 25:
        colorCode = context.getString(R.string.Cluster25);
        break;
      case 26:
        colorCode = context.getString(R.string.Cluster26);
        break;
      case 29:
        colorCode = context.getString(R.string.Cluster29);
        break;
      case 30:
        colorCode = context.getString(R.string.Cluster30);
        break;
      case 31:
        colorCode = context.getString(R.string.Cluster31);
        break;
      case 33:
        colorCode = context.getString(R.string.Cluster33);
        break;
      case 35:
        colorCode = context.getString(R.string.Cluster35);
        break;
      case 36:
        colorCode = context.getString(R.string.Cluster36);
        break;
      case 37:
        colorCode = context.getString(R.string.Cluster37);
        break;
      default:
        colorCode = "#b6f442";
    }
    return colorCode;
  }
}
