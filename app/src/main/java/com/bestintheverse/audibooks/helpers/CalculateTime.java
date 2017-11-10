package com.bestintheverse.audibooks.helpers;

import com.bestintheverse.audibooks.services.MediaService;
import com.bestintheverse.audibooks.activities.MainActivity;
import com.bestintheverse.audibooks.data.Chapter;

import java.util.ArrayList;

public class CalculateTime {

    private MediaService mediaSrv;
    private MainActivity mActivity;
    private ArrayList<Chapter> chapterList;

    public void calculateTime(MediaService mService, MainActivity mainActivity, ArrayList<Chapter> chapters){
        mediaSrv = mService;
        mActivity = mainActivity;
        chapterList = chapters;
    }

    public String getTimeElapsed(int currentPosition){
        int daysElapsed = (currentPosition / (1000*60*60*24)) % 364;
        int hoursElapsed = (currentPosition / (1000*60*60)) %24;
        int minutesElapsed = (currentPosition / (1000*60)) % 60;
        int secondsElapsed = (currentPosition / 1000) % 60;

        return formatTime(daysElapsed, hoursElapsed, minutesElapsed, secondsElapsed);
    }

    public int getTimeLeft(int currentPosition){
        return mActivity.getDuration() - currentPosition;
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
            if(mActivity.getBookTitle().equals(chapter.getTitle()) && currChap > Integer.parseInt(chapter.getChapter())){
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
        return mActivity.getTotalDuration() - getDurationElapsed(currentPosition);
    }

    public String getDurationLeftOutput(int durLeft){
        int daysLeft = (durLeft / (1000*60*60*24)) % 364;
        int hoursLeft = (durLeft / (1000*60*60)) %24;
        int minutesLeft = (durLeft / (1000*60)) % 60;
        int secondsLeft = (durLeft / 1000) % 60;

        return formatTime(daysLeft, hoursLeft, minutesLeft, secondsLeft);

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

}
