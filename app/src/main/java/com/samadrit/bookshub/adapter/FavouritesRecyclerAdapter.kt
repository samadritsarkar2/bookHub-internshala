package com.samadrit.bookshub.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.samadrit.bookshub.R
import com.samadrit.bookshub.activity.DescriptionActivity
import com.samadrit.bookshub.database.BookEntity
import com.squareup.picasso.Picasso

class FavouritesRecyclerAdapter(val context : Context, val bookList: List<BookEntity>) :
    RecyclerView.Adapter<FavouritesRecyclerAdapter.FavouriteViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_favourite_single_row, parent, false)

        return FavouriteViewHolder(view)

    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val book = bookList[position]

        holder.txtFavBookName.text = book.bookName
        holder.txtFavBookAuthor.text = book.bookAuthor
        holder.txtFavBookPrice.text =book.bookPrice
        holder.txtFavBookRating.text= book.bookRating
        Picasso.get().load(book.bookImage).error(R.drawable.default_book_cover).into(holder.txtFavBookImage)

        holder.recyclerLayout.setOnClickListener {
            val intent = Intent(context, DescriptionActivity::class.java)
            intent.putExtra("book_id", book.book_id)
            context.startActivity(intent)
        }

    }

    class FavouriteViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val recyclerLayout : CardView = view.findViewById(R.id.favRecycler)
        val txtFavBookName : TextView = view.findViewById(R.id.txtFavBookName)
        val txtFavBookAuthor : TextView = view.findViewById(R.id.txtFavBookAuthor)
        val txtFavBookPrice : TextView = view.findViewById(R.id.txtFavBookPrice)
        val txtFavBookRating : TextView = view.findViewById(R.id.txtFavBookRating)
        val txtFavBookImage : ImageView = view.findViewById(R.id.imgFavBookImage)
        val llFavContent : LinearLayout= view.findViewById(R.id.llFavContent)


    }

}