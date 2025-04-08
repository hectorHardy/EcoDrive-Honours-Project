package uk.ac.rgu.ecodrive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rgu.ecodrive.adapters.HistoryRecyclerViewAdapter;
import uk.ac.rgu.ecodrive.adapters.LeaderboardRecyclerViewAdapter;
import uk.ac.rgu.ecodrive.models.DriveData;
import uk.ac.rgu.ecodrive.models.DriveHistoryCallback;
import uk.ac.rgu.ecodrive.models.UserData;
import uk.ac.rgu.ecodrive.models.UserDataCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LeaderboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getUserData(new UserDataCallback() {
            @Override
            public void onUserDataLoaded(ArrayList<UserData> userData) {
                Log.d("HISTORY", "Drives loaded: " + userData.size());

                RecyclerView rv = requireView().findViewById(R.id.rv_leaderboard);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                RecyclerView.Adapter adapter = new LeaderboardRecyclerViewAdapter(getContext(), userData); // sends list of drives to be displayed on recycler view
                rv.setAdapter(adapter);

            }

            @Override
            public void onError(Exception e) {
                Log.e("HISTORYFAIL", "Failed to load drive history", e);
            }
        });

    }

    public void getUserData(UserDataCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .orderBy("totalPoints", Query.Direction.DESCENDING) // Sort by totalPoints in descending order
                .limit(50) //limit to top 25 users
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UserData> leaderboard = new ArrayList<>();

                    // Loop through the results and create a User object for each
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId(); // User ID
                        double totalPoints = Math.round(document.getDouble("totalPoints")); // Get the totalPoints

                        // Add the user to the leaderboard list
                        leaderboard.add(new UserData(totalPoints, userId));
                    }

                    callback.onUserDataLoaded(leaderboard);
                    // Now you have the leaderboard in descending order of totalPoints
                    Log.d("LEADERBOARD", "Top users: " + leaderboard);

                    // You can now use the leaderboard list to update your UI (e.g., RecyclerView)
                })
                .addOnFailureListener(e -> {
                    Log.e("DATABASEFAIL", "Error fetching leaderboard", e);
                });

    }

}