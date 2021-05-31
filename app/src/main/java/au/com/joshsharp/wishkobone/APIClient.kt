package au.com.joshsharp.wishkobone

import okhttp3.OkHttpClient.Builder.retryOnConnectionFailure
import okhttp3.OkHttpClient.Builder.build
import okhttp3.HttpUrl.newBuilder
import okhttp3.HttpUrl.Builder.setQueryParameter
import okhttp3.Request.Builder.url
import okhttp3.HttpUrl.Builder.build
import okhttp3.Request.Builder.get
import okhttp3.Request.Builder.addHeader
import okhttp3.Request.Builder.build
import okhttp3.OkHttpClient.newCall
import okhttp3.Call.enqueue
import okhttp3.FormBody.Builder.add
import okhttp3.FormBody.Builder.build
import okhttp3.Request.Builder.post
import okhttp3.Call.execute
import au.com.joshsharp.wishkobone.APIClient
import okhttp3.*
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
        var urlBuilder: Builder = HttpUrl.parse(getAbsoluteUrl(url))!!.newBuilder()
        if (params != null) {
            for (key in params.keys) {
                urlBuilder = urlBuilder.setQueryParameter(key, params[key])
            }
        }
        val request: Request = Builder().url(urlBuilder.build())
            .get()
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
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
        contentType: String?,
        responseHandler: Callback?
    ) {
        var builder = Builder()
        for (key in params.keys) {
            builder = builder.add(key, params[key])
        }
        val formBody: FormBody = builder.build()
        val request: Request = Builder().url(getAbsoluteUrl(url)).post(formBody)
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .addHeader("Content-type", contentType)
            .build()
        client.newCall(request).enqueue(responseHandler!!)
    }

    fun post(url: String, payload: String, contentType: String?, responseHandler: Callback?) {
        val body: RequestBody = RequestBody.create(payload, get.get(contentType))
        val request: Request = Builder().url(getAbsoluteUrl(url)).post(body)
            .addHeader("User-Agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Origin", "https://www.kobo.com")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .addHeader("Content-type", contentType)
            .build()
        client.newCall(request).enqueue(responseHandler!!)
    }

    @Throws(IOException::class)
    fun syncGet(url: String, params: HashMap<String?, String?>?): Response {
        var urlBuilder: Builder = HttpUrl.parse(getAbsoluteUrl(url))!!.newBuilder()
        if (params != null) {
            for (key in params.keys) {
                urlBuilder = urlBuilder.setQueryParameter(key, params[key])
            }
        }
        val request: Request = Builder().url(urlBuilder.build())
            .get()
            .addHeader("User-agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
            .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", "KoboSession=$cookieValue")
            .build()
        return client.newCall(request).execute()
    }

    @Throws(IOException::class)
    fun syncPost(url: String, payload: String, contentType: String?): Response {
        val body: RequestBody = RequestBody.create(payload, get.get(contentType))
        val request: Request = Builder().url(getAbsoluteUrl(url)).post(body)
            .addHeader("User-agent", agent)
            .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cookie", String.format("KoboSession=%s;", cookieValue))
            .addHeader("Content-type", contentType)
            .build()
        return client.newCall(request).execute()
    }

    @Throws(IOException::class)
    fun syncPost(url: String, params: HashMap<String?, String?>): Response {
        var builder = Builder()
        for (key in params.keys) {
            builder = builder.add(key, params[key])
        }
        val formBody: FormBody = builder.build()
        return this.syncPost(url, formBody.toString(), "application/x-www-form-urlencoded")
    }

    companion object {
        private const val BASE_URL = "https://www.kobo.com/"
        private var client: OkHttpClient
        private const val agent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0"

        private fun getAbsoluteUrl(relativeUrl: String): String {
            return BASE_URL + relativeUrl
        }
    }

    init {
        client = Builder().retryOnConnectionFailure(false)
            .build()
    }
}