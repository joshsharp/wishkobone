package au.com.joshsharp.wishkobone;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Josh on 2/09/2014.
 */
public class APIClient {
    private static final String BASE_URL = "https://www.kobo.com/";
    private static OkHttpClient client;
    private static String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0";
    private String cookieValue;

    public APIClient() {
        client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .build();

    }

    public void setCookie(String value){
        cookieValue = value;
    }

    public void get(String url, HashMap<String, String> params, Callback responseHandler) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getAbsoluteUrl(url)).newBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                urlBuilder = urlBuilder.setQueryParameter(key, params.get(key));
            }
        }

        Request request = new Request.Builder().url(urlBuilder.build())
                .get()
                .addHeader("User-Agent", agent)
                .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
                .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
                .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie", "KoboSession=" + cookieValue)
                .build();
        client.newCall(request).enqueue(responseHandler);
    }

    public void post(String url, @NotNull HashMap<String, String> params, Callback responseHandler) {
        this.post(url, params, "application/x-www-form-urlencoded", responseHandler);
    }

    public void post(String url, @NotNull HashMap<String, String> params, String contentType, Callback responseHandler) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : params.keySet()) {
            builder = builder.add(key, params.get(key));
        }

        FormBody formBody = builder.build();

        Request request = new Request.Builder().url(getAbsoluteUrl(url)).post(formBody)
                .addHeader("User-Agent", agent)
                .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
                .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
                .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie", "KoboSession=" + cookieValue)
                .addHeader("Content-type", contentType)
                .build();
        client.newCall(request).enqueue(responseHandler);

    }

    public void post(String url, @NotNull String payload, String contentType, Callback responseHandler){

        RequestBody body = RequestBody.create(payload, MediaType.get(contentType));

        Request request = new Request.Builder().url(getAbsoluteUrl(url)).post(body)
                .addHeader("User-Agent", agent)
                .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Origin","https://www.kobo.com")
                .addHeader("Cookie", "KoboSession=" + cookieValue)
                .addHeader("Content-type", contentType)
                .build();
        client.newCall(request).enqueue(responseHandler);
    }

    public Response syncGet(String url, HashMap<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getAbsoluteUrl(url)).newBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                urlBuilder = urlBuilder.setQueryParameter(key, params.get(key));
            }
        }

        Request request = new Request.Builder().url(urlBuilder.build())
                .get()
                .addHeader("User-agent", agent)
                .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
                .addHeader("Accept", "application / json, text / javascript, * / *; q = 0.01")
                .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie", "KoboSession=" + cookieValue)
                .build();
        return client.newCall(request).execute();
    }

    public Response syncPost(String url, @NotNull String payload, String contentType) throws IOException {


        RequestBody body = RequestBody.create(payload, MediaType.get(contentType));

        Request request = new Request.Builder().url(getAbsoluteUrl(url)).post(body)
                .addHeader("User-agent", agent)
                .addHeader("Referer", "https://www.kobo.com/au/en/account/wishlist")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Accept-Language", "en-GB,en-AU;q=0.7,en;q=0.3")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("Cookie", String.format("KoboSession=%s;", cookieValue))
                .addHeader("Content-type", contentType)
                .build();
        return client.newCall(request).execute();
    }

    public Response syncPost(String url, @NotNull HashMap<String, String> params) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : params.keySet()) {
            builder = builder.add(key, params.get(key));
        }

        FormBody formBody = builder.build();

        return this.syncPost(url, formBody.toString(), "application/x-www-form-urlencoded");
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
