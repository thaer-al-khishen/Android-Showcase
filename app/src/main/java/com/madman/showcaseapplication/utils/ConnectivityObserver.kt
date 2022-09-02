package com.madman.showcaseapplication.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class ConnectivityObserver(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus = callbackFlow {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable).isSuccess
            }

            override fun onAvailable(network: Network) {
                val hasInternet = InternetUtils.hasActiveInternet()
                if (hasInternet) {
                    trySend(NetworkStatus.Available).isSuccess
                }
                else trySend(NetworkStatus.AvailableButNotConnected).isSuccess
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable).isSuccess
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }

}

inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onUnavailable: suspend () -> Result,
    crossinline onAvailable: suspend () -> Result,
    crossinline onAvailableButNotConnected: suspend () -> Result,
): Flow<Result> = map { status ->
    when (status) {
        NetworkStatus.Unavailable -> onUnavailable()
        NetworkStatus.Available -> onAvailable()
        NetworkStatus.AvailableButNotConnected -> onAvailableButNotConnected()
    }
}

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object AvailableButNotConnected : NetworkStatus()
    object Unavailable : NetworkStatus()
}