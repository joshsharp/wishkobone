package au.com.joshsharp.wishkobone

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Login to Kobo to continue"

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(web, true)

        web.settings.javaScriptEnabled = true;
        web.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                Log.d("cookie", "now at ${url}")
                Log.d("cookie",getCookie(".kobo.com","KoboSession") ?: "null");

                val cookieVal = getCookie(".kobo.com","KoboSession")
                if (cookieVal != null){
                    val app = application as WishkoboneApplication
                    app.setCookie(cookieVal)
                    this@LoginActivity.finish()
                }
            }
        }

        cookieManager.removeAllCookies {
            web.loadUrl("https://www.kobo.com/account/wishlist")
        }
    }

    fun getCookie(siteName: String, name: String): String? {

        val manager = CookieManager.getInstance()
        manager.getCookie(siteName)?.let {cookies ->
            val typedArray = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (element in typedArray) {
                val split = element.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if(split.size >= 2) {
                    if (split[0].trim() == name) {
                        return split[1]
                    }
                }

            }
        }

        return null
    }
}