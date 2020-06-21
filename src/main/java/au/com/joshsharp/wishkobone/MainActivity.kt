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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var hasCookie = false
    val viewAdapter = BookAdapter(ArrayList())
    var totalPages = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewManager = LinearLayoutManager(this)

        status_text.text = "Loading..."

        list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }


        val app = application as WishkoboneApplication

        val cookie = app.getCookie()
        if (cookie != null) {
            hasCookie = true
            loadBooks()
        } else {
            doLogin()
        }


    }

    fun doLogin(){
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login);
    }

    override fun onResume() {
        super.onResume()

        if (!hasCookie) {
            val app = application as WishkoboneApplication
            val cookie = app.getCookie()
            if (cookie != null) { // we just got this via login

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
        runOnUiThread {
            status.visibility = View.VISIBLE
            status_text.text = "Loading page $page of $totalPages"
        }

        val app = application as WishkoboneApplication
        app.client.post(
            "account/wishlist/fetch",
            "{\"pageIndex\":$page}",
            "application/json",
            object :
                JsonCallback() {
                override fun onFailure(call: Call, e: IOException) {
                    hasCookie = false
                }

                override fun onResponse(call: Call, r: Response, response: JSONObject) {
                    if (!response.has("TotalNumPages")){
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
                                    authorList,
                                    obj.getString("Title"),
                                    "https:" + obj.getString("ImageUrl"),
                                    "http://www.kobo.com" + obj.getString("ProductUrl"),
                                    series,
                                    obj.getString("Synopsis"),
                                    price
                                )
                            )
                        }

                    }
                    hasCookie = true
                    runOnUiThread {
                        viewAdapter.sortBooks()
                    }

                    if (array.length() == 12) {
                        // full page, probably worth going again
                        loadBooks(page + 1)
                    } else {
                        // the end!
                        runOnUiThread {
                            status_text.text =
                                "Load complete! ${viewAdapter.books.size} items shown."
                            status.postDelayed({
                                status.visibility = View.GONE
                            }, 3000)
                        }
                    }
                }

                override fun onFailureResponse(call: Call, r: Response, response: JSONObject) {
                    hasCookie = false
                }
            });
    }

    class BookAdapter(val books: ArrayList<Book>) :
        RecyclerView.Adapter<BookAdapter.BookViewHolder>(),
        RecyclerViewFastScroller.OnPopupTextUpdate {
        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        private var filteredBooks: ArrayList<Book> = ArrayList()

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
            if (book.price != null) {
                holder.price.text = String.format("$%.2f", book.price)
            } else {
                holder.price.text = "N/A"
            }

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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(book.url))
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(it.context, intent, null)
            }
        }

        override fun getItemCount(): Int {
            return filteredBooks.size
        }

        override fun onChange(position: Int): CharSequence {
            return filteredBooks[position].formattedPrice()
        }

    }
}


