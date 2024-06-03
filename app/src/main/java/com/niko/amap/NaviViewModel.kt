package com.niko.amap


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.navi.model.AMapNaviPath

class NaviViewModel : ViewModel() {


    private val _naviEndInfo = MutableLiveData<String>(null)
    val naviEndInfo: LiveData<String> = _naviEndInfo

    private var naviStartTime = 0L

    private var naviPath: AMapNaviPath? = null

    fun onNaviStart(path: AMapNaviPath) {
        naviStartTime = System.currentTimeMillis()
        naviPath = path
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


    private fun getDistanceDisplayString(distanceMeters: Int, context: Context): String {
        if (distanceMeters >= 1000) {
            return String.format("%.1f", distanceMeters / 1000f) + context.getString(R.string.kilometer)
        } else {
            return distanceMeters.toString() + context.getString(R.string.meter)
        }
    }

    private fun getTimeDisplayString(timeSecond: Int, context: Context): String {
        val stringBuilder = StringBuilder()

        var second = timeSecond

        if (second > 3600) {
            val hour = second / 3600
            stringBuilder.append(hour).append(context.getString(R.string.hour))
            second %= 3600
        }

        if (second > 60) {
            val minutes = second / 60
            stringBuilder.append(minutes).append(context.getString(R.string.minute))
            second %= 60
        }

        stringBuilder.append(second).append(context.getString(R.string.second))


        return stringBuilder.toString()
    }


}