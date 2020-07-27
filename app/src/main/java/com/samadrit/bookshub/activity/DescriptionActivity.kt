package com.samadrit.bookshub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.samadrit.bookshub.R
import com.samadrit.bookshub.database.BookDatabase
import com.samadrit.bookshub.database.BookEntity
import com.samadrit.bookshub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc: TextView
    lateinit var btnAddFav: Button
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar
    var bookId: String? = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desription)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddFav = findViewById(R.id.btnAddFav)
        progressBar = findViewById(R.id.progressBarDesc)
        progressLayout = findViewById(R.id.progressBarLayoutDesc)
        toolbar = findViewById(R.id.toolbar)
        progressLayout.visibility = View.VISIBLE

        setUpToolbar()

        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error occured",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (bookId == "0") {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error occured",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"
        val postJsonObj = JSONObject()
        postJsonObj.put("book_id", bookId)
        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonRequest = object : JsonObjectRequest(Request.Method.POST, url, postJsonObj,
                Response.Listener {

                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            progressLayout.visibility = View.GONE
                            val data = it.getJSONObject("book_data")
                            val bookImageUrl = data.getString("image")
                            Picasso.get().load(bookImageUrl)
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = data.getString("name")
                            txtBookAuthor.text = data.getString("author")
                            txtBookPrice.text = data.getString("price")
                            txtBookRating.text = data.getString("rating")
                            txtBookDesc.text = data.getString("description")

                            val bookEntity = BookEntity(
                                bookId as String,
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookRating.text.toString(),
                                txtBookPrice.text.toString(),
                                bookImageUrl,
                                txtBookDesc.text.toString()
                            )

                            val isFav =
                                DBASyncTask(applicationContext, bookEntity, 1).execute().get()


                            if (isFav) {
                               addedToFav()

                            } else {
                               notFav()
                            }

                            btnAddFav.setOnClickListener {
                                if (!DBASyncTask(applicationContext, bookEntity, 1).execute().get())
                                {
                                    val result =
                                        DBASyncTask(applicationContext, bookEntity, 2).execute()
                                            .get()
                                    if(result)
                                    {

                                        Toast.makeText(applicationContext, "Added to favorites",Toast.LENGTH_SHORT).show()

                                            addedToFav()
                                    }else
                                    {
                                        Toast.makeText(applicationContext, "Removed from favorites",Toast.LENGTH_SHORT).show()
                                        notFav()
                                    }
                                } else
                                {
                                    val result = DBASyncTask(applicationContext, bookEntity, 3).execute().get()
                                    if (result)
                                    {
                                        Toast.makeText(applicationContext, "Removed from favorites",Toast.LENGTH_SHORT).show()
                                        notFav()
                                    }else
                                    {
                                        Toast.makeText(applicationContext, "Added to favorites",Toast.LENGTH_SHORT).show()

                                        addedToFav()
                                    }
                                }
                            }


                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Some Error occurred",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Some Error occured",
                            Toast.LENGTH_LONG
                        ).show()
                    }


                }, Response.ErrorListener {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Something Went Wrong in Volley",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "cad5ac075a7a98"
                    return headers
                }
            }
            queue.add(jsonRequest)
        } else {

            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Connection Error")
            dialog.setMessage("Internet Connection not found")
            dialog.setPositiveButton("Open Settings") { text, listner ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { text, listner ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"
    }

    class DBASyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        /*
        Mode 1-> Check on DB if its fav or not
        Mode 2 -> Add the book to fav
        Mode 3 -> Remove from fav from fav
         */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            when (mode) {
                1 -> {
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id)
                    db.close()
                    return book != null
                }

                2 -> {
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }


            }


            return false
        }

    }

    fun addedToFav(){
        btnAddFav.text = "Remove from favourites"
        val favColor =
            ContextCompat.getColor(applicationContext, R.color.color_fav)
        btnAddFav.setBackgroundColor(favColor)
    }

    fun notFav() {
        btnAddFav.text = "Add to Favourites"
        val noFavColor =
            ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        btnAddFav.setBackgroundColor(noFavColor)
    }

}
