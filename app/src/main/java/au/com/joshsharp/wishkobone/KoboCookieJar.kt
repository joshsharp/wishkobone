package au.com.joshsharp.wishkobone

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.ArrayList

class KoboCookieJar: CookieJar {

    var list = ArrayList<Cookie>();

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        Log.d("cookies", "returning some cookies: ${list.joinToString(", ")}")
        return list;
    }

    fun setCookie(value: String){
        list.clear();
        list.add(Cookie.Builder()
            .name("KoboSession")
            .domain("kobo.com")
            .expiresAt(2092109000000)
            .value(value)
            .build());
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // pass
    }
}