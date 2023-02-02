package au.com.joshsharp.wishkobone

import android.app.Application
import android.content.SharedPreferences

class WishkoboneApplication: Application() {

    var client = APIClient()

    override fun onCreate() {
        super.onCreate()
        if (getCookie() != null){
            client.setCookie(getCookie())
        }
    }

    fun getCookie(): String? {
        val prefs = getSharedPreferences(getString(R.string.app_name), 0)
        return prefs.getString("cookie",null)
    }

    fun setCookie(cookie: String) {
        val editor = getSharedPreferences(getString(R.string.app_name), 0).edit()
        editor.putString("cookie", cookie)
        editor.apply()
        client.setCookie(cookie)

    }

    fun invalidateCookie() {
        val editor = getSharedPreferences(getString(R.string.app_name), 0).edit()
        editor.clear()
        editor.apply()
        client = APIClient() // remove cookie

    }
}