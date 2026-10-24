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
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import au.com.joshsharp.wishkobone.databinding.ActivityLoginBinding
import java.net.URL


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val STATUS_BAR_TAG = "status_bar"

    fun applyEdgeToEdgeInsets(statusBarDrawable: Drawable?) {
        // Add this condition if you only want to support edge to edge in Android 15+ devices else remove this.
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val view = findViewById<View>(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                val bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
                )

                val statusBarHeight =
                    windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top

                applyStatusBarColor(window, statusBarDrawable, true, statusBarHeight)
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
                v.updatePadding(
                    left = bars.left,
                    top =  bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                windowInsets
            }
        //}
    }

    private fun createStatusBarView(context: Context, statusBarBackground: Drawable?, height: Int): View {
        val statusBarView = View(context)
        statusBarView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, height
        )
        statusBarView.background = statusBarBackground
        statusBarView.tag = STATUS_BAR_TAG
        return statusBarView
    }

    fun applyStatusBarColor(window: Window, statusBarBackground: Drawable?, isDecor: Boolean, height: Int): View {
        val parent =
            if (isDecor) window.decorView as ViewGroup else (window.findViewById<View>(android.R.id.content) as ViewGroup)
        var fakeStatusBarView = parent.findViewWithTag<View>(STATUS_BAR_TAG)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.visibility == View.GONE) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.background = statusBarBackground
        } else {
            fakeStatusBarView = createStatusBarView(window.context, statusBarBackground, height)
            parent.addView(fakeStatusBarView)
        }
        return fakeStatusBarView
    }

    override fun onPostResume() {
        super.onPostResume()
        applyEdgeToEdgeInsets(getDrawable(R.drawable.statusbar))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)
        applyEdgeToEdgeInsets(getDrawable(R.drawable.statusbar))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Log in to Kobo to continue"

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.web, true)

        binding.web.settings.javaScriptEnabled = true;
        binding.web.webViewClient = object: WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                if (!url.isNullOrBlank()){
                    Log.d("login", url);
                    val u = URL(url)

//                    if (u.path.contains("account/wishlist", ignoreCase = true)){
//                        val dialog = ProgressDialog.show(
//                            this@LoginActivity, "",
//                            "Loading. Please wait...", true
//                        ).show()
//                        Log.d("login", "loading");
//                    }
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