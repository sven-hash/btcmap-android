package api

import area.AreaJson
import element.ElementJson
import event.EventJson
import http.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import reports.ReportJson
import user.UserJson
import java.time.ZonedDateTime

class ApiImpl(
    private val httpClient: OkHttpClient,
    private val json: Json,
) : Api {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getElements(updatedSince: ZonedDateTime?, limit: Long): List<ElementJson> {
        val url = HttpUrl.Builder().apply {
            scheme("https")
            host("api.btcmap.org")
            addPathSegment("v2")
            addPathSegment("elements")

            if (updatedSince != null) {
                addQueryParameter("updated_since", updatedSince.toString())
            }

            addQueryParameter("limit", limit.toString())
        }.build()

        val request = httpClient.newCall(Request.Builder().url(url).build())
        val response = request.await()

        if (!response.isSuccessful) {
            throw Exception("Unexpected HTTP response code: ${response.code}")
        }

        return withContext(Dispatchers.IO) {
            response.body!!.byteStream().use { responseBody ->
                withContext(Dispatchers.IO) {
                    json.decodeFromStream(
                        stream = responseBody,
                        deserializer = ListSerializer(ElementJson.serializer()),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getAreas(updatedSince: ZonedDateTime?, limit: Long): List<AreaJson> {
        val url = HttpUrl.Builder().apply {
            scheme("https")
            host("api.btcmap.org")
            addPathSegment("v2")
            addPathSegment("areas")

            if (updatedSince != null) {
                addQueryParameter("updated_since", updatedSince.toString())
            }

            addQueryParameter("limit", limit.toString())
        }.build()

        val request = httpClient.newCall(Request.Builder().url(url).build())
        val response = request.await()

        if (!response.isSuccessful) {
            throw Exception("Unexpected HTTP response code: ${response.code}")
        }

        return withContext(Dispatchers.IO) {
            response.body!!.byteStream().use { responseBody ->
                withContext(Dispatchers.IO) {
                    json.decodeFromStream(
                        stream = responseBody,
                        deserializer = ListSerializer(AreaJson.serializer()),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getEvents(updatedSince: ZonedDateTime?, limit: Long): List<EventJson> {
        val url = HttpUrl.Builder().apply {
            scheme("https")
            host("api.btcmap.org")
            addPathSegment("v2")
            addPathSegment("events")

            if (updatedSince != null) {
                addQueryParameter("updated_since", updatedSince.toString())
            }

            addQueryParameter("limit", limit.toString())
        }.build()

        val request = httpClient.newCall(Request.Builder().url(url).build())
        val response = request.await()

        if (!response.isSuccessful) {
            throw Exception("Unexpected HTTP response code: ${response.code}")
        }

        return withContext(Dispatchers.IO) {
            response.body!!.byteStream().use { responseBody ->
                withContext(Dispatchers.IO) {
                    json.decodeFromStream(
                        stream = responseBody,
                        deserializer = ListSerializer(EventJson.serializer()),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getReports(updatedSince: ZonedDateTime?, limit: Long): List<ReportJson> {
        val url = HttpUrl.Builder().apply {
            scheme("https")
            host("api.btcmap.org")
            addPathSegment("v2")
            addPathSegment("reports")

            if (updatedSince != null) {
                addQueryParameter("updated_since", updatedSince.toString())
            }

            addQueryParameter("limit", limit.toString())
        }.build()

        val request = httpClient.newCall(Request.Builder().url(url).build())
        val response = request.await()

        if (!response.isSuccessful) {
            throw Exception("Unexpected HTTP response code: ${response.code}")
        }

        return withContext(Dispatchers.IO) {
            response.body!!.byteStream().use { responseBody ->
                withContext(Dispatchers.IO) {
                    json.decodeFromStream(
                        stream = responseBody,
                        deserializer = ListSerializer(ReportJson.serializer()),
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getUsers(updatedSince: ZonedDateTime?, limit: Long): List<UserJson> {
        val url = HttpUrl.Builder().apply {
            scheme("https")
            host("api.btcmap.org")
            addPathSegment("v2")
            addPathSegment("users")

            if (updatedSince != null) {
                addQueryParameter("updated_since", updatedSince.toString())
            }

            addQueryParameter("limit", limit.toString())
        }.build()

        val request = httpClient.newCall(Request.Builder().url(url).build())
        val response = request.await()

        if (!response.isSuccessful) {
            throw Exception("Unexpected HTTP response code: ${response.code}")
        }

        return withContext(Dispatchers.IO) {
            response.body!!.byteStream().use { responseBody ->
                withContext(Dispatchers.IO) {
                    json.decodeFromStream(
                        stream = responseBody,
                        deserializer = ListSerializer(UserJson.serializer()),
                    )
                }
            }
        }
    }
}