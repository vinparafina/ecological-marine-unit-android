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
import android.support.annotation.Nullable;
import com.esri.arcgisruntime.geometry.Point;

/**
 * A Model encapsulating a set of physical properties
 * for a specific occurrence of an EMU at a location.
 */
public class EMUObservation implements Comparable<EMUObservation> {
  @NonNull private EMU emu;
  private int top;
  private int thickness;
  @NonNull private Point location;
  @Nullable private Double salinity;
  @Nullable private Double temperature;
  @Nullable private Double oxygen;
  @Nullable private Double phosphate;
  @Nullable private Double silicate;
  @Nullable private Double nitrate;

  @Nullable public Double getOxygen() {
    return oxygen;
  }

  public void setOxygen(@Nullable final Double oxygen) {
    this.oxygen = oxygen;
  }



  @Override public int compareTo(final EMUObservation another) {
    if (top > another.top){
      return -1;
    }
    return this.getTop() < another.getTop() ? 1 : 0;

  }

  @Override public String toString() {
    return "EMUObservation{" +
        "emu=" + emu +
        ", top=" + top +
        ", thickness=" + thickness +
        ", location=" + location +
        ", salinity=" + salinity +
        ", temperature=" + temperature +
        ", oxygen=" + oxygen +
        ", phosphate=" + phosphate +
        ", silicate=" + silicate +
        ", nitrate=" + nitrate +
        '}';
  }

  @NonNull public EMU getEmu() {
    return emu;
  }

  public void setEmu(@NonNull final EMU emu) {
    this.emu = emu;
  }

  public int getTop() {
    return top;
  }

  public void setTop(final int top) {
    this.top = top;
  }

  public int getThickness() {
    return thickness;
  }

  public void setThickness(final int thickness) {
    this.thickness = thickness;
  }

  @NonNull public Point getLocation() {
    return location;
  }

  public void setLocation(final Point point) {
    this.location = point;
  }

  @Nullable public Double getSalinity() {
    return salinity;
  }

  public void setSalinity(@Nullable final Double salinity) {
    this.salinity = salinity;
  }

  @Nullable public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(@Nullable final Double temperature) {
    this.temperature = temperature;
  }

  @Nullable public Double getPhosphate() {
    return phosphate;
  }

  public void setPhosphate(@Nullable final Double phosphate) {
    this.phosphate = phosphate;
  }

  @Nullable public Double getSilicate() {
    return silicate;
  }

  public void setSilicate(@Nullable final Double silicate) {
    this.silicate = silicate;
  }

  @Nullable public Double getNitrate() {
    return nitrate;
  }

  public void setNitrate(@Nullable final Double nitrate) {
    this.nitrate = nitrate;
  }
}
