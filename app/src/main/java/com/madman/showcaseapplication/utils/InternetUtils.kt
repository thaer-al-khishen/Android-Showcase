package com.madman.showcaseapplication.utils

import java.net.HttpURLConnection
import java.net.URL

object InternetUtils {

    fun hasActiveInternet(): Boolean {
        try {
            val url = URL("http://www.google.com")
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("User-Agent", "Android Application:1")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 2000
            urlConnection.connect()

            // http://www.w3.org/Protocols/HTTP/HTRESP.html
            if (urlConnection.responseCode === 200 || urlConnection.responseCode > 400) {
                // Requested site is available
                return true
            }
        } catch (ex: Exception) {
            // Error while trying to connect
            return false
        }
        return false
    }

}
