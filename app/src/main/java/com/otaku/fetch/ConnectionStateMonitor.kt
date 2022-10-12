package com.otaku.fetch

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class ConnectionStateMonitor(
    val onInternetDisconnected: () -> Unit,
    val onInternetConnected: () -> Unit)  : NetworkCallback() {
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    fun enable(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disable(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onInternetConnected()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onInternetDisconnected()
    }

    override fun onUnavailable() {
        super.onUnavailable()
        onInternetDisconnected()
    }
}