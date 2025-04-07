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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.ac.rgu.ecodrive.models.DriveData;
import uk.ac.rgu.ecodrive.models.DriveHistoryCallback;
import uk.ac.rgu.ecodrive.models.HistoryRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getHistory(new DriveHistoryCallback() {
            @Override
            public void onHistoryLoaded(ArrayList<DriveData> driveHistory) {
                Log.d("HISTORY", "Drives loaded: " + driveHistory.size());

                RecyclerView rv = requireView().findViewById(R.id.rv_history);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                RecyclerView.Adapter adapter = new HistoryRecyclerViewAdapter(getContext(), driveHistory); // sends list of drives to be displayed on recycler view
                rv.setAdapter(adapter);

            }

            @Override
            public void onError(Exception e) {
                Log.e("HISTORYFAIL", "Failed to load drive history", e);
            }
        });

    }

    public void getHistory(DriveHistoryCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("driveScores")
                    .orderBy("date", Query.Direction.DESCENDING) // Optional: sort by newest
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ArrayList<DriveData> driveHistory = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Double score = document.getDouble("score");
                            String date = document.getString("date");
                            driveHistory.add(new DriveData(score != null ? score : 0, date != null ? date : ""));
                        }

                        callback.onHistoryLoaded(driveHistory);
                    })
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("User not logged in"));
        }
    }
}