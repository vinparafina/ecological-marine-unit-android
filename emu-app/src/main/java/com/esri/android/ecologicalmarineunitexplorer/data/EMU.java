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

public class EMU {
  @NonNull private Integer name;
  @NonNull private String physicalSummary;
  @NonNull private String nutrientSummary;
  @NonNull private String geomorphologyBase;
  @NonNull private String geomorphologyFeatures;
  private double count = 0.0;
  private double percentWater = 0.0;
  private double temp_mean = 0.0;
  private double temp_min = 0.0;
  private double temp_max = 0.0;
  private double temp_std = 0.0;
  private double salinity_mean = 0.0;
  private double salinity_min = 0.0;
  private double salinity_max = 0.0;
  private double salinity_std = 0.0;
  private double disso2_mean = 0.0;
  private double disso2_min = 0.0;
  private double disso2_max = 0.0;
  private double disso2_std = 0.0;
  private double phosphate_mean = 0.0;
  private double phosphate_min = 0.0;
  private double phosphate_max = 0.0;
  private double phosphate_std = 0.0;
  private double nitrate_mean = 0.0;
  private double nitrate_min = 0.0;
  private double nitrate_max = 0.0;
  private double nitrate_std = 0.0;
  private double silicate_mean = 0.0;
  private double silicate_min = 0.0;
  private double silicate_max = 0.0;
  private double silicate_std = 0.0;

  @NonNull public Integer getName() {
    return name;
  }

  public void setName(@NonNull final Integer name) {
    this.name = name;
  }

  @NonNull public String getPhysicalSummary() {
    return physicalSummary;
  }

  public void setPhysicalSummary(@NonNull final String physicalSummary) {
    this.physicalSummary = physicalSummary;
  }

  @NonNull public String getNutrientSummary() {
    return nutrientSummary;
  }

  public void setNutrientSummary(@NonNull final String nutrientSummary) {
    this.nutrientSummary = nutrientSummary;
  }

  @NonNull public String getGeomorphologyBase() {
    return geomorphologyBase;
  }

  public void setGeomorphologyBase(@NonNull final String geomorphologyBase) {
    this.geomorphologyBase = geomorphologyBase;
  }

  @NonNull public String getGeomorphologyFeatures() {
    return geomorphologyFeatures;
  }

  public void setGeomorphologyFeatures(@NonNull final String geomorphologyFeatures) {
    this.geomorphologyFeatures = geomorphologyFeatures;
  }

  public double getCount() {
    return count;
  }

  public void setCount(final double count) {
    this.count = count;
  }

  public double getPercentWater() {
    return percentWater;
  }

  public void setPercentWater(final double percentWater) {
    this.percentWater = percentWater;
  }

  public double getTemp_mean() {
    return temp_mean;
  }

  public void setTemp_mean(final double temp_mean) {
    this.temp_mean = temp_mean;
  }

  public double getTemp_min() {
    return temp_min;
  }

  public void setTemp_min(final double temp_min) {
    this.temp_min = temp_min;
  }

  public double getTemp_max() {
    return temp_max;
  }

  public void setTemp_max(final double temp_max) {
    this.temp_max = temp_max;
  }

  public double getTemp_std() {
    return temp_std;
  }

  public void setTemp_std(final double temp_std) {
    this.temp_std = temp_std;
  }

  public double getSalinity_mean() {
    return salinity_mean;
  }

  public void setSalinity_mean(final double salinity_mean) {
    this.salinity_mean = salinity_mean;
  }

  public double getSalinity_min() {
    return salinity_min;
  }

  public void setSalinity_min(final double salinity_min) {
    this.salinity_min = salinity_min;
  }

  public double getSalinity_max() {
    return salinity_max;
  }

  public void setSalinity_max(final double salinity_max) {
    this.salinity_max = salinity_max;
  }

  public double getSalinity_std() {
    return salinity_std;
  }

  public void setSalinity_std(final double salinity_std) {
    this.salinity_std = salinity_std;
  }

  public double getDisso2_mean() {
    return disso2_mean;
  }

  public void setDisso2_mean(final double disso2_mean) {
    this.disso2_mean = disso2_mean;
  }

