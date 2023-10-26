package com.hb.surfingcompose.data

import java.net.InetAddress

fun isInternetAvailable(): Boolean {
    return try {
        val ipAddress = InetAddress.getByName("www.google.com")
        !ipAddress.equals("")
    } catch (e: Exception) {
        false
    }
}