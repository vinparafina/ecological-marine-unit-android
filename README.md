# Ecological Marine Unit Explorer
Explore our ocean ecosystems with EMUs!

## Description
Using 50 year's worth of aggregated nutrient and physical ocean data from [NOAA](https://www.nodc.noaa.gov/OC5/woa13/), Esri has collaborated with the USGS, the Marine Conservation Institute, NatureServe, the University of Auckland, GRID-Arendal, NOAA, Duke University, the Woods Hole Oceanographic Institution, and many other partners to classify our oceans into 37 statistically distinct [ecological marine units](http://www.esri.com/ecological-marine-units) (EMUs).  Leveraging the Runtime SDK and ArcGIS, we'll show you how this Android mobile app can be used to explore ocean conditions locally and globally. (TODO: Mention how you can add your own data to enhance this rich data set for your own organization!)

## Feature Services
The heart of this application lies in the rich data stores collected by NOAA and analyzed by the scientific collaboration mentioned above.  Over 50 million spatial and non-spatial datapoints are hosted through AGOL [feature services](http://server.arcgis.com/en/server/10.5/publish-services/windows/what-is-a-feature-service-.htm).  

Why these types of layers?  Show how they're set up.

## Querying Feature Tables
Querying by geometry
Querying non-spatial data

## Data Prep
For traversing depth levels, what considerations were made?  What iterations were done?
What are the steps we went through to prepare data/services?
Clustering points --> Dissolving --> Buffering --> Symbology

## Licensing
Copyright 2016 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/maps-app-android/blob/master/license.txt) file.

For information about licensing your deployed app, see [License your app](https://developers.arcgis.com/android/guide/license-your-app.htm).

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​

The Model View Presenter architecture is used in this application.  The descriptions below were taken from this [site](http://www.tinmegali.com/en/model-view-presenter-android-part-1/).
Presenter

The Presenter is responsible to act as the middle man between View and Model. It retrieves data from the Model and returns it formatted to the View. But unlike the typical MVC, it also decides what happens when you interact with the View.

View

The View, usually implemented by an Activity, will contain a reference to the presenter. The only thing that the view will do is to call a method from the Presenter every time there is an interface action.

Model

In an application with a good layered architecture, this model would only be the gateway to the domain layer or business logic. See it as the provider of the data we want to display in the view.
