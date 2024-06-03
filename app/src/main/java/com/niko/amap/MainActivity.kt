package com.niko.amap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CameraUpdateFactory.newCameraPosition
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Poi
import com.amap.api.maps.model.animation.ScaleAnimation
import com.amap.api.navi.model.NaviLatLng
import com.niko.amap.databinding.ActivityMainBinding


class MainActivity : ComponentActivity() {

    // region const

    companion object {
        private const val TAG = "MainActivity"
    }

    // endregion

    // region view

    private lateinit var binding: ActivityMainBinding

    private lateinit var mapView: MapView

    // endregion

    // region map camera

    private val defaultZoomLevel = 18f

    /**
     * Zoom level in Follow Mode
     */
    private val followZoomLevel = 19f

    /**
     * The tilt angle of the 2D view
     */
    private val tilt2D = 0f

    /**
     * The tilt angle of the 3D view
     */
    private val tilt3D = 45f

    /**
     * Is it currently in the positioned animation
     */
    private var locateAnimating = false

    private val cameraAnimationDuration = 300L

    // endregion

    // region marker

    private val markerOption by lazy { MarkerOptions() }

    private var marker: Marker? = null

    private val markerAnimation by lazy { ScaleAnimation(0f, 1f, 0f, 1f).apply { setDuration(500) } }

    // endregion

    // region members

    private lateinit var map: AMap

    private val locationStyle = MyLocationStyle()

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    private lateinit var backgroundLocationPermissionRequest: ActivityResultLauncher<Array<String>>

    private val vm by viewModels<MainViewModel>()

    // endregion

    // region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.locationStyle = vm.locationStyle
        binding.projectionStyle = vm.projectionStyle

        initLocateButton()
        initProjectionButton()
        subscribeViewModel()

