package ru.wb.go.network.monitor

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresPermission

class NetworkMonitor
@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
constructor(val application: Application) {

    fun startNetworkCallback() {
        val builder = NetworkRequest.Builder()
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            connectivityManager().registerDefaultNetworkCallback(networkCallback)
        } else {
            connectivityManager().registerNetworkCallback(builder.build(), networkCallback)
        }
    }

    fun stopNetworkCallback() {
        connectivityManager().unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }

    private fun connectivityManager() =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        val state = NetworkState()

        override fun onAvailable(network: Network) {
            state.isNetworkConnected = true
        }

        override fun onLost(network: Network) {
            state.isNetworkConnected = false
        }
    }

}
