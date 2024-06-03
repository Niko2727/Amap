# Map

## 1. Summary

A simple map demo, implemented with the help of Amap sdk. There are currently two pages, positioning and navigation.

On the location page, you can view your current location on the map and switch between 2D and 3D views. When you select a location on the map, three buttons appear at the bottom of the page for walking, driving, and biking. By clicking on them, you can choose how to navigate to the location of your choice.

<img src="https://github.com/Niko2727/Amap/assets/52966802/ccdfb723-aeea-4ba1-9bce-181f560ba7c0" width = "250" height = "500" alt="location page" align=center />

On the navigation page, you can follow the route on the map to reach your destination.

<img src="https://github.com/Niko2727/Amap/assets/52966802/31997c65-7786-43d5-ba0a-9edf6427b435" width = "250" height = "500" alt="location page" align=center />

## 2. Amap SDK for Android

If you want to know the complete content of Amap sdk, you can check the [official website](https://lbs.amap.com/api/android-sdk/summary).

### 2.1 Get Amap key
Click the [link](https://lbs.amap.com/dev/#/) to apply for Amap key

1. Click [My Apps] on the left side of the page

<img width="229" alt="My apps" src="https://github.com/JunfengGuo/Map/assets/52966802/a1084d19-9ee6-428a-bef3-a5a981ba0210">

2. Click [Create new App] on the right side of the page

<img width="267" alt="Create new App" src="https://github.com/JunfengGuo/Map/assets/52966802/8bc361c3-9daf-4941-8711-8bad378962dc">

3. After creating the application, click Add key and fill in the application information.

<img width="706" alt="infomation" src="https://github.com/JunfengGuo/Map/assets/52966802/42d5458e-573a-406f-bba9-4da8640e7fd1">


### 2.2 Configure dependencies in the build.gradle file of the main project
```gradle

implementation("com.amap.api:3dmap:latest.integration")

```

### 2.3 Add Amap key

In order to ensure the normal use of the functions of AutoNavi Android SDK, you need to apply for AutoNavi Key and configure it into the project.

In the project's "AndroidManifest.xml" file, add the following code:

``` xml
<application
         android:icon="@drawable/icon"
         android:label="@string/app_name" >
         <meta-data
            android:name="com.amap.api.v2.apikey"
            android:value="input your user Key"/>
            ……
</application>

```

### 2.4 Configure permissions
Configure permissions in AndroidManifest.xml:
``` xml
    <!--Allow access to the network, required permissions-->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!--llows obtaining rough location, required if GPS is used to locate the small blue dot function-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!--Used to access GPS positioning-->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
    <!--Allows access to network status for network positioning. If there is no GPS but the function of positioning the small blue dot still needs to be achieved, this permission is required.-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!--Allows access to wifi network information for network positioning. If there is no GPS but still needs to locate the small blue dot, this permission is required.-->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <!--Allows access to wifi status changes for network positioning. If there is no GPS but still needs to locate the small blue dot, this permission is required.-->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    
    <!--Allow writing to extended storage, used for data caching. If you do not have this permission, write to a private directory.-->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    <!--Allow writing to device cache for troubleshooting purposes-->
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    
    <!--Allows reading device and other information for troubleshooting-->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <!--If target >= 28 is set, this permission must be declared if background positioning needs to be started.-->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
    <!--If your app requires background positioning permissions, may run on Android Q devices, and is set to target>28, you must add this permission statement.-->
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

```

### 2.5 Privacy compliance interface

Compliance checks must be performed before constructing MapView. Ensure privacy policy compliance before setting the interface. The check interface is as follows:
```kotlin
MapsInitializer.updatePrivacyShow(context,true,true);
MapsInitializer.updatePrivacyAgree(context,true);
```

## 3. Positioning page 

### 3.1 Initialize map container
MapView is a subclass of the AndroidView class and is used to place maps in Android Views. MapView is a map container. The method of loading a map using MapView is the same as other Views provided by Android. The specific usage steps are as follows:

First add the map control in the layout xml file:
```xml
<com.amap.api.maps.MapView
    android:id="@+id/map"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
When using maps in projects, you need to pay attention to the need to reasonably manage the map life cycle, which is very important.

The following example briefly outlines map lifecycle management:
``` java

public class MainActivity extends Activity {
  MapView mMapView = null;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState); 
    setContentView(R.layout.activity_main);
    mMapView = (MapView) findViewById(R.id.map);
    mMapView.onCreate(savedInstanceState);
  }
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.onDestroy();
  }
 @Override
 protected void onResume() {
    super.onResume();
    mMapView.onResume();
    }
 @Override
 protected void onPause() {
    super.onPause();
    mMapView.onPause();
    }
 @Override
 protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mMapView.onSaveInstanceState(outState);
  } 
}