        initPrivacy()
        initMap(savedInstanceState)
        registerPermissionResultReceiver()
        checkLocationPermission()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy() called")
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy()
    }

    override fun onResume() {
        Log.i(TAG, "onResume() called")
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume()
    }

    override fun onPause() {
        Log.i(TAG, "onPause() called")
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        Log.i(TAG, "onSaveInstanceState() called with: outState = $outState")
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState)
    }

    // endregion lifecycle

    // region button

    private fun initLocateButton() {
        Log.i(TAG, "initLocateButton() called")
        binding.locationBtn.setOnClickListener { vm.clickLocationButton() }
    }

    private fun initProjectionButton() {
        Log.i(TAG, "initProjectionButton() called")
        binding.projectionBtn.setOnClickListener { vm.clickProjectionButton() }
    }

    // endregion

    // region permission

    private fun registerPermissionResultReceiver() {
        Log.i(TAG, "registerPermissionResultReceiver() called")
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    requestBackgroundLocationPermission()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.

                }

                else -> {
                    // No location access granted.
                }
            }
        }

        backgroundLocationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    // Precise location access granted.
                }

                else -> {
                    // No location access granted.
                }
            }
        }
    }

    private fun checkLocationPermission() {
        Log.i(TAG, "checkLocationPermission() called")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        Log.i(TAG, "requestLocationPermission() called")
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestBackgroundLocationPermission() {
        Log.i(TAG, "requestBackgroundLocationPermission() called")
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
    }

    // endregion

    // region Amap

    private fun initPrivacy() {
        Log.i(TAG, "initPrivacy() called")
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        Log.i(TAG, "initMap() called with: savedInstanceState = $savedInstanceState")
        //获取地图控件引用
        mapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);

        //初始化地图控制器对象
        map = mapView.map

        val locationStyle = MyLocationStyle() //初始化定位蓝点样式类
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)

        with(map) {
            myLocationStyle = locationStyle //设置定位蓝点的Style
            isMyLocationEnabled = true // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            isTouchPoiEnable = true
            addOnPOIClickListener {
                onClickPOI(it)
            }

            setAMapGestureListener(object : DefaultAmapGestureListener() {
                override fun onFling(p0: Float, p1: Float) {
                    super.onFling(p0, p1)
                    vm.moveMap()
                }

                override fun onScroll(p0: Float, p1: Float) {
                    super.onScroll(p0, p1)
                    vm.moveMap()
                }
            })

            setOnMapLoadedListener {
                showMapText(true)
                showIndoorMap(true)
                showBuildings(true)
            }

            moveCamera(CameraUpdateFactory.zoomTo(defaultZoomLevel))
        }

        with(map.uiSettings) {
            isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示，非必需设置。
            isZoomControlsEnabled = true    // 设置缩放按钮
        }
    }

    private fun onClickPOI(poi: Poi) {
        Log.i(TAG, "onClickPOI() called with: poi = $poi")
        marker?.remove()
        with(markerOption) {
            position(poi.coordinate)
            title(poi.name)
        }

        marker = map.addMarker(markerOption).apply {
            setAnimation(markerAnimation)
            startAnimation()
        }

        navigate(poi)
    }

    private fun getUserLatlng(): LatLng? {
        if (map.myLocation == null) return null
        return LatLng(map.myLocation.latitude, map.myLocation.longitude)
    }


    private fun changeLocateMode(zoomLevel: Float, tilt: Float, myLocationStyle: Int, tryAgain: Boolean = true) {
        Log.i(TAG, "changeLocateMode() called with: zoomLevel = $zoomLevel, tilt = $tilt, mylocationStyle = $myLocationStyle,tryAgain = $tryAgain")

        val changeLocationType = locationStyle.myLocationType != myLocationStyle

        if (getUserLatlng() == null) {
            Log.i(TAG, "changeLocateMode() return due to getUserLatlng() is null")
            return
        }
        map.stopAnimation()
        locateAnimating = true
        map.animateCamera(
            newCameraPosition(CameraPosition(getUserLatlng(), zoomLevel, tilt, 0f)),
            cameraAnimationDuration,
            object : CameraAnimationCallback() {
                override fun animated(cancel: Boolean) {
                    locateAnimating = false
                    Log.i(TAG, "changeLocateMode animated cancel = $cancel")

                    if (cancel && tryAgain) changeLocateMode(zoomLevel, tilt, myLocationStyle, false)
                    else if (changeLocationType) {
                        locationStyle.myLocationType(myLocationStyle)
                        map.myLocationStyle = locationStyle
                    }
                }
            })
    }

    // endregion

    // region navigate

    private fun navigate(endPoint: Poi) {

        NaviActivity.start(
            this, NaviLatLng(map.myLocation.latitude, map.myLocation.longitude),
            NaviLatLng(endPoint.coordinate.latitude, endPoint.coordinate.longitude), true
        )
    }

    // endregion

    // region subscribe viewModel

    private fun subscribeViewModel() {
        vm.locationStyle.observe(this) {
            Log.i(TAG, "observe locationStyle = $it")
            when (it) {
                MainViewModel.LocationStyle.Center -> {
                    changeLocateMode(defaultZoomLevel, tilt2D, MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                }

                MainViewModel.LocationStyle.Follow -> {
                    changeLocateMode(followZoomLevel, tilt3D, MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                }

                else -> {
                    if (locationStyle.myLocationType != MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER) {
                        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                        map.myLocationStyle = locationStyle
                    }
                }
            }
        }

        vm.projectionStyle.observe(this) {
            Log.i(TAG, "observe projectionStyle = $it, locateAnimating = $locateAnimating")

            if (locateAnimating) return@observe

            val degree = if (it == MainViewModel.ProjectionStyle.ThreeD) tilt3D else tilt2D

            Log.i(TAG, "$it animate")
            map.stopAnimation()
            map.animateCamera(CameraUpdateFactory.changeTilt(degree), cameraAnimationDuration, object : CameraAnimationCallback() {
                override fun animated(cancel: Boolean) {
                    Log.i(TAG, "$it animated cancel = $cancel")
                }
            })
        }

    }

    // endregion

}