package uk.ac.rgu.ecodrive.models;

public class UserProfile {
    private String username;

    public UserProfile() {
        // Required for Firestore
    }

    public UserProfile(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}