```

### 3.2 Show map
The AMap class is the map's controller class and is used to operate the map. The tasks it carries include: switching map layers (such as satellite images, night maps), changing map status (map rotation angle, pitch angle, center point coordinates and zoom level), adding point markers (Marker), drawing geometric figures ( Polyline, Polygon, Circle), various event monitoring (clicks, gestures, etc.), AMap is the most important core class of the map SDK, and many operations rely on it to complete.

After the MapView object is initialized, the AMap object is constructed. The sample code is as follows:
```java

mapView = (MapView) findViewById(R.id.map);
mapView.onCreate(savedInstanceState);
AMap aMap;
if (aMap == null) {
    aMap = mapView.getMap();        
}

```

### 3.3 Show positioning blue dot


```kotlin
val locationStyle = MyLocationStyle() //Initialize positioning blue dot style class
locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) // Positioning blue dot display mode

with(map) {
    myLocationStyle = locationStyle   //Set the Style for positioning the blue dot
    isMyLocationEnabled = true // Set to true to start displaying the positioning blue dot, false to hide the positioning blue dot and not perform positioning. The default is false.

    setOnMapLoadedListener {
        showMapText(true)
        showIndoorMap(true)
        showBuildings(true)
    }

    moveCamera(CameraUpdateFactory.zoomTo(defaultZoomLevel))
  }

with(map.uiSettings) {
    isMyLocationButtonEnabled = false // Sets whether to display the default positioning button. This setting is not required.
    isZoomControlsEnabled = true 
}
```

### 3.4 Position button

By clicking the positioning button, you can switch to positioning and following modes.

```kotlin

locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) // positioning mode
locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)      // following mode

map.myLocationStyle = locationStyle

map.moveCamera(newCameraPosition(CameraPosition(getUserLatlng(), zoomLevel, tilt, 0f))) // change zoom level and tilt

```

### 3.5 Projection button
By clicking the positioning button, you can switch to 2D and 3D modes.
```kotlin
private val tilt2D = 0f
private val tilt3D = 45f

val degree = if (it == MainViewModel.ProjectionStyle.ThreeD) tilt3D else tilt2D

map.animateCamera(CameraUpdateFactory.changeTilt(degree))

```

### 3.6 Select destination

The clickable locations on the map exist in the form of POIs in the code. The code for selecting the destination is as follows:
```kotlin

map.addOnPOIClickListener {onClickPOI(it)}


private fun onClickPOI(poi: Poi) {
        markerOption.position(poi.coordinate)

        // display marker on selected position
        map.addMarker(markerOption).apply {
            setAnimation(markerAnimation)
            startAnimation()
        }
    }

```

## 4. Navigation Page

### 4.1 Start navigation page
Starting the navigation page requires passing in the starting and ending point location information, navigation type, and whether to use real GPS signals.

```kotlin
fun start(context: Context, start: NaviLatLng, end: NaviLatLng, naviType: AmapNaviType, isGps: Boolean) {
            val intent = Intent(context, NaviActivity::class.java)
            intent.putExtra(EXTRA_START, start)
            intent.putExtra(EXTRA_END, end)
            intent.putExtra(EXTRA_NAVI_TYPE, naviType.name)
            intent.putExtra(EXTRA_IS_GPS, isGps)
            context.startActivity(intent)
        }
```

### 4.2 Initialize map container
Define the layout, navigation needs to use AmapNaviViw, not the MapView used for positioning
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.amap.navifragement.NaviFragment">

    <com.amap.api.navi.AMapNaviView
        android:id="@+id/navi_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```
