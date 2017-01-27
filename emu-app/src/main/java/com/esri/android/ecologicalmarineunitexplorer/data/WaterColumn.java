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
import com.esri.arcgisruntime.geometry.Point;

import java.util.*;

/**
 * A Model object encapsulating a collection of EMUObservations for a specific location
 */

public class WaterColumn implements  Comparable<WaterColumn> {
  @NonNull private final Set<EMUObservation> emuSet;
  private double distanceFrom = 0;
  private Point location;

  public WaterColumn(){
    emuSet = new TreeSet<>();
  }

  @NonNull public Set<EMUObservation> getEmuSet() {
    return emuSet;
  }

  public void addObservation(final EMUObservation observation){
    emuSet.add(observation);
  }


  public int getDepth(){
    int depth = 0;
    final Iterator<EMUObservation> iter = emuSet.iterator();
    while (iter.hasNext()){
      final EMUObservation observation = iter.next();
      depth = depth + observation.getThickness();
    }
    return depth;
  }

  @Override public String toString() {
    return "WaterColumn{" +
        "emuSet=" + emuSet +
        '}';
  }

  @Override public int compareTo(@NonNull final WaterColumn another) {
    if (distanceFrom < another.distanceFrom){
      return  -1;
    }
    else if (distanceFrom > another.distanceFrom){
      return 1;
    }
    else{
      return  0;
    }
  }

  public void setDistanceFrom(final double distanceFrom) {
    this.distanceFrom = distanceFrom;
  }

  public Point getLocation() {
    return location;
  }

  public void setLocation(final Point location) {
    this.location = location;
  }

  /**
   * Return a list of EMU Observation items with a given emu name
   * @param emuName - int representing emu name
   * @return - a list containing zero or more EMU Observation items
   */
  public List<EMUObservation> getEMUObservations(final int emuName){
    final List<EMUObservation> emuObservations = new ArrayList<>();
    final Iterator<EMUObservation> iter = emuSet.iterator();
    while (iter.hasNext()){
      final EMUObservation observation = iter.next();
      if (observation.getEmu().getName() == emuName){
        emuObservations.add(observation);
      }
    }
    return  emuObservations;

  }
}
