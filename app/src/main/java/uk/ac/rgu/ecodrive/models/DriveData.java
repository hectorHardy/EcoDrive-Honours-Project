package uk.ac.rgu.ecodrive.models;

import android.util.Log;

public class DriveData {
    private double score;
    private String date;

    public DriveData(double score, String date) {
        this.score = score;
        this.date = date;
        Log.d("driveData class method thingy", "DriveData: thingy123");
    }

    public double getScore() {
        return score;
    }

    public String getDate() {
        return date;
    }

    public String toString(){
        return(date + " " + score);
    }
}