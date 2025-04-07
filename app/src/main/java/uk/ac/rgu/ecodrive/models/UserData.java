package uk.ac.rgu.ecodrive.models;

public class UserData {
    private double totalScore;
    private String userName;

    public UserData(double totalScore, String userName) {

        this.totalScore = totalScore;
        this.userName = userName;

    }

    public double getTotalScore() {
        return totalScore;
    }

    public String getUserName() {
        return userName;
    }

    public String toString(){
        return(totalScore + " " + userName);
    }
}