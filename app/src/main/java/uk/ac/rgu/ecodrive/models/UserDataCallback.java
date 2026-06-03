package uk.ac.rgu.ecodrive.models;

import java.util.ArrayList;

public interface UserDataCallback {
    void onUserDataLoaded(ArrayList<UserData> userData);
    void onError(Exception e);
}
