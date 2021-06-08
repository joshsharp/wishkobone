package au.com.joshsharp.wishkobone

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.HashMap

/**
 * Created by Josh on 2/09/2014.
 */
class APIClient {
    var cookieValue: String? = null

    operator fun get(url: String, params: HashMap<String, String?>?, responseHandler: Callback) {
        var urlBuilder: HttpUrl.Builder = getAbsoluteUrl(url).toHttpUrl().newBuilder()
        params?.let {
            for (key in it.keys) {
                urlBuilder = urlBuilder.setQueryParameter(key, it[key])
            }
        }
        val request: Request = Request.Builder().url(urlBuilder.build())
            .get()
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .build()
        client.newCall(request).enqueue(responseHandler)
    }

    fun post(url: String, payload: String, contentType: String, responseHandler: Callback) {
        val body: RequestBody = payload.toRequestBody(contentType.toMediaType())
        val request: Request = Request.Builder().url(getAbsoluteUrl(url)).post(body)
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .build()
        client.newCall(request).enqueue(responseHandler)
    }

    companion object {
        private const val BASE_URL = "https://www.kobo.com/"
        private var client: OkHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build()
        private fun getAbsoluteUrl(relativeUrl: String): String {
            return BASE_URL + relativeUrl
        }
    }
}