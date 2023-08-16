package com.hb.surfingcompose.data

import android.util.Log
import java.net.InetAddress

fun isInternetAvailable(): Boolean {
    return try {
        val ipAddress = InetAddress.getByName("www.google.com")
        !ipAddress.equals("")
    } catch (e: Exception) {
        Log.d("isInternetAvailable", "exception ${e.message}")
        false
    }
}