Implement the life cycle of AMapNaviView.
```Java

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
     mAMapNaviView.onCreate(savedInstanceState);
    mAMapNaviView.setAMapNaviViewListener(this);
}

@Override
public void onResume() {
    super.onResume();
    mAMapNaviView.onResume();
}

@Override
public void onPause() {
    super.onPause();
    mAMapNaviView.onPause();
}

@Override
public void onDestroy() {
    super.onDestroy();
    mAMapNaviView.onDestroy();
}

```

### 4.3 Configure AMapNavi
AMapNavi is a navigation control unit that can calculate the path between two points and provide a callback when reaching the destination.
```kotlin
mapNavi = AMapNavi.getInstance(applicationContext)
mapNavi.addAMapNaviListener(naviListener)
mapNavi.setEmulatorNaviSpeed(30)
mapNavi.setUseInnerVoice(true, true)
```

### 4.4 Calculate Route
AMapNavi provides an interface for us to calculate walking, cycling and driving routes. However, motorcycles and eleBike are currently charged. If the fee is not paid, no result will be returned if called.

```kotlin
private fun calculateRoute(start: NaviLatLng, end: NaviLatLng, naviType: AmapNaviType) {

        when (naviType) {
            AmapNaviType.WALK -> {
                mapNavi.calculateWalkRoute(start, end)
            }

            AmapNaviType.RIDE -> {
                mapNavi.calculateRideRoute(start, end)
            }

            AmapNaviType.MOTORCYCLE -> {
                mapNavi.calculateEleBikeRoute(start, end)
            }

            else -> {
                calculateDriveRoute(start, end)
            }
        }
    }

```


### 4.5 Start navigation
When the path calculation is successful, draw the path and start navigation

1. draw path
``` kotlin
private val naviListener = object : DefaultAmapNaviListener() {

        override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
            super.onCalculateRouteSuccess(p0)

            // // When re-planning the route, remove the previous route
            routeOverLay?.removeFromMap()
            routeOverLay?.destroy()

            // Get the array routIDs of the returned route aMapCalcRouteResult will return one or more routes.
            // ps: Multiple routes are used to select multiple routes, but here we only do simple navigation. So we just draw one.
            val routIds = p0!!.routeid
            val routeId = routIds[0]
            

            val path = mapNavi.naviPaths[routeId]
            routeOverLay = RouteOverLay(map, path, this@NaviActivity)
            routeOverLay?.addToMap()

            this@NaviActivity.onCalculateRouteSuccess()
        }

        override fun onLocationChange(p0: AMapNaviLocation?) {
            super.onLocationChange(p0)
            //Draw the gray route traveled
            routeOverLay?.updatePolyline(p0)
        }
}
```

2. start navigation
``` kotlin
fun onCalculateRouteSuccess() {
        if (isGps) {
            mapNavi.startNavi(NaviType.GPS)
        } else {
            mapNavi.startNavi(NaviType.EMULATOR)
        }
    }
```

### 4.6 Reach destination
Display distance and time when arriving at destination
```kotlin
private val naviListener = object : DefaultAmapNaviListener() {
  override fun onArriveDestination() {
            super.onArriveDestination()
            Log.d(TAG, "onArriveDestination() called")
            vm.onNaviEnd(this@NaviActivity)
        }

        override fun onEndEmulatorNavi() {
            super.onEndEmulatorNavi()
            vm.onNaviEnd(this@NaviActivity)
        }
}

fun onNaviEnd(context: Context) {
        val realDuration = getTimeDisplayString(((System.currentTimeMillis() - naviStartTime) / 1000).toInt(), context)
        val duration = getTimeDisplayString(naviPath!!.allTime, context)
        val distanceDisplay = getDistanceDisplayString(naviPath!!.allLength, context)


        val stringBuilder = StringBuilder()
        stringBuilder.append(context.getString(R.string.distance)).append(":").append(distanceDisplay).append('\n')
        stringBuilder.append(context.getString(R.string.estimated_duration)).append(":").append(duration).append('\n')
        stringBuilder.append(context.getString(R.string.real_duration)).append(":").append(realDuration)

        _naviEndInfo.value = stringBuilder.toString()
}

```







