package com.bestintheverse.audibooks;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.os.Binder;
import android.net.Uri;


import java.util.ArrayList;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Chapter> chapters;
    private ArrayList<Books> books;
    private ArrayList<String> bookChapters;
    private int chapterPos;
    private int bookPos;
    private String bookTitle;
    private String author;
    private int chapterSize;
    private int totalDuration;
    private int percentCompleted;
    private String coverURL;
    public volatile boolean playing;

    private ServiceCallbacks serviceCallbacks;


    private final IBinder mediaBind = new MediaBinder();

    public void onCreate() {
        super.onCreate();

        bookPos = 0;
        player = new MediaPlayer();

        initMediaPlayer();
    }

    public void playBook() {
        Chapter findChapter;
        Chapter playChapter = chapters.get(0);
        player.reset();

        Books playBook = books.get(bookPos);
        bookTitle = playBook.getTitle();
        author = playBook.getAuthor();
        chapterSize = playBook.getChapterSize();
        bookChapters = playBook.getChapters();
        totalDuration = playBook.getTotalDuration();
        coverURL = playBook.coverURL;
        percentCompleted = playBook.percentCompleted;

        chapterPos = playBook.currentChapter;


        for (int i = 0; i < chapters.size(); i++) {
            findChapter = chapters.get(i);
            int chapterNum = Integer.parseInt(findChapter.getChapter());
            if (bookTitle.equals(findChapter.getTitle()) && chapterPos == chapterNum) {
                playChapter = chapters.get(i);
                break;
            }
        }

        long currChapter = playChapter.getID();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currChapter);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MEDIA SERVICE", "Error setting data source", e);
        }
        if (player != null) {
            player.prepareAsync();
        }

    }

    public void initMediaPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        playing = player.isPlaying();
        return playing;
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public String getBooktitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public int getChapter() {
        return chapterPos;
    }

    public int getBookPos() {
        return bookPos;
    }

    public ArrayList<String> getChapters() {
        return bookChapters;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getChapterSize() {
        return chapterSize;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void playPrev() {
        if (chapterPos != 1 && player.getCurrentPosition() < 10000) chapterPos--;
        books.set(bookPos, new Books(bookTitle, author, bookChapters, chapterPos, 0, totalDuration, coverURL, percentCompleted));
        playBook();
    }

    public void playNext() {
        if (chapterPos == chapterSize) {
            if (!isPlaying()) {
                go();
            }
            return;
        }
        chapterPos++;
        books.set(bookPos, new Books(bookTitle, author, bookChapters, chapterPos, 0, totalDuration, coverURL, percentCompleted));
        playBook();
    }

    public void stopPlaying() {
        books.set(bookPos, new Books(bookTitle, author, bookChapters, chapterPos, player.getCurrentPosition(), totalDuration, coverURL, percentCompleted));
        player.stop();
    }

    public void setChapterList(ArrayList<Chapter> chapterList) {
        chapters = chapterList;
    }

    public void setBookList(ArrayList<Books> bookList) {
        books = bookList;
    }

    public class MediaBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }

    public void setBook(int bookIndex) {
        bookPos = bookIndex;
    }

    @Override
    public IBinder onBind(Intent ar0) {
        return mediaBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        seek(books.get(bookPos).seekPos);
        mp.start();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (chapterPos == chapterSize) {
            stopPlaying();
        } else {
            playNext();
            if (serviceCallbacks != null) {
                serviceCallbacks.updateChapterText();
            }
        }
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }
}
