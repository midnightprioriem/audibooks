package com.bestintheverse.audibooks;

import java.util.ArrayList;

public class Books {
    public String title;
    public String author;
    public ArrayList<String> chapters;
    public int currentChapter;
    public int seekPos;
    public int totalDuration;
    public String coverURL;
    public int percentCompleted;

    public Books(String bookTitle, String bookAuthor, ArrayList<String> chapterList, int chapterPlace, int chapterPos, int totalDur, String url, int thisPercentCompleted){
        title = bookTitle;
        author = bookAuthor;
        chapters = chapterList;
        currentChapter = chapterPlace;
        seekPos = chapterPos;
        totalDuration = totalDur;
        coverURL = url;
        percentCompleted = thisPercentCompleted;
    }
    public String getTitle(){return title;}
    public String getAuthor(){return author;}
    public ArrayList<String> getChapters(){return chapters;}
    public int getChapterSize(){return chapters.size();}
    public int getTotalDuration(){return totalDuration;}
    public String getCoverURL(){return coverURL;}
    public int getPercentCompleted(){return percentCompleted;}
}
