package com.niko.amap

import com.amap.api.maps.AMap.CancelableCallback

abstract class CameraAnimationCallback : CancelableCallback {

    abstract fun animated(cancel: Boolean)


    override fun onFinish() {
        animated(false)
    }

    override fun onCancel() {
        animated(true)
    }
}