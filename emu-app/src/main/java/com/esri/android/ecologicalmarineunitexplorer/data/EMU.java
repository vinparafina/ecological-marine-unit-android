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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class EMU {
  @NonNull private Integer name;
  @NonNull private String physicalSummary;
  @NonNull private String nutrientSummary;
  @NonNull private String geomorphologyBase;
  @NonNull private String geomorphologyFeatures;
  @Nullable private double count;
  @Nullable private double percentWater;
  @Nullable private double temp_mean;
  @Nullable private double temp_min;
  @Nullable private double temp_max;
  @Nullable private double temp_std;
  @Nullable private double salinity_mean;
  @Nullable private double salinity_min;
  @Nullable private double salinity_max;
  @Nullable private double salinity_std;
  @Nullable private double disso2_mean;
  @Nullable private double disso2_min;
  @Nullable private double disso2_max;
  @Nullable private double disso2_std;
  @Nullable private double phosphate_mean;
  @Nullable private double phosphate_min;
  @Nullable private double phosphate_max;
  @Nullable private double phosphate_std;
  @Nullable private double nitrate_mean;
  @Nullable private double nitrate_min;
  @Nullable private double nitrate_max;
  @Nullable private double nitrate_std;
  @Nullable private double silicate_mean;
  @Nullable private double silicate_min;
  @Nullable private double silicate_max;
  @Nullable private double silicate_std;

  @NonNull public Integer getName() {
    return name;
  }

  public void setName(@NonNull Integer name) {
    this.name = name;
  }

  @NonNull public String getPhysicalSummary() {
    return physicalSummary;
  }

  public void setPhysicalSummary(@NonNull String physicalSummary) {
    this.physicalSummary = physicalSummary;
  }

  @NonNull public String getNutrientSummary() {
    return nutrientSummary;
  }

  public void setNutrientSummary(@NonNull String nutrientSummary) {
    this.nutrientSummary = nutrientSummary;
  }

  @NonNull public String getGeomorphologyBase() {
    return geomorphologyBase;
  }

  public void setGeomorphologyBase(@NonNull String geomorphologyBase) {
    this.geomorphologyBase = geomorphologyBase;
  }

  @NonNull public String getGeomorphologyFeatures() {
    return geomorphologyFeatures;
  }

  public void setGeomorphologyFeatures(@NonNull String geomorphologyFeatures) {
    this.geomorphologyFeatures = geomorphologyFeatures;
  }

  @Nullable public double getCount() {
    return count;
  }

  public void setCount(@Nullable double count) {
    this.count = count;
  }

  @Nullable public double getPercentWater() {
    return percentWater;
  }

  public void setPercentWater(@Nullable double percentWater) {
    this.percentWater = percentWater;
  }

  @Nullable public double getTemp_mean() {
    return temp_mean;
  }

  public void setTemp_mean(@Nullable double temp_mean) {
    this.temp_mean = temp_mean;
  }

  @Nullable public double getTemp_min() {
    return temp_min;
  }

  public void setTemp_min(@Nullable double temp_min) {
    this.temp_min = temp_min;
  }

  @Nullable public double getTemp_max() {
    return temp_max;
  }

  public void setTemp_max(@Nullable double temp_max) {
    this.temp_max = temp_max;
  }

  @Nullable public double getTemp_std() {
    return temp_std;
  }

  public void setTemp_std(@Nullable double temp_std) {
    this.temp_std = temp_std;
  }

  @Nullable public double getSalinity_mean() {
    return salinity_mean;
  }

  public void setSalinity_mean(@Nullable double salinity_mean) {
    this.salinity_mean = salinity_mean;
  }

  @Nullable public double getSalinity_min() {
    return salinity_min;
  }

  public void setSalinity_min(@Nullable double salinity_min) {
    this.salinity_min = salinity_min;
  }

  @Nullable public double getSalinity_max() {
    return salinity_max;
  }

  public void setSalinity_max(@Nullable double salinity_max) {
    this.salinity_max = salinity_max;
  }

  @Nullable public double getSalinity_std() {
    return salinity_std;
  }

  public void setSalinity_std(@Nullable double salinity_std) {
    this.salinity_std = salinity_std;
  }

  @Nullable public double getDisso2_mean() {
    return disso2_mean;
  }

  public void setDisso2_mean(@Nullable double disso2_mean) {
    this.disso2_mean = disso2_mean;
  }

  @Nullable public double getDisso2_min() {
    return disso2_min;
  }

  public void setDisso2_min(@Nullable double disso2_min) {
    this.disso2_min = disso2_min;
  }

  @Nullable public double getDisso2_max() {
    return disso2_max;
  }

  public void setDisso2_max(@Nullable double disso2_max) {
    this.disso2_max = disso2_max;
  }

  @Nullable public double getDisso2_std() {
    return disso2_std;
  }

  public void setDisso2_std(@Nullable double disso2_std) {
    this.disso2_std = disso2_std;
  }

  @Nullable public double getPhosphate_mean() {
    return phosphate_mean;
  }

  public void setPhosphate_mean(@Nullable double phosphate_mean) {
    this.phosphate_mean = phosphate_mean;
  }

  @Nullable public double getPhosphate_min() {
    return phosphate_min;
  }

  public void setPhosphate_min(@Nullable double phosphate_min) {
    this.phosphate_min = phosphate_min;
  }

  @Nullable public double getPhosphate_max() {
    return phosphate_max;
  }

  public void setPhosphate_max(@Nullable double phosphate_max) {
    this.phosphate_max = phosphate_max;
  }

  @Nullable public double getPhosphate_std() {
    return phosphate_std;
  }

  public void setPhosphate_std(@Nullable double phosphate_std) {
    this.phosphate_std = phosphate_std;
  }

  @Nullable public double getNitrate_mean() {
    return nitrate_mean;
  }

  public void setNitrate_mean(@Nullable double nitrate_mean) {
    this.nitrate_mean = nitrate_mean;
  }

  @Nullable public double getNitrate_min() {
    return nitrate_min;
  }

  public void setNitrate_min(@Nullable double nitrate_min) {
    this.nitrate_min = nitrate_min;
  }

  @Nullable public double getNitrate_max() {
    return nitrate_max;
  }

  public void setNitrate_max(@Nullable double nitrate_max) {
    this.nitrate_max = nitrate_max;
  }

  @Nullable public double getNitrate_std() {
    return nitrate_std;
  }

  public void setNitrate_std(@Nullable double nitrate_std) {
    this.nitrate_std = nitrate_std;
  }

  @Nullable public double getSilicate_mean() {
    return silicate_mean;
  }

  public void setSilicate_mean(@Nullable double silicate_mean) {
    this.silicate_mean = silicate_mean;
  }

  @Nullable public double getSilicate_min() {
    return silicate_min;
  }

  public void setSilicate_min(@Nullable double silicate_min) {
    this.silicate_min = silicate_min;
  }

  @Nullable public double getSilicate_max() {
    return silicate_max;
  }

  public void setSilicate_max(@Nullable double silicate_max) {
    this.silicate_max = silicate_max;
  }

  @Nullable public double getSilicate_std() {
    return silicate_std;
  }

  public void setSilicate_std(@Nullable double silicate_std) {
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
