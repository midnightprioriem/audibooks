package com.bestintheverse.audibooks.adapters;


import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestintheverse.audibooks.data.Books;
import com.bestintheverse.audibooks.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class BooksGridAdapter extends ArrayAdapter<Books> {
    public BooksGridAdapter(Context context, ArrayList<Books> books){
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Books books = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_grid,parent,false);
        }

        TextView titleView = (TextView) convertView.findViewById(R.id.book_title_grid);
        TextView authorView = (TextView) convertView.findViewById(R.id.book_author_grid);
        ImageView bookCover = (ImageView) convertView.findViewById(R.id.book_cover_grid);
        CardView cardView = (CardView) convertView.findViewById(R.id.cv_grid);

        File cover = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers/" + books.title + ".jpg");
        if(cover.exists()) {
            bookCover.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audibooks/Covers/" + books.title + ".jpg"));
        }
        else if(!books.getCoverURL().equals("")){
            Uri uri = Uri.fromFile(new File(books.getCoverURL()));
            Picasso.with(getContext()).load(uri).resize(450, 600).centerInside().into(bookCover);
        }
        else bookCover.setImageResource(R.drawable.default_book);

        titleView.setText(books.title);
        authorView.setText("by " + books.author);

        return convertView;
    }

}
