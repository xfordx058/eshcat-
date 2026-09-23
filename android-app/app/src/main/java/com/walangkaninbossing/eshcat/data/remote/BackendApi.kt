package com.walangkaninbossing.eshcat.data.remote

import com.walangkaninbossing.eshcat.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/** Checks the Flask LAN connection by reading services from the server database. */
object BackendApi {
    suspend fun fetchServiceCount(): Int = withContext(Dispatchers.IO) {
        val endpoint = URL(BuildConfig.BACKEND_BASE_URL.trimEnd('/') + "/api/services")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                throw IllegalStateException("Server returned HTTP $status")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONArray(body).length()
        } finally {
            connection.disconnect()
        }
    }
}
