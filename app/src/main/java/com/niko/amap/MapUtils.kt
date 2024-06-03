package com.niko.amap

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption

object MapUtils {

    fun getLocationImage(context: Context, locateStyle: MainViewModel.LocationStyle): Drawable? {
        return when (locateStyle) {
            MainViewModel.LocationStyle.Center -> {
                context.getDrawable(R.drawable.location_center)
            }

            MainViewModel.LocationStyle.Loss -> {
                context.getDrawable(R.drawable.location_loss)
            }

            MainViewModel.LocationStyle.Follow -> {
                context.getDrawable(R.drawable.location_follow)
            }
        }
    }

    fun getProjectionImage(context: Context, projectionStyle: MainViewModel.ProjectionStyle): Drawable? {
        return when (projectionStyle) {
            MainViewModel.ProjectionStyle.TwoD -> {
                context.getDrawable(R.drawable.projection_2d)
            }

            MainViewModel.ProjectionStyle.ThreeD -> {
                context.getDrawable(R.drawable.projection_3d)
            }
        }
    }

    fun initMapLocationClient(context: Context) {

        //初始化定位
        val mLocationClient = AMapLocationClient(context)

        //初始化AMapLocationClientOption对象
        val mLocationOption = AMapLocationClientOption()

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        //如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true)

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true)

        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(false)

        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false)
        mLocationClient.setLocationOption(mLocationOption)

        mLocationClient.setLocationListener { Log.d("-_-", "initMapLocationClient() called $it") }

        //启动定位
        mLocationClient.startLocation();
    }


}