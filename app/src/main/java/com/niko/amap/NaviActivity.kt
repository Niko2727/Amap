package com.niko.amap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviView
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.AMapNaviLocation
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.navi.view.RouteOverLay
import com.niko.amap.databinding.ActivityNaviBinding

class NaviActivity : ComponentActivity() {

    // region const
    companion object {

        private const val TAG = "NaviActivity"

        private const val START_EXTRA = "start"
        private const val END_EXTRA = "end"
        private const val IS_GPS_EXTRA = "is_gps"

        fun start(context: Context, start: NaviLatLng, end: NaviLatLng, isGps: Boolean) {
            val intent = Intent(context, NaviActivity::class.java)
            intent.putExtra(START_EXTRA, start)
            intent.putExtra(END_EXTRA, end)
            intent.putExtra(IS_GPS_EXTRA, isGps)
            context.startActivity(intent)
        }
    }

    // endregion

    // region vew

    private lateinit var naviView: AMapNaviView

    private lateinit var binding: ActivityNaviBinding

    // endregion

    // region members

    private lateinit var mapNavi: AMapNavi

    private lateinit var map: AMap

    private var isGps = false

    private val vm by viewModels<NaviViewModel>()

    private var routeOverLay: RouteOverLay? = null

    private val defaultZoomLevel = 18f

    // endregion

    // region callback

    private val naviListener = object : DefaultAmapNaviListener() {

        override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
            super.onCalculateRouteSuccess(p0)

            routeOverLay?.removeFromMap()
            routeOverLay?.destroy()

            //获取返回路线的数组routIDs aMapCalcRouteResult会返回一条或者多条路线。
            //ps:多条路线是用来做多路线选择的功能但是这里我们只做简单导航。所以我们只绘制一条。
            val routIds = p0!!.routeid
            val routeId = routIds[0]
            //通过routeId获取AMapNaviPath数据。
            val path = mapNavi.naviPaths[routeId]
            //然后就可以创建RouteOverLay了
            routeOverLay = RouteOverLay(map, path, this@NaviActivity)
            //添加到AMapNaviView上。
            routeOverLay?.addToMap()

            vm.onNaviStart(path!!)

            onCalculateWalkRouteSuccess()
        }

//        override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {
//            super.onNaviInfoUpdate(naviInfo)
//            var naviLatLngList: List<NaviLatLng> = routeOverLay!!.getArrowPoints(naviInfo!!.curStep)
//            //画导航的箭头
//            routeOverLay?.drawArrow(naviLatLngList)
//        }

        override fun onLocationChange(p0: AMapNaviLocation?) {
            super.onLocationChange(p0)
            //画走过的灰色路线
            routeOverLay?.updatePolyline(p0)
        }


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

    private val naviViewListener = object : DefaultAmapNaviViewListener() {
        override fun onNaviBackClick(): Boolean {
            finish()
            return true
        }
    }

    // endregion

    // region lifecycle

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navi)
        binding.lifecycleOwner = this
        binding.setNaviEndInfo(vm.naviEndInfo)

        naviView = binding.naviView
        naviView.onCreate(savedInstanceState)
        naviView.setAMapNaviViewListener(naviViewListener)

        initNavi()
        initMap()

        getNaviParam()
    }

    public override fun onResume() {
        super.onResume()
        naviView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        naviView.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        naviView.onDestroy()

        mapNavi.stopNavi()
        mapNavi.removeAMapNaviListener(naviListener)
    }

    // endregion

    // region map

    private fun initMap() {
        map = naviView.map

        val locationStyle = MyLocationStyle() //初始化定位蓝点样式类
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)

        with(map) {
            myLocationStyle = locationStyle //设置定位蓝点的Style
            isMyLocationEnabled = true // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            isTouchPoiEnable = false

            setOnMapLoadedListener {
                showMapText(true)
                showIndoorMap(true)
                showBuildings(true)
            }

            map.moveCamera(CameraUpdateFactory.zoomTo(defaultZoomLevel))
            map.moveCamera(CameraUpdateFactory.changeTilt(45f))
        }

        with(map.uiSettings) {
            isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示，非必需设置。
            isZoomControlsEnabled = false    // 设置缩放按钮
        }
    }

    // endregion

    // region navigate

    private fun initNavi() {
        mapNavi = AMapNavi.getInstance(applicationContext)
        mapNavi.addAMapNaviListener(naviListener)
        mapNavi.setEmulatorNaviSpeed(30)
        mapNavi.setUseInnerVoice(true, true)
    }

    /**
     *  获取intent参数并计算路线
     */
    private fun getNaviParam() {
        val intent = intent ?: return
        isGps = intent.getBooleanExtra(IS_GPS_EXTRA, false)
        val start = intent.getParcelableExtra(START_EXTRA, NaviLatLng::class.java)
        val end = intent.getParcelableExtra(END_EXTRA, NaviLatLng::class.java)

        calculateWalkRoute(start!!, end!!)
    }

    //驾车路径规划计算,计算单条路径
    private fun calculateDriveRoute(start: NaviLatLng, end: NaviLatLng) {
        var strategyFlag = 0
        val startList: MutableList<NaviLatLng> = ArrayList()

        /**
         * 途径点坐标集合
         */
        val wayList: List<NaviLatLng> = ArrayList()

        /**
         * 终点坐标集合［建议就一个终点］
         */
        val endList: MutableList<NaviLatLng> = ArrayList()
        try {
            strategyFlag = mapNavi.strategyConvert(true, false, false, true, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startList.add(start)
        endList.add(end)
        mapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag)
    }

    //路径规划成功后开始导航
    fun onCalculateRouteSuccess() {
        if (isGps) {
            mapNavi.startNavi(NaviType.GPS)
        } else {
            mapNavi.startNavi(NaviType.EMULATOR)
        }
    }

    //步行路径规划计算,计算单条路径
    private fun calculateWalkRoute(start: NaviLatLng, end: NaviLatLng) {
        mapNavi.calculateWalkRoute(start, end)
    }

    //路径规划成功后开始导航
    fun onCalculateWalkRouteSuccess() {
        if (isGps) {
            mapNavi.startNavi(NaviType.GPS)
        } else {
            mapNavi.startNavi(NaviType.EMULATOR)
        }
    }

    // endregion

}