package au.com.joshsharp.wishkobone

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import au.com.joshsharp.wishkobone.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Log in to Kobo to continue"

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.web, true)

        binding.web.settings.javaScriptEnabled = true;
        binding.web.webViewClient = object: WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.contains("account/wishlist", ignoreCase = true) == true){
                    val dialog = ProgressDialog.show(
                        this@LoginActivity, "",
                        "Loading. Please wait...", true
                    ).show()
                }
            }

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
            binding.web.loadUrl("https://www.kobo.com/account/wishlist")
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