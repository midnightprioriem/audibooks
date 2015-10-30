package com.example.zach.audibooks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;


public class BookTable extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "book_positions.db";
    public static final String TABLE_NAME = "position_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "TITLE";
    public static final String COL_3 = "CHAPTER_POS";
    public static final String COL_4 = "SEEK_POSITION";
    public static final String FILE_DIR = "/Audibooks/positions";

    public BookTable(Context context) {
        super(context, Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator + DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT, CHAPTER_POS INTEGER, SEEK_POSITION INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean insertData(String title, int chapPos, int seekPos){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, title);
        contentValues.put(COL_3, chapPos);
        contentValues.put(COL_4, seekPos);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean updateData(String title, int chapPos, int seekPos, int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Log.d("content values" , title + " " + chapPos + " " +  seekPos );
        contentValues.put(COL_2, title);
        contentValues.put(COL_3, chapPos);
        contentValues.put(COL_4, seekPos);
        long result = db.update(TABLE_NAME, contentValues, COL_1 + " = " + ID, null);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor queryBooks(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] {COL_1, COL_2, COL_3, COL_4},
                "TITLE like " + "'%" + query + "%'", null, null, null, null);
        return cursor;
    }


}
