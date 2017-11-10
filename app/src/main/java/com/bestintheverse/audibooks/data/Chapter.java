package com.bestintheverse.audibooks.data;


public class Chapter {

    private long id;
    private String title;
    private String author;
    private String chapter;
    private int duration;
    private String path;

    public Chapter(long chapterID, String bookTitle, String bookAuthor, String chapterNum, int chapDuration, String thisPath){
        id = chapterID;
        title = bookTitle;
        author = bookAuthor;
        chapter = chapterNum;
        duration = chapDuration;
        path = thisPath;
    }
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getAuthor(){return author;}
    public String getChapter(){return chapter;}
    public int getDuration(){return duration;}
    public String getPath(){return path;}
}
