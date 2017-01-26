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

package com.esri.android.ecologicalmarineunitexplorer.data;


import android.support.annotation.NonNull;

/**
 * An object respresenting several physical
 * properties and their measured values
 */
public class Measurement implements  Comparable<Measurement>{
  private Double depth = null;
  private Double temperature = null;
  private Double salinity = null;
  private Double dissolvedOxygen = null;
  private Double silicate = null;
  private Double phosphate = null;
  private Double nitrate = null;
  private int emu = 0;

  public Double getDepth() {
    return depth;
  }

  public void setDepth(final Double depth) {
    this.depth = depth;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(final Double temperature) {
    this.temperature = temperature;
  }

  public Double getSalinity() {
    return salinity;
  }

  public void setSalinity(final Double salinity) {
    this.salinity = salinity;
  }

  public Double getDissolvedOxygen() {
    return dissolvedOxygen;
  }

  public void setDissolvedOxygen(final Double dissolvedOxygen) {
    this.dissolvedOxygen = dissolvedOxygen;
  }

  public Double getSilicate() {
    return silicate;
  }

  public void setSilicate(final Double silicate) {
    this.silicate = silicate;
  }

  public Double getPhosphate() {
    return phosphate;
  }

  public void setPhosphate(final Double phosphate) {
    this.phosphate = phosphate;
  }

  public Double getNitrate() {
    return nitrate;
  }

  public void setNitrate(final Double nitrate) {
    this.nitrate = nitrate;
  }

  public void setEmu(final int emu){
    this.emu = emu;
  }

  public int getEmu(){
    return emu;
  }

  public Double getValueForProperty(final String property){
    Double value;
    switch (property){
      case "NITRATE":
        value = nitrate;
        break;
      case "PHOSPHATE":
        value = phosphate;
        break;
      case "DISSOLVED_OXYGEN":
        value = dissolvedOxygen;
        break;
      case "SILICATE":
        value = silicate;
        break;
      case "SALINITY":
        value = salinity;
        break;
      case "TEMPERATURE":
        value = temperature;
        break;
      default:
        value = null;

    }
    return value;
  }

  // Order by depth
  @Override public int compareTo(@NonNull final Measurement another) {
    if (depth > another.depth){
      return -1;
    }
    return getDepth() < another.getDepth() ? 1 : 0;
  }
}
