package au.com.joshsharp.wishkobone

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.joshsharp.wishkobone.databinding.ActivityMainBinding
import coil.api.load
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var hasCookie = false
    val viewAdapter = BookAdapter(ArrayList())
    var totalPages = 1
    var pagesLoaded = 0
    var refreshing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val app = application as WishkoboneApplication

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Your wishlist"
        val toggle =
            ActionBarDrawerToggle(this, binding.drawerlayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerlayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        binding.navigation.setCheckedItem(R.id.home)
        binding.navigation.setNavigationItemSelectedListener {
            if (it.itemId == R.id.logout) {
                app.invalidateCookie()
                hasCookie = false
                binding.drawerlayout.closeDrawers()
                doLogin()
                return@setNavigationItemSelectedListener true
            }
            false
        }

        val viewManager = LinearLayoutManager(this)

        binding.statusText.text = "Loading..."

        binding.list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        binding.fastscroller.setHandleStateListener(object : RecyclerViewFastScroller.HandleStateListener {
            override fun onDragged(offset: Float, position: Int) {
                Log.d("scroller", "dragged: $position")
                super.onDragged(offset, position)
            }

            override fun onEngaged() {
                Log.d("scroller", "engaged")
                super.onEngaged()
            }

            override fun onReleased() {
                Log.d("scroller", "released")
                super.onReleased()
            }
        })

        binding.refresher.setOnRefreshListener {
            if (!refreshing) {
                viewAdapter.books.clear()
                viewAdapter.filteredBooks.clear()
                viewAdapter.notifyDataSetChanged()
                binding.refresher.isRefreshing = false
                loadBooks()
            }

        }

        val cookie = app.getCookie()
        if (cookie != null) {
            hasCookie = true
            loadBooks()
        } else {
            doLogin()
        }

    }

    fun doLogin() {
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login);
    }

    override fun onResume() {
        super.onResume()

        if (!hasCookie) {
            val app = application as WishkoboneApplication
            val cookie = app.getCookie()
            if (cookie != null) { // we just got this via login
                viewAdapter.books.clear()
                loadBooks()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.menu_search).actionView as SearchView).apply {
            val view = this
            // Assumes current activity is the searchable activity
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    view.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewAdapter.filter(newText)
                    return true
                }

            })
            setIconifiedByDefault(true)

            setOnCloseListener {
                viewAdapter.filter(null) // reset
                return@setOnCloseListener false
            }
        }

        return true
    }


    @SuppressLint("SetTextI18n")
    private fun loadBooks(page: Int = 1) {
        refreshing = true
        runOnUiThread {
            binding.snackbarSpinner.visibility = View.VISIBLE
            binding.status.visibility = View.VISIBLE
            if (page == 1){
                binding.statusText.text = "Loading page $page of $totalPages"
            } else {
                binding.statusText.text = "Loading remaining pages..."
            }
        }

        val app = application as WishkoboneApplication
        app.client.post(
            "account/wishlist/fetch",
            "{\"pageIndex\":$page}",
            "application/json",
            null,
            object :
                JsonCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    hasCookie = false
                    pagesLoaded += 1

                    if (pagesLoaded >= totalPages){
                        finishRefreshing()
                    }
                }

                override fun onResponse(call: Call, r: Response, response: JSONObject) {
                    if (!response.has("TotalNumPages")) {
                        // must be unauthenticated
                        hasCookie = false
                        runOnUiThread {
                            app.invalidateCookie()
                            doLogin()
                        }
                        return
                    }
                    totalPages = response.getInt("TotalNumPages")
                    val array = response.getJSONArray("Items")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)

                        val authors = obj.getJSONArray("Authors")
                        val authorList = ArrayList<String>()
                        for (j in 0 until authors.length()) {
                            authorList.add(authors.getJSONObject(j).getString("Name"))
                        }

                        val price = obj.getString("Price").substring(1).toFloatOrNull()
                        val currency = obj.getString("Price").substring(0,1)
                        var series: String? = null
                        if (obj.has("Series") && !obj.isNull("Series")) {
                            series = obj.getString("Series")
                            if (obj.has("SeriesNumber") && !obj.isNull("SeriesNumber")) {
                                series += " ${obj.getString("SeriesNumber")}"
                            }
                        }

                        runOnUiThread {
                            viewAdapter.books.add(
                                Book(
                                    obj.getString("CrossRevisionId"),
                                    authorList,
                                    obj.getString("Title"),
                                    "https:" + obj.getString("ImageUrl"),
                                    "https://www.kobo.com" + obj.getString("ProductUrl"),
                                    series,
                                    obj.getString("Synopsis"),
                                    price,
                                    currency
                                )
                            )
                        }

                    }
                    hasCookie = true
                    runOnUiThread {
                        viewAdapter.sortBooks()
                    }

                    if (page == 1) {
                        if (totalPages > 1) {

                            // let's do all the rest at once
                            var tasks: List<Job> = kotlin.collections.ArrayList()

                            for (i in 2..totalPages) {

                                tasks = tasks.plus(
                                    GlobalScope.launch {
                                        loadBooks(i)
                                    }
                                )
                            }

                            //tasks.joinAll()
                            //Log.d("load", "book tasks complete apparently")
                            //finishRefreshing()

                        }
                    }

                    // increment our counter
                    pagesLoaded += 1
                    binding.statusText.text =
                        "${viewAdapter.books.size} items loaded."

                    // call the finish method if our counter is full
                    if (pagesLoaded >= totalPages){
                        finishRefreshing()
                    }

                }

                override fun onFailureResponse(call: Call, r: Response, response: JSONObject) {
                    hasCookie = false
                    pagesLoaded += 1

                    if (pagesLoaded >= totalPages){
                        finishRefreshing()
                    }
                }
            });
    }

    fun finishRefreshing(){
        Log.d("load", "finish refreshing")
        // the end!
        runOnUiThread {
            refreshing = false
            binding.snackbarSpinner.visibility = View.INVISIBLE
            binding.statusText.text =
                "${viewAdapter.books.size} items loaded."
            binding.status.postDelayed({
                binding.status.animate()
                    .translationY(binding.status.height.toFloat())
                    .setDuration(500)
                    .withEndAction {
                        binding.status.visibility = View.GONE
                        binding.status.translationY = 0F
                    }
                    .start()

            }, 1000)
        }
    }

    class BookAdapter(val books: ArrayList<Book>) :
        RecyclerView.Adapter<BookAdapter.BookViewHolder>(),
        RecyclerViewFastScroller.OnPopupTextUpdate {
        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        var filteredBooks: ArrayList<Book> = ArrayList()

        init {
            filteredBooks.addAll(books)
        }

        class BookViewHolder(
            val title: TextView,
            val authors: TextView,
            val price: TextView,
            val series: TextView,
            val synopsis: TextView,
            val cover: ImageView,
            itemView: View
        ) : RecyclerView.ViewHolder(
            itemView
        )

        fun filter(query: String?) {
            if (query == null) {
                filteredBooks.clear()
                filteredBooks.addAll(books)

            } else {

                Log.d("filter", "looking for $query")

                filteredBooks = books.filter {
                    it.title.contains(query, true)
                            || it.authors.joinToString(", ").contains(query, true)
                            || it.series?.contains(query, true) ?: false
                } as ArrayList<Book>
            }
            notifyDataSetChanged()
        }

        fun sortBooks() {
            books.sortBy {
                if (it.price != null) {
                    it.price
                } else {
                    9999f
                }
            }
            filteredBooks.clear()
            filteredBooks.addAll(books)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.book_item, parent, false) as ViewGroup
            // set the view's size, margins, paddings and layout parameters

            return BookViewHolder(
                layout.findViewById(R.id.title), layout.findViewById(R.id.authors),
                layout.findViewById(R.id.price), layout.findViewById(R.id.series),
                layout.findViewById(R.id.synopsis), layout.findViewById(R.id.cover),
                layout
            )

        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = filteredBooks[position]
            holder.title.text = book.title
            holder.authors.text = book.authors.joinToString(", ")

            holder.price.text = book.formattedPrice()

            holder.synopsis.text = Html.fromHtml(book.synopsis).toString().trim()
            if (book.series != null) {
                holder.series.visibility = View.VISIBLE
                holder.series.text = book.series
            } else {
                holder.series.visibility = View.GONE
            }

            holder.cover.load(book.imageUrl) {
                crossfade(true)
            }

            holder.itemView.setOnClickListener {
                Log.d("click", book.url)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(book.url))
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(it.context, intent, null)
            }

        }

        private fun removeBook(app : WishkoboneApplication, book: Book, position : Int){
            val url = "https://www.kobo.com/account/wishlist/removeitem"
            val json = """{"crossRevisionId":"${book.id}"}"""

            app.client.post(url, json, "application/json", book.url, object :
                JsonCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("api", "error removing", e)
                }

                override fun onResponse(call: Call, r: Response, response: JSONObject) {
                    books.remove(book)
                    this@BookAdapter.notifyItemRemoved(position)
                }

                override fun onFailureResponse(call: Call, r: Response, response: JSONObject) {
                    super.onFailureResponse(call, r, response)
                }
            })
        }


        override fun getItemCount(): Int {
            return filteredBooks.size
        }

        override fun onChange(position: Int): CharSequence {
            Log.d("scroller", "$position")
            return filteredBooks[position].formattedPrice()
        }

    }
}


