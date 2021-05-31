package au.com.joshsharp.wishkobone

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.Throws
import java.util.HashMap

/**
 * Created by Josh on 2/09/2014.
 */
class APIClient {
    private var cookieValue: String? = null
    fun setCookie(value: String?) {
        cookieValue = value
    }

    operator fun get(url: String, params: HashMap<String?, String?>?, responseHandler: Callback?) {
        var urlBuilder: HttpUrl.Builder = getAbsoluteUrl(url).toHttpUrl().newBuilder()
        params?.let {
            for (key in it.keys.filterNotNull()) {
                urlBuilder = urlBuilder.setQueryParameter(key, params[key])
            }
        }
        val request: Request = Request.Builder().url(urlBuilder.build())
            .get()
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .build()
        client.newCall(request).enqueue(responseHandler!!)
    }

    fun post(url: String, params: HashMap<String?, String?>, responseHandler: Callback?) {
        this.post(url, params, "application/x-www-form-urlencoded", responseHandler)
    }

    fun post(
        url: String,
        params: HashMap<String?, String?>,
        contentType: String,
        responseHandler: Callback?
    ) {
        var builder = FormBody.Builder()
        for (key in params.keys.filterNotNull()) {
            builder = builder.add(key, params[key].toString())
        }
        val formBody: FormBody = builder.build()
        val request: Request = Request.Builder().url(getAbsoluteUrl(url)).post(formBody)
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .addHeader("Content-type", contentType)
            .build()
        client.newCall(request).enqueue(responseHandler!!)
    }

    fun post(url: String, payload: String, contentType: String, responseHandler: Callback?) {
        val body: RequestBody = payload.toRequestBody(contentType.toMediaType())
        val request: Request = Request.Builder().url(getAbsoluteUrl(url)).post(body)
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Origin", "https://www.kobo.com")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .addHeader("Content-type", contentType)
            .build()
        client.newCall(request).enqueue(responseHandler!!)
    }

    @Throws(IOException::class)
    fun syncGet(url: String, params: HashMap<String, String>?): Response {
        var urlBuilder: HttpUrl.Builder = getAbsoluteUrl(url).toHttpUrl().newBuilder()
        params?.let {
            for (key in it.keys) {
                urlBuilder = urlBuilder.setQueryParameter(key, it[key])
            }
        }
        val request: Request = Request.Builder().url(urlBuilder.build())
            .get()
            .addHeader("User-agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .build()
        return client.newCall(request).execute()
    }

    @Throws(IOException::class)
    fun syncPost(url: String, payload: String, contentType: String): Response {
        val body: RequestBody = payload.toRequestBody(contentType.toMediaType())
        val request: Request = Request.Builder().url(getAbsoluteUrl(url)).post(body)
            .addHeader("User-agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", String.format("KoboSession=%s;", cookieValue))
            .addHeader("Content-type", contentType)
            .build()
        return client.newCall(request).execute()
    }

    @Throws(IOException::class)
    fun syncPost(url: String, params: HashMap<String, String>): Response {
        var builder = FormBody.Builder()
        for (key in params.keys) {
            builder = builder.add(key, params[key].toString())
        }
        val formBody: FormBody = builder.build()
        return this.syncPost(url, formBody.toString(), "application/x-www-form-urlencoded")
    }

    companion object {
        private const val BASE_URL = "https://www.kobo.com/"
        private var client: OkHttpClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build()
        private const val agent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0"
        private fun getAbsoluteUrl(relativeUrl: String): String {
            return BASE_URL + relativeUrl
        }
    }
}