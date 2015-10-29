package com.example.zach.audibooks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ListAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.commit451.nativestackblur.NativeStackBlur;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.wnafee.vector.MorphButton;

import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;

import android.widget.SeekBar;

import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends Activity implements MediaPlayerControl, ServiceCallbacks, FolderChooserDialog.FolderCallback {

    ListView bookView;
    GridView bookViewGrid;
    private ArrayList<Chapter> chapterList;
    private ArrayList<Books> bookList;
    private MediaService mediaSrv;



    private Intent playIntent;
    private boolean mediaBound = false;
    public SlidingUpPanelLayout slidingLayout;

    public TextView bookTitleView;
    public TextView bookAuthorView;
    public TextView chapterView;
    public TextView timeLeft;
    public TextView timeElapsed;
    public TextView durationElapsed;
    public TextView percentElapsed;
    public TextView percentElapsedOne;
    public TextView durationLeft;
    public ImageView bookCover;
    public ImageView bookCoverBlur;
    public ImageView expandButtonView;

    public ViewGroup coverBlur;

    private BooksAdapter adapter;
    private BooksGridAdapter gridAdapter;

    public LinearLayout controlLayout;

    public SwipeRefreshLayout refreshLayout;

    public MaterialRippleLayout back;
    public MaterialRippleLayout replay30;
    public MaterialRippleLayout replay10;
    public MorphButton playPauseButton;
    public MaterialRippleLayout forward10;
    public MaterialRippleLayout forward30;
    public MaterialRippleLayout next;
    public MaterialRippleLayout expandButton;
    public SeekBar seekBar;
    private Handler mHandler = new Handler();
    public int sortState = 0;

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";

    public static final String PREFS_NAME = "AudibooksPref";
    public static final String viewMode = "viewModePref";
    public static final String sortMode = "sortModePref";

    public String defaultViewMode = "listView";
    public String defaultSortMode = "byTitle";

    public String savedViewMode;
    public String savedSortMode;

    private Menu optionsMenu;




    public int FILE_CODE = 0;

    public int coverCounter = 0;

    DataBaseHelper db;

    public ProgressBar progressBar;

    public boolean isListView = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        File folder2 = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers");
        if(!folder2.exists()){
            folder2.mkdir();
        }

        //Retrieve Chapters
        chapterList = new ArrayList<>();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            getChapterListWrapper();
        else
            getChapterList();
        
        Collections.sort(chapterList, new Comparator<Chapter>() {
            @Override
            public int compare(Chapter lhs, Chapter rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        bookList = new ArrayList<>();
        getBookList(chapterList, bookList);
        getBookPositions();
        bookView = (ListView) findViewById(R.id.bookView);
        bookViewGrid = (GridView) findViewById(R.id.bookViewGrid);


        adapter = new BooksAdapter(this, bookList);
        bookView.setAdapter(adapter);
        gridAdapter = new BooksGridAdapter(this, bookList);
        bookViewGrid.setAdapter(gridAdapter);


        //GET PREFERENCES

        savedViewMode = prefs.getString(viewMode, defaultViewMode);
        savedSortMode = prefs.getString(sortMode, defaultSortMode);

        if(savedSortMode.equals("byAuthor")){
            Collections.sort(bookList, new Comparator<Books>() {
                @Override
                public int compare(Books lhs, Books rhs) {
                    return lhs.getAuthor().compareTo(rhs.getAuthor());
                }
            });
            adapter.notifyDataSetChanged();
            gridAdapter.notifyDataSetChanged();
            sortState = 1;
        }

        if(savedViewMode.equals("listView")){
            bookViewGrid.setVisibility(View.GONE);
            bookView.setVisibility(View.VISIBLE);
            isListView = true;
        }

        if(savedViewMode.equals("gridView")){
            bookView.setVisibility(View.GONE);
            bookViewGrid.setVisibility(View.VISIBLE);
            isListView = false;
        }

        //END SHARED PREFERENCES




        setButtons();

        bookView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), getChapters(), getChapterPos(), getCurrentPosition(), getTotalDuration(), getCoverURL(), getPercentCompleted()));
                }
                mediaSrv.setBook(position);
                mediaSrv.playBook();
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                bookCover = (ImageView) findViewById(R.id.main_cover);
                bookCoverBlur = (ImageView) findViewById(R.id.main_cover_blur);
                File cover = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers/" + bookList.get(position).title + ".jpg");
                if (cover.exists()) {
                    Log.d("cover", "cover found!!");
                    bookCover.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audibooks/Covers/" + bookList.get(position).title + ".jpg"));
                } else if (!bookList.get(position).getCoverURL().equals("")) {
                    Uri uri = Uri.fromFile(new File(bookList.get(position).getCoverURL()));
                    Picasso.with(getApplicationContext()).load(uri).resize(600, 800).centerInside().into(bookCover);
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 5;
                    Bitmap bitmap = NativeStackBlur.process(BitmapFactory.decodeFile(bookList.get(position).getCoverURL(), opts), 75);
                    bookCoverBlur.setImageBitmap(bitmap);
                    //bookCoverBlur.setColorFilter(Color.parseColor("#0f0f0f"), PorterDuff.Mode.MULTIPLY);


                }


                bookTitleView = (TextView) findViewById(R.id.Book_Title_View);
                bookTitleView.setText(mediaSrv.getBooktitle());
                bookAuthorView = (TextView) findViewById(R.id.Book_Author_View);
                bookAuthorView.setText(mediaSrv.getAuthor());
                chapterView = (TextView) findViewById(R.id.Chapter_View);
                chapterView.setText("Chapter " + mediaSrv.getChapter() + " of " + mediaSrv.getChapterSize());
                timeLeft = (TextView) findViewById(R.id.timeLeft);
                timeElapsed = (TextView) findViewById(R.id.timeElapsed);
                durationElapsed = (TextView) findViewById(R.id.totalDuration);
                percentElapsed = (TextView) findViewById(R.id.percentElapsed);
                percentElapsedOne = (TextView) findViewById(R.id.percentElapsedOne);
                durationLeft = (TextView) findViewById(R.id.durationLeft);
                controlLayout = (LinearLayout) findViewById(R.id.controlLayout);
                int titleHeight = bookTitleView.getHeight();
                int authorHeight = bookAuthorView.getHeight();
                slidingLayout.setPanelHeight(titleHeight + authorHeight);


                playPauseButton.setState(MorphButton.MorphState.START, true);

                if (mediaSrv.getChapterSize() == 1) {
                    progressBar.setVisibility(View.INVISIBLE);
                    durationElapsed.setVisibility(View.INVISIBLE);
                    percentElapsed.setVisibility(View.INVISIBLE);
                    durationLeft.setVisibility(View.INVISIBLE);
                    percentElapsedOne.setVisibility(View.VISIBLE);

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    durationElapsed.setVisibility(View.VISIBLE);
                    percentElapsed.setVisibility(View.VISIBLE);
                    durationLeft.setVisibility(View.VISIBLE);
                    percentElapsedOne.setVisibility(View.INVISIBLE);
                }


            }
        });

        bookViewGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), getChapters(), getChapterPos(), getCurrentPosition(), getTotalDuration(), getCoverURL(), getPercentCompleted()));
                }

                mediaSrv.setBook(position);
                mediaSrv.playBook();
                bookCover = (ImageView) findViewById(R.id.main_cover);
                bookCoverBlur = (ImageView) findViewById(R.id.main_cover_blur);
                File cover = new File(Environment.getExternalStorageDirectory() + "/Audibooks/Covers/" + bookList.get(position).title + ".jpg");
                if (cover.exists()) {
                    Log.d("cover", "cover found!!");
                    bookCover.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audibooks/Covers/" + bookList.get(position).title + ".jpg"));
                } else if (!bookList.get(position).getCoverURL().equals("")) {
                    Uri uri = Uri.fromFile(new File(bookList.get(position).getCoverURL()));
                    Picasso.with(getApplicationContext()).load(uri).resize(600, 800).centerInside().into(bookCover);
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 7;
                    Bitmap bitmap = NativeStackBlur.process(BitmapFactory.decodeFile(bookList.get(position).getCoverURL(), opts), 70);
                    bookCoverBlur.setImageBitmap(bitmap);
                    bookCoverBlur.setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY);


                }


                bookTitleView = (TextView) findViewById(R.id.Book_Title_View);
                bookTitleView.setText(mediaSrv.getBooktitle());
                bookAuthorView = (TextView) findViewById(R.id.Book_Author_View);
                bookAuthorView.setText(mediaSrv.getAuthor());
                chapterView = (TextView) findViewById(R.id.Chapter_View);
                chapterView.setText("Chapter " + mediaSrv.getChapter() + " of " + mediaSrv.getChapterSize());
                timeLeft = (TextView) findViewById(R.id.timeLeft);
                timeElapsed = (TextView) findViewById(R.id.timeElapsed);
                durationElapsed = (TextView) findViewById(R.id.totalDuration);
                percentElapsed = (TextView) findViewById(R.id.percentElapsed);
                percentElapsedOne = (TextView) findViewById(R.id.percentElapsedOne);
                durationLeft = (TextView) findViewById(R.id.durationLeft);
                controlLayout = (LinearLayout) findViewById(R.id.controlLayout);

                int titleHeight = bookTitleView.getHeight();
                int authorHeight = bookAuthorView.getHeight();
                slidingLayout.setPanelHeight(titleHeight + authorHeight);


                playPauseButton.setState(MorphButton.MorphState.START, true);



                if (mediaSrv.getChapterSize() == 1) {
                    progressBar.setVisibility(View.INVISIBLE);
                    durationElapsed.setVisibility(View.INVISIBLE);
                    percentElapsed.setVisibility(View.INVISIBLE);
                    durationLeft.setVisibility(View.INVISIBLE);
                    percentElapsedOne.setVisibility(View.VISIBLE);

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    durationElapsed.setVisibility(View.VISIBLE);
                    percentElapsed.setVisibility(View.VISIBLE);
                    durationLeft.setVisibility(View.VISIBLE);
                    percentElapsedOne.setVisibility(View.INVISIBLE);
                }

                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        });


        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        expandButtonView = (ImageView) findViewById(R.id.expand_button_view);

        slidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
                expandButtonView.setImageResource(R.drawable.expand_more_button);
            }

            @Override
            public void onPanelExpanded(View view) {
                expandButtonView.setImageResource(R.drawable.expand_less_button);
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });


        seekBar = (SeekBar) findViewById(R.id.seekBar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaSrv != null && mediaBound) {
                    if(isPlaying()){
                        seekBar.setMax(mediaSrv.getDur());
                        int currentPosition = mediaSrv.getPosn();
                        seekBar.setProgress(currentPosition);
                        String timeElapsedOutput = getTimeElapsed(currentPosition);
                        String timeLeftOutput = getTimeLeftOutput(getTimeLeft(currentPosition));
                        timeElapsed.setText(timeElapsedOutput);
                        timeLeft.setText(timeLeftOutput);
                        setProgressBar(currentPosition);
                    }

                }
                mHandler.postDelayed(this, 10);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean playing = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaSrv != null && fromUser) {
                    mediaSrv.seek(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!isPlaying()) {
                    mediaSrv.go();
                    playing = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!playing) {
                    pause();
                }
                playing = true;
            }
        });

    }

    private ServiceConnection mediaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaService.MediaBinder binder = (MediaService.MediaBinder) service;
            mediaSrv = binder.getService();
            mediaSrv.setChapterList(chapterList);
            mediaSrv.setBookList(bookList);
            mediaBound = true;
            mediaSrv.setCallbacks(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MediaService.class);
            bindService(playIntent, mediaConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    public void getBookList(ArrayList<Chapter> chapters, ArrayList<Books> books){
        Chapter currChapter;
        Books currBook;
        int totalDur = 0;
        int chapterPos = 1;
        int currentPos = 0;
        Books:
        for (int i = 0; i < chapterList.size(); i++) {
            currChapter = chapters.get(i);
            String bTitle = currChapter.getTitle();
            String bAuthor = currChapter.getAuthor();
            String path = currChapter.getPath();
            String coverPath = "";
            ArrayList<String> bookChapters = new ArrayList<>();
            for(int j = i; j < chapterList.size(); j++){
                currChapter = chapters.get(j);
                if (bTitle.equals(currChapter.getTitle())) {
                    bookChapters.add(currChapter.getChapter());
                    totalDur = totalDur + currChapter.getDuration();

                }
            }
            for(int c = 0; c < bookList.size(); c++) {
                currBook = books.get(c);
                if (bTitle.equals(currBook.getTitle())) {
                    totalDur = 0;
                    continue Books;

                }
            }
            ContentResolver mediaResolver = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = mediaResolver.query(uri, null, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
            if(cursor !=null && cursor.moveToFirst()){
                int coverID = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int coverColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                coverPath = cursor.getString(coverColumn);
                cursor.close();
            }

            bookList.add(new Books(bTitle, bAuthor, bookChapters, chapterPos, currentPos, totalDur, coverPath, 0));
            totalDur = 0;
        }

    }

    public void getChapterList(){
        ContentResolver musicResolver = getContentResolver();
        Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor mediaCursor = musicResolver.query(mediaUri, null, MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%Audibooks%"}, null);
        if(mediaCursor != null && mediaCursor.moveToFirst()){
            int titleColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int idColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int authorColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int chapterColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
            int durationColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathColumn = mediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                long thisId = mediaCursor.getLong(idColumn);
                String thisTitle = mediaCursor.getString(titleColumn);
                String thisAuthor = mediaCursor.getString(authorColumn);
                String thisChapter = mediaCursor.getString(chapterColumn);
                String thisPath = mediaCursor.getString(pathColumn);
                thisPath = thisPath.substring(0, thisPath.lastIndexOf("/") + 1);
                int thisDuration = Integer.parseInt(mediaCursor.getString(durationColumn));
                chapterList.add(new Chapter(thisId, thisTitle, thisAuthor, thisChapter, thisDuration, thisPath));
            }
            while (mediaCursor.moveToNext());
        }
        if(mediaCursor != null){
            mediaCursor.close();
        }
    }

    public void getBookCovers(){

        if(coverCounter == bookList.size())
            return;
        Books book;
        book = bookList.get(coverCounter);
        queryBooks(book.title + " " + book.author);

    }

    private void queryBooks(final String searchString) {

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Have the client get a JSONArray of data
        // and define how to respond
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        Log.d("omg android", jsonObject.toString());
                        setBookCover(jsonObject);

                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                        coverCounter++;
                        getBookCovers();
                    }
                });
    }

    public void setBookCover(JSONObject jsonObject){

        JSONArray jsonArray = jsonObject.optJSONArray("docs");
        JSONObject jsonBook = jsonArray.optJSONObject(0);
        if(jsonBook != null){
            for(int i=0; i < jsonArray.length(); i ++){
                jsonBook = jsonArray.optJSONObject(i);
                if(jsonBook.has("cover_i")){
                    Log.d("json", jsonBook.toString());
                    String imageID = jsonBook.optString("cover_i");
                    String imageURL = IMAGE_URL_BASE + imageID + "-L.jpg";
                    Books book;
                    book = bookList.get(coverCounter);
                    final String bookTitle = book.title;
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    File file = new File(
                                            Environment.getExternalStorageDirectory().getPath()
                                                    + "/Audibooks/Covers/" + bookTitle + ".jpg");
                                    try {
                                        file.createNewFile();
                                        FileOutputStream ostream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,ostream);
                                        ostream.close();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {}

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {}
                    };

                    Picasso.with(getApplicationContext()).load(imageURL).error(R.drawable.default_book).into(target);
                }
            }


            adapter.notifyDataSetChanged();


        }
        coverCounter++;
        getBookCovers();
    }

    public void playNext(){
        mediaSrv.playNext();
        updateChapterText();
        if (playPauseButton.getState() == MorphButton.MorphState.END) {
            playPauseButton.setState(MorphButton.MorphState.START, true);
        }

    }

    public void playPrev(){
        mediaSrv.playPrev();
        updateChapterText();
        if(playPauseButton.getState() == MorphButton.MorphState.END){
            playPauseButton.setState(MorphButton.MorphState.START, true);
        }
    }

    @Override
    public void updateChapterText() {
        chapterView.setText("Chapter " + mediaSrv.getChapter() + " of " + mediaSrv.getChapterSize());

    }

    @Override
    public void start() {
        mediaSrv.go();
        playPauseButton.setState(MorphButton.MorphState.START, true);

    }

    @Override
    public void pause() {
        mediaSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(mediaSrv!=null && mediaBound && mediaSrv.getDur() != 0 )
            return mediaSrv.getDur();
        else return 0;
    }

    public int getTotalDuration(){
        return mediaSrv.getTotalDuration();
    }

    @Override
    public int getCurrentPosition() {
        if(mediaSrv!=null && mediaBound)
            return mediaSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        mediaSrv.seek(pos);

    }

    public String getTimeElapsed(int currentPosition){
        int daysElapsed = (currentPosition / (1000*60*60*24)) % 364;
        int hoursElapsed = (currentPosition / (1000*60*60)) %24;
        int minutesElapsed = (currentPosition / (1000*60)) % 60;
        int secondsElapsed = (currentPosition / 1000) % 60;

        return formatTime(daysElapsed, hoursElapsed, minutesElapsed, secondsElapsed);
    }

    public int getTimeLeft(int currentPosition){
        return getDuration() - currentPosition;
    }

    public String getTimeLeftOutput(int durLeft){
        int daysLeft = (durLeft / (1000*60*60*24)) % 364;
        int hoursLeft = (durLeft / (1000*60*60)) %24;
        int minutesLeft = (durLeft / (1000*60)) % 60;
        int secondsLeft = (durLeft / 1000) % 60;

        return formatTime(daysLeft, hoursLeft, minutesLeft, secondsLeft);

    }

    public int getDurationElapsed(int currentPosition){
        int currChap = mediaSrv.getChapter();
        int durElapsed = 0;
        for(int i =0; i < chapterList.size(); i++){
            Chapter chapter = chapterList.get(i);
            if(getBookTitle().equals(chapter.getTitle()) && currChap > Integer.parseInt(chapter.getChapter())){
                durElapsed = durElapsed + chapter.getDuration();
            }

        }
        durElapsed = durElapsed + currentPosition;
        return durElapsed;

    }

    public String getDurationElapsedOutput(int durElapsed){
        int daysElapsed = (durElapsed / (1000*60*60*24)) % 364;
        int hoursElapsed = (durElapsed / (1000*60*60)) %24;
        int minutesElapsed = (durElapsed / (1000*60)) % 60;
        int secondsElapsed = (durElapsed / 1000) % 60;

        return formatTime(daysElapsed, hoursElapsed, minutesElapsed, secondsElapsed);
    }

    public int getDurationLeft(int currentPosition){
        return getTotalDuration() - getDurationElapsed(currentPosition);
    }

    public String getDurationLeftOutput(int durLeft){
        int daysLeft = (durLeft / (1000*60*60*24)) % 364;
        int hoursLeft = (durLeft / (1000*60*60)) %24;
        int minutesLeft = (durLeft / (1000*60)) % 60;
        int secondsLeft = (durLeft / 1000) % 60;

        return formatTime(daysLeft, hoursLeft, minutesLeft, secondsLeft);

    }

    public void setProgressBar(int currentPosition){
        int durElapsed = getDurationElapsed(currentPosition);
        int totalDur = getTotalDuration();
        int durLeft = getDurationLeft(currentPosition);
        double dDurElapsed = (double) durElapsed;
        double dTotalDur = (double) totalDur;
        String durationElapsedOutput = getDurationElapsedOutput(durElapsed);
        String durationLeftOutput = getDurationLeftOutput(durLeft);
        durationElapsed.setText(durationElapsedOutput + " of " + getDurationElapsedOutput(totalDur));
        durationLeft.setText(durationLeftOutput);
        double rElapsed = (dDurElapsed/dTotalDur)*100;
        int pElapsed = (int) rElapsed;
        progressBar.setProgress(pElapsed);
        percentElapsed.setText(String.valueOf(pElapsed + "%" + " Read"));
        percentElapsedOne.setText(String.valueOf(pElapsed + "%" + " Read"));


    }

    public String formatTime(int days, int hours, int minutes, int seconds){
        String timeOutput;
        if(days != 0){
            timeOutput = String.format("%02d:%02d:%02d:%02d",
                    days, hours, minutes, seconds);
        }
        else if(hours != 0){
            timeOutput = String.format("%02d:%02d:%02d",
                    hours, minutes, seconds);
        }
        else{
            timeOutput = String.format("%02d:%02d",
                    minutes, seconds);
        }
        return timeOutput;
    }

    public void setButtons() {
        back = (MaterialRippleLayout) findViewById(R.id.back);
        replay10 = (MaterialRippleLayout) findViewById(R.id.replay_10);
        replay30 = (MaterialRippleLayout) findViewById(R.id.replay_30);
        playPauseButton = (MorphButton) findViewById(R.id.playPauseButton);
        forward10 = (MaterialRippleLayout) findViewById(R.id.forward_10);
        forward30 = (MaterialRippleLayout) findViewById(R.id.forward_30);
        next = (MaterialRippleLayout) findViewById(R.id.next);
        bookCoverBlur = (ImageView) findViewById(R.id.main_cover_blur);
        bookCover = (ImageView) findViewById(R.id.main_cover);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentPosition() < 100000) {
                    playPrev();
                } else {
                    seekTo(0);
                }
            }
        });

        replay10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    start();
                }
                if (getCurrentPosition() > 10000) {
                    seekTo(getCurrentPosition() - 10000);
                } else if (getChapterPos() > 1) {
                    int timeRemainder = 10000 - getCurrentPosition();
                    int newChapter = getChapterPos() - 1;
                    int seekPos = 5000;
                    Books book = bookList.get(getBookPos());
                    Chapter chapter;
                    for (int i = 0; i < chapterList.size(); i++) {
                        chapter = chapterList.get(i);
                        int chapterNum = Integer.parseInt(chapter.getChapter());
                        if (getBookTitle().equals(chapter.getTitle()) && newChapter == chapterNum) {
                            seekPos = chapter.getDuration() - timeRemainder;
                            break;
                        }

                    }
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), book.getChapters(), newChapter, seekPos, getTotalDuration(), book.coverURL, book.percentCompleted));
                    mediaSrv.playBook();
                    updateChapterText();
                } else {
                    seekTo(0);
                }
            }
        });

        replay30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    start();
                }
                if (getCurrentPosition() > 30000) {
                    seekTo(getCurrentPosition() - 30000);
                } else if (getChapterPos() > 1) {
                    int timeRemainder = 30000 - getCurrentPosition();
                    int newChapter = getChapterPos() - 1;
                    int seekPos = 5000;
                    Books book = bookList.get(getBookPos());
                    Chapter chapter;
                    for (int i = 0; i < chapterList.size(); i++) {
                        chapter = chapterList.get(i);
                        int chapterNum = Integer.parseInt(chapter.getChapter());
                        if (getBookTitle().equals(chapter.getTitle()) && newChapter == chapterNum) {
                            seekPos = chapter.getDuration() - timeRemainder;
                            break;
                        }

                    }
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), book.getChapters(), newChapter, seekPos, getTotalDuration(), book.coverURL, book.percentCompleted));
                    mediaSrv.playBook();
                    updateChapterText();
                } else {
                    seekTo(0);
                }
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaSrv != null && mediaBound && mediaSrv.isPlaying()) {
                    playPauseButton.setState(MorphButton.MorphState.END, true);
                    pause();
                } else if (mediaSrv != null && mediaBound) {
                    playPauseButton.setState(MorphButton.MorphState.START, true);
                    start();
                }
            }
        });

        bookCoverBlur.setSoundEffectsEnabled(false);
        bookCoverBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bookCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaSrv != null && mediaBound && mediaSrv.isPlaying()) {
                    playPauseButton.setState(MorphButton.MorphState.END, true);
                    pause();
                } else if (mediaSrv != null && mediaBound) {
                    playPauseButton.setState(MorphButton.MorphState.START, true);
                    start();
                }
            }
        });

        forward10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    start();
                }
                if ((getDuration() - getCurrentPosition()) > 10000) {
                    seekTo(getCurrentPosition() + 10000);
                } else if (getChapterPos() < bookList.get(getBookPos()).getChapterSize()) {
                    int seekPos = 10000 - (getDuration() - getCurrentPosition());
                    int newChapter = getChapterPos() + 1;
                    Books book = bookList.get(getBookPos());
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), book.getChapters(), newChapter, seekPos, getTotalDuration(), book.coverURL, book.percentCompleted));
                    mediaSrv.playBook();
                    updateChapterText();
                }

            }
        });

        forward30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    start();
                }
                if ((getDuration() - getCurrentPosition()) > 30000) {
                    seekTo(getCurrentPosition() + 30000);
                } else if (getChapterPos() < bookList.get(getBookPos()).getChapterSize()) {
                    int seekPos = 30000 - (getDuration() - getCurrentPosition());
                    int newChapter = getChapterPos() + 1;
                    Books book = bookList.get(getBookPos());
                    bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), book.getChapters(), newChapter, seekPos, getTotalDuration(), book.coverURL, book.percentCompleted));
                    mediaSrv.playBook();
                    updateChapterText();
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });


    }

    @Override
    public boolean isPlaying() {
        if (mediaSrv!=null && mediaBound)
            return mediaSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(savedViewMode.equals("listView")){
            menu.findItem(R.id.view_button).setIcon(R.drawable.view_grid);
        }

        if(savedViewMode.equals("gridView")){
            menu.findItem(R.id.view_button).setIcon(R.drawable.view_list);
        }

        return super.onCreateOptionsMenu(menu);
    }


    public String getBookTitle(){return mediaSrv.getBooktitle();}
    public String getBookAuthor(){return mediaSrv.getAuthor();}
    public ArrayList<String> getChapters(){return mediaSrv.getChapters();}
    public int getChapterPos(){return mediaSrv.getChapter();}
    public int getBookPos(){return mediaSrv.getBookPos();}
    public String getCoverURL(){return mediaSrv.getCoverURL();}
    public int getPercentCompleted(){return mediaSrv.getPercentCompleted();}

    public void getBookPositions() {
        Books books;
        db = new DataBaseHelper(this);
        String bookTitle;
        int chapPos;
        int seekPos;
        int durationElapsed = 0;
        for(int i=0; i < bookList.size(); i++){
            books = bookList.get(i);
            String titleQuery = books.title;
            if(books.title.contains("'")){
                titleQuery = titleQuery.replaceAll("'" , "''");
            }
            Cursor cursor = db.queryBooks(titleQuery);
            if (cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                bookTitle = cursor.getString(1);
                chapPos = cursor.getInt(2);
                seekPos = cursor.getInt(3);
                Log.d("Database" , bookTitle + " " + chapPos + " " + seekPos);

                if(books.title.equals(bookTitle)) {
                    for (int k = 0; k < chapterList.size(); k++) {
                        Chapter chapter = chapterList.get(k);
                        if (bookTitle.equals(chapter.getTitle()) && chapPos > Integer.parseInt(chapter.getChapter())) {
                            durationElapsed = durationElapsed + chapter.getDuration();
                        }

                    }
                    double dDurElapsed = (double) durationElapsed;
                    double dTotalDur = (double) books.totalDuration;
                    double rElapsed = (dDurElapsed / dTotalDur) * 100;
                    int pElapsed = (int) rElapsed;
                    bookList.set(i, new Books(books.title, books.author, books.chapters, chapPos, seekPos, books.totalDuration, books.coverURL, pElapsed));
                    cursor.moveToNext();
                }
            }
            durationElapsed = 0;

        }

    }

    public void writeBookPositions(){
        db = new DataBaseHelper(this);
        Books books;
        for(int i = 0; i < bookList.size(); i++ ){
            books = bookList.get(i);
            String titleQuery = books.title;
            Log.d("WRITE BOOKS" , books.title);
            if(books.currentChapter == 1 && books.seekPos == 0){
                continue;
            }
            if(books.title.contains("'")){
                titleQuery = titleQuery.replaceAll("'" , "''");
            }
            Cursor cursor = db.queryBooks(titleQuery);
            if (cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                int id = cursor.getInt(0);
                boolean result = db.updateData(books.title, books.currentChapter, books.seekPos, id);
                if(!result){
                    Log.d("DB" , "FAILED TO UPDATE DATA");
                }
            }
            else {
                db.insertData(books.title, books.currentChapter, books.seekPos);
            }

        }

    }



    @Override
    protected void onStop() {
        super.onStop();
        if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
            bookList.set(getBookPos(), new Books(getBookTitle(), getBookAuthor(), getChapters(), getChapterPos(), getCurrentPosition(), getTotalDuration(), getCoverURL(), getPercentCompleted()));
        }
        writeBookPositions();
       
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.onStop();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(viewMode , savedViewMode );
        editor.putString(sortMode , savedSortMode);
        editor.commit();
        unbindService(mediaConnection);
    }

    @SuppressLint("NewApi")
    private void getChapterListWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessageOKCancel("You need to allow access to Contacts",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                        123);
                            }
                        });
                return;
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
            return;
        }
        getChapterList();

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @SuppressLint("NewApi")
    private void getBookCoversWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.INTERNET);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                showMessageOKCancel("You need to allow access to the Internet",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[] {Manifest.permission.INTERNET},
                                        123);
                            }
                        });
                return;
            }
            requestPermissions(new String[]{Manifest.permission.INTERNET},
                    123);
            return;
        }
        getBookCovers();

    }
    public void startCustomDirectoryActivity(){

        Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, FILE_CODE);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                        }
                    }

                } else {
                    Uri uri = data.getData();
                    // Do something with the URI
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_button:
                if(isListView){
                    item.setIcon(R.drawable.view_list);
                    bookView.setVisibility(View.GONE);
                    bookViewGrid.setVisibility(View.VISIBLE);
                    isListView = false;
                    savedViewMode = "gridView";
                }
                else {
                    item.setIcon(R.drawable.view_grid);
                    bookViewGrid.setVisibility(View.GONE);
                    bookView.setVisibility(View.VISIBLE);
                    isListView = true;
                    savedViewMode = "listView";
                }

                return true;

            case R.id.sort_button:
                new MaterialDialog.Builder(this)
                        .title(R.string.sort_dialog_title)
                        .items(R.array.sort_array)
                        .theme(Theme.LIGHT)
                        .itemsCallbackSingleChoice(sortState, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                Log.d("sort", String.valueOf(which));

                                if (which == 0) {
                                    Log.d("sort", "Sorting by title");
                                    Collections.sort(bookList, new Comparator<Books>() {
                                        @Override
                                        public int compare(Books lhs, Books rhs) {
                                            return lhs.getTitle().compareTo(rhs.getTitle());
                                        }
                                    });
                                    adapter.notifyDataSetChanged();
                                    gridAdapter.notifyDataSetChanged();
                                    sortState = 0;
                                    savedSortMode = "byTitle";
                                } else if (which == 1) {

                                    Log.d("sort", "Sorting by Author");
                                    Collections.sort(bookList, new Comparator<Books>() {
                                        @Override
                                        public int compare(Books lhs, Books rhs) {
                                            return lhs.getAuthor().compareTo(rhs.getAuthor());
                                        }
                                    });
                                    adapter.notifyDataSetChanged();
                                    gridAdapter.notifyDataSetChanged();
                                    sortState = 1;
                                    savedSortMode = "byAuthor";
                                }

                                return true;
                            }
                        })
                        .show();

            case R.id.folder_chooser_button:

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else
            moveTaskToBack(true);

    }


    @Override
    public void onFolderSelection(File file) {

    }
}


