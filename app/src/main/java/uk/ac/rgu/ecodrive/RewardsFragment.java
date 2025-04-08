package uk.ac.rgu.ecodrive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import uk.ac.rgu.ecodrive.models.TotalPointsCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RewardsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RewardsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RewardsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RewardsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RewardsFragment newInstance(String param1, String param2) {
        RewardsFragment fragment = new RewardsFragment();
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
        return inflater.inflate(R.layout.fragment_rewards, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar pb_reward1 = getView().findViewById(R.id.pb_reward1);
        ProgressBar pb_reward2 = getView().findViewById(R.id.pb_reward2);
        ProgressBar pb_reward3 = getView().findViewById(R.id.pb_reward3);
        ProgressBar pb_reward4 = getView().findViewById(R.id.pb_reward4);

        getTotalPoints(total -> {
            Log.d("USER_SCORE", "User has total points: " + total);
            pb_reward1.setProgress((int)total);
            pb_reward2.setProgress((int)total);
            pb_reward3.setProgress((int)total);
            pb_reward4.setProgress((int)total);
        });

    }

    public void getTotalPoints(TotalPointsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double rawTotal = documentSnapshot.getDouble("totalPoints");
                            double totalPoints = (rawTotal != null && !rawTotal.isNaN()) ? rawTotal : 0.0;

                            callback.onTotalPointsRetrieved(totalPoints);
                        } else {
                            callback.onTotalPointsRetrieved(0.0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE", "Failed to fetch totalPoints", e);
                        callback.onTotalPointsRetrieved(0.0);
                    });
        } else {
            callback.onTotalPointsRetrieved(0.0);
        }
    }

}