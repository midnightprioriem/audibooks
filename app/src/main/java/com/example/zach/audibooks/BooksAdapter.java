package com.example.zach.audibooks;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class BooksAdapter extends ArrayAdapter<Books> {
    public BooksAdapter(Context context, ArrayList<Books> books){
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Books books = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book,parent,false);
        }

        TextView titleView = (TextView) convertView.findViewById(R.id.book_title);
        TextView authorView = (TextView) convertView.findViewById(R.id.book_author);
        ImageView bookCover = (ImageView) convertView.findViewById(R.id.book_cover);


        File cover = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers/" + books.title + ".jpg");
        if(cover.exists()) {
            Log.d("cover", "cover found!!");
            bookCover.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audibooks/Covers/" + books.title + ".jpg"));
        }
        else if(!books.getCoverURL().equals("")){
            bookCover.setImageBitmap(BitmapFactory.decodeFile(books.getCoverURL()));

        }

        titleView.setText(books.title);
        authorView.setText(books.author);


        return convertView;
    }

}
