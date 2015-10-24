package com.example.zach.audibooks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class BookAdapter extends BaseAdapter{
    private ArrayList<Books> books;
    private LayoutInflater bookInf;

    public BookAdapter(Context c, ArrayList<Books> theBooks){
        books = theBooks;
        bookInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLay = (LinearLayout)bookInf.inflate
                (R.layout.book, parent, false);
        //get title and artist views
        TextView titleView = (TextView)songLay.findViewById(R.id.book_title);
        TextView authorView = (TextView)songLay.findViewById(R.id.book_author);
        //get song using position
        Books currChapter = books.get(position);
        //get title and artist strings
        titleView.setText(currChapter.getTitle());
        authorView.setText(currChapter.getAuthor());

        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

}
