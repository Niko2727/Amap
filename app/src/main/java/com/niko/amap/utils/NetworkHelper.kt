package com.niko.amap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log

class NetworkHelper {

    companion object {
        private const val TAG = "NetworkHelper"
    }


    private var callback: NetworkCallback? = null

    private var cm: ConnectivityManager? = null

    private val availableNetworks: MutableSet<Network> = HashSet()

    private val handler = Handler(Looper.getMainLooper())

    /**
     * WIFI和数据同时连接时，如果断开wifi，会先回调[NetworkCallback.onLost],再回调[NetworkCallback.onAvailable]
     * 所以需要延迟判断是否断网
     */
    private val runnable = Runnable { onNetworkDisconnected?.invoke() }

    private val delayDuration = 2000L

    private var onNetworkDisconnected: (() -> Unit)? = null

    fun register(context: Context, onNetworkDisconnected: () -> Unit) {

        this.onNetworkDisconnected = onNetworkDisconnected

        //获取ConnectivityManager
        cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (cm?.activeNetwork == null) {
            onNetworkDisconnected()
        }

        callback = object : NetworkCallback() {


            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable() called with: network = $network")
                if (availableNetworks.isEmpty()) {
                    handler.removeCallbacks(runnable)
                }
                availableNetworks.add(network)
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "onLost() called with: network = $network")
                availableNetworks.remove(network)
                if (availableNetworks.isEmpty()) {
                    handler.postDelayed(runnable, delayDuration)
                }
            }
        }

        val builder =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        cm?.registerNetworkCallback(builder.build(), callback!!)
    }

    fun unregister() {
        callback?.run { cm?.unregisterNetworkCallback(this) }
    }

}