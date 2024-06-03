package com.niko.amap

import com.amap.api.navi.AMapNaviViewListener

open class DefaultAmapNaviViewListener : AMapNaviViewListener {
    override fun onNaviSetting() {
    }

    override fun onNaviCancel() {
    }

    override fun onNaviBackClick(): Boolean {
        return false
    }

    override fun onNaviMapMode(p0: Int) {
    }

    override fun onNaviTurnClick() {
    }

    override fun onNextRoadClick() {
    }

    override fun onScanViewButtonClick() {
    }

    override fun onLockMap(p0: Boolean) {
    }

    override fun onNaviViewLoaded() {
    }

    override fun onMapTypeChanged(p0: Int) {
    }

    override fun onNaviViewShowMode(p0: Int) {
    }
}