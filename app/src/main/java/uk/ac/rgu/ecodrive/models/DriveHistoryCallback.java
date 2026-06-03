package uk.ac.rgu.ecodrive.models;

import java.util.ArrayList;

public interface DriveHistoryCallback {
    void onHistoryLoaded(ArrayList<DriveData> driveHistory);
    void onError(Exception e);
}
