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

public class EmuHelper {
  /**
   * Most EMU clusters have a color associated with them.
   * For those that don't return a bright neon green.
   * @param emuName - an int representing the EMU name
   * @return colorCode - a string representing a hex color code.
   */
  public static String getColorForEMUCluster( int emuName){
    String colorCode = null;
    switch (emuName){
      case 3:
        colorCode = "#708cd9";
        break;
      case 5:
        colorCode = "#a0d7d1";
        break;
      case 8:
        colorCode = "#ccb0ba";
        break;
      case 9:
        colorCode = "#c5b6d0";
        break;
      case 10:
        colorCode = "#7570e6";
        break;
      case 11:
        colorCode = "#cabfd9";
        break;
      case 13:
        colorCode = "#364799";
        break;
      case 14:
        colorCode = "#465290";
        break;
      case 18:
        colorCode = "#eb96cc";
        break;
      case 19:
        colorCode = "#b9caf6";
        break;
      case 21:
        colorCode = "#ebbccd";
        break;
      case 23:
        colorCode = "#a0d7d1";
        break;
      case 24:
        colorCode = "#eba9d4";
        break;
      case 25:
        colorCode = "#a0d7d1";
        break;
      case 26:
        colorCode = "#9365e6";
        break;
      case 29:
        colorCode = "#4792c9";
        break;
      case 30:
        colorCode = "#b4d7e7";
        break;
      case 31:
        colorCode = "#9ad4e6";
        break;
      case 33:
        colorCode = "#7591ff";
        break;
      case 35:
        colorCode = "#9edbff";
        break;
      case 36:
        colorCode = "#1a52aa";
        break;
      case 37:
        colorCode = "#4792c9";
        break;
      default:
        colorCode ="#b6f442";
    }
    return colorCode;
  }
}
