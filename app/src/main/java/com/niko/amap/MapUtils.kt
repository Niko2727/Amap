package com.niko.amap

import android.content.Context
import android.graphics.drawable.Drawable

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


}