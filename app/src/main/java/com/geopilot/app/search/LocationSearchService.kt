package com.geopilot.app.search

import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

data class LocationSearchResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

class LocationSearchService(
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun search(query: String): Result<List<LocationSearchResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
            val request = Request.Builder()
                .url("https://nominatim.openstreetmap.org/search?format=jsonv2&limit=6&countrycodes=id&q=$encodedQuery")
                .header("User-Agent", "GeoPilot-Android/0.1 (location testing app)")
                .header("Accept-Language", "en,id;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Search service returned ${response.code}")
                val body = response.body?.string() ?: throw IOException("Search response was empty")
                json.parseToJsonElement(body).jsonArray.mapNotNull { element ->
                    val item = element.jsonObject
                    val latitude = item["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    val longitude = item["lon"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    val name = item["display_name"]?.jsonPrimitive?.content
                    if (latitude == null || longitude == null || name.isNullOrBlank()) null
                    else LocationSearchResult(name, latitude, longitude)
                }
            }
        }
    }
}
