package com.niko.amap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.Poi

class MainViewModel : ViewModel() {

    /**
     * [LocationStyle.Loss]: locate the blue dot not in the middle of the screen
     * [LocationStyle.Center]: locate the blue dot in the middle of the screen (2D)
     * [LocationStyle.Follow]: locate the blue dot in the middle of the screen (3D)
     */
    enum class LocationStyle { Loss, Center, Follow }
    enum class ProjectionStyle { TwoD, ThreeD }

    private val _locationStyle = MutableLiveData(LocationStyle.Center)
    val locationStyle: LiveData<LocationStyle> = _locationStyle


    private val _projectionStyle = MutableLiveData(ProjectionStyle.TwoD)
    val projectionStyle: LiveData<ProjectionStyle> = _projectionStyle

    private val _marker = MutableLiveData<Marker>()
    val marker: LiveData<Marker> = _marker

    private val _choosingNaviType = MutableLiveData(false)
    val choosingNaviType: LiveData<Boolean> = _choosingNaviType

    var poi: Poi? = null

    fun moveMap() {
        //After moving the map, the positioning blue dot is no longer in the middle of the screen
        if (_locationStyle.value != LocationStyle.Loss)
            _locationStyle.value = LocationStyle.Loss
    }

    fun clickMark() {
        //After click marker, the positioning blue dot is no longer in the middle of the screen
        if (_locationStyle.value != LocationStyle.Loss)
            _locationStyle.value = LocationStyle.Loss
    }

    fun clickLocationButton() {
        if (_locationStyle.value == LocationStyle.Loss) {
            _locationStyle.value = LocationStyle.Center
            _projectionStyle.value = ProjectionStyle.TwoD
        } else if (_locationStyle.value == LocationStyle.Center) {
            _locationStyle.value = LocationStyle.Follow
            _projectionStyle.value = ProjectionStyle.ThreeD
        } else if (_locationStyle.value == LocationStyle.Follow) {
            _locationStyle.value = LocationStyle.Center
            _projectionStyle.value = ProjectionStyle.TwoD
        }
    }

    fun clickProjectionButton() {
        if (_projectionStyle.value == ProjectionStyle.TwoD) {
            _projectionStyle.value = ProjectionStyle.ThreeD
        } else if (_projectionStyle.value == ProjectionStyle.ThreeD) {
            _projectionStyle.value = ProjectionStyle.TwoD
        }
    }

    fun clickPoi(marker: Marker, p: Poi) {
        _marker.value?.remove()
        _marker.value = marker
        poi = p
        _choosingNaviType.value = true
    }

    fun clickRouteBtn() {
        _choosingNaviType.value = !(_choosingNaviType.value ?: false)
    }

}