  public double getDisso2_min() {
    return disso2_min;
  }

  public void setDisso2_min(final double disso2_min) {
    this.disso2_min = disso2_min;
  }

  public double getDisso2_max() {
    return disso2_max;
  }

  public void setDisso2_max(final double disso2_max) {
    this.disso2_max = disso2_max;
  }

  public double getDisso2_std() {
    return disso2_std;
  }

  public void setDisso2_std(final double disso2_std) {
    this.disso2_std = disso2_std;
  }

  public double getPhosphate_mean() {
    return phosphate_mean;
  }

  public void setPhosphate_mean(final double phosphate_mean) {
    this.phosphate_mean = phosphate_mean;
  }

  public double getPhosphate_min() {
    return phosphate_min;
  }

  public void setPhosphate_min(final double phosphate_min) {
    this.phosphate_min = phosphate_min;
  }

  public double getPhosphate_max() {
    return phosphate_max;
  }

  public void setPhosphate_max(final double phosphate_max) {
    this.phosphate_max = phosphate_max;
  }

  public double getPhosphate_std() {
    return phosphate_std;
  }

  public void setPhosphate_std(final double phosphate_std) {
    this.phosphate_std = phosphate_std;
  }

  public double getNitrate_mean() {
    return nitrate_mean;
  }

  public void setNitrate_mean(final double nitrate_mean) {
    this.nitrate_mean = nitrate_mean;
  }

  public double getNitrate_min() {
    return nitrate_min;
  }

  public void setNitrate_min(final double nitrate_min) {
    this.nitrate_min = nitrate_min;
  }

  public double getNitrate_max() {
    return nitrate_max;
  }

  public void setNitrate_max(final double nitrate_max) {
    this.nitrate_max = nitrate_max;
  }

  public double getNitrate_std() {
    return nitrate_std;
  }

  public void setNitrate_std(final double nitrate_std) {
    this.nitrate_std = nitrate_std;
  }

  public double getSilicate_mean() {
    return silicate_mean;
  }

  public void setSilicate_mean(final double silicate_mean) {
    this.silicate_mean = silicate_mean;
  }

  public double getSilicate_min() {
    return silicate_min;
  }

  public void setSilicate_min(final double silicate_min) {
    this.silicate_min = silicate_min;
  }

  public double getSilicate_max() {
    return silicate_max;
  }

  public void setSilicate_max(final double silicate_max) {
    this.silicate_max = silicate_max;
  }

  public double getSilicate_std() {
    return silicate_std;
  }

  public void setSilicate_std(final double silicate_std) {
    this.silicate_std = silicate_std;
  }

  @Override public String toString() {
    return "EMU{" +
        "name=" + name +
        ", physicalSummary='" + physicalSummary + '\'' +
        ", nutrientSummary='" + nutrientSummary + '\'' +
        ", geomorphologyBase='" + geomorphologyBase + '\'' +
        ", geomorphologyFeatures='" + geomorphologyFeatures + '\'' +
        ", count=" + count +
        ", percentWater=" + percentWater +
        ", temp_mean=" + temp_mean +
        ", temp_min=" + temp_min +
        ", temp_max=" + temp_max +
        ", temp_std=" + temp_std +
        ", salinity_mean=" + salinity_mean +
        ", salinity_min=" + salinity_min +
        ", salinity_max=" + salinity_max +
        ", salinity_std=" + salinity_std +
        ", disso2_mean=" + disso2_mean +
        ", disso2_min=" + disso2_min +
        ", disso2_max=" + disso2_max +
        ", disso2_std=" + disso2_std +
        ", phosphate_mean=" + phosphate_mean +
        ", phosphate_min=" + phosphate_min +
        ", phosphate_max=" + phosphate_max +
        ", phosphate_std=" + phosphate_std +
        ", nitrate_mean=" + nitrate_mean +
        ", nitrate_min=" + nitrate_min +
        ", nitrate_max=" + nitrate_max +
        ", nitrate_std=" + nitrate_std +
        ", silicate_mean=" + silicate_mean +
        ", silicate_min=" + silicate_min +
        ", silicate_max=" + silicate_max +
        ", silicate_std=" + silicate_std +
        '}';
  }
}
