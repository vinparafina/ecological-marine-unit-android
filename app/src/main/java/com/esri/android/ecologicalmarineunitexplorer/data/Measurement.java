package com.esri.android.ecologicalmarineunitexplorer.data;
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

/**
 * An object respresenting several physical
 * properties and their measured values
 */
public class Measurement implements  Comparable<Measurement>{
  private Double depth;
  private Double temperature;
  private Double salinity;
  private Double dissolvedOxygen;
  private Double silicate;
  private Double phosphate;
  private Double nitrate;
  private int emu;

  public Double getDepth() {
    return depth;
  }

  public void setDepth(Double depth) {
    this.depth = depth;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Double getSalinity() {
    return salinity;
  }

  public void setSalinity(Double salinity) {
    this.salinity = salinity;
  }

  public Double getDissolvedOxygen() {
    return dissolvedOxygen;
  }

  public void setDissolvedOxygen(Double dissolvedOxygen) {
    this.dissolvedOxygen = dissolvedOxygen;
  }

  public Double getSilicate() {
    return silicate;
  }

  public void setSilicate(Double silicate) {
    this.silicate = silicate;
  }

  public Double getPhosphate() {
    return phosphate;
  }

  public void setPhosphate(Double phosphate) {
    this.phosphate = phosphate;
  }

  public Double getNitrate() {
    return nitrate;
  }

  public void setNitrate(Double nitrate) {
    this.nitrate = nitrate;
  }

  public void setEmu(int emu){
    this.emu = emu;
  }

  public int getEmu(){
    return emu;
  }

  public Double getValueForProperty(String property){
    Double value = null;
    switch (property){
      case "NITRATE":
        value = getNitrate();
        break;
      case "PHOSPHATE":
        value = getPhosphate();
        break;
      case "DISSOLVED_OXYGEN":
        value = getDissolvedOxygen();
        break;
      case "SILICATE":
        value = getSilicate();
        break;
      case "SALINITY":
        value = getSalinity();
        break;
      case "TEMPERATURE":
        value = getTemperature();
        break;
      default:
        value = null;

    }
    return value;
  }

  // Order by depth
  @Override public int compareTo(Measurement another) {
    if (this.getDepth() > another.getDepth()){
      return -1;
    }
    if (this.getDepth() < another.getDepth()){
      return 1;
    }else{
      return 0;
    }
  }
}
