package com.example.zach.audibooks;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
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

        TextView titleView = (TextView) convertView.findViewById(R.id.book_title);
        TextView authorView = (TextView) convertView.findViewById(R.id.book_author);
        ImageView bookCover = (ImageView) convertView.findViewById(R.id.book_cover);
        CardView cardView = (CardView) convertView.findViewById(R.id.cv);
        NumberProgressBar numberProgressBar = (NumberProgressBar) convertView.findViewById(R.id.number_progress_bar);

        File cover = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers/" + books.title + ".jpg");
        if(cover.exists()) {
            Log.d("cover", "cover found!!");
            bookCover.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audibooks/Covers/" + books.title + ".jpg"));
        }
        else if(!books.getCoverURL().equals("")){
            bookCover.setImageBitmap(BitmapFactory.decodeFile(books.getCoverURL()));

        }
        else bookCover.setImageResource(R.drawable.default_book);

        titleView.setText(books.title);
        authorView.setText("by " + books.author);
        Log.d("percentComplete" , String.valueOf(books.percentCompleted));
        if(books.percentCompleted != 0){
            numberProgressBar.setProgress(books.percentCompleted);
        }




        return convertView;
    }

}
