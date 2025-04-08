package uk.ac.rgu.ecodrive;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import uk.ac.rgu.ecodrive.models.TotalPointsCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RewardsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RewardsFragment extends Fragment implements View.OnClickListener{

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

        Button btn_reward1 = getView().findViewById(R.id.btn_reward1);
        Button btn_reward2 = getView().findViewById(R.id.btn_reward2);
        Button btn_reward3 = getView().findViewById(R.id.btn_reward3);
        Button btn_reward4 = getView().findViewById(R.id.btn_reward4);

        TextView tv_displaytotal = getView().findViewById(R.id.tv_displayTotal);

        btn_reward1.setOnClickListener(this);
        btn_reward2.setOnClickListener(this);
        btn_reward3.setOnClickListener(this);
        btn_reward4.setOnClickListener(this);

        getTotalPoints(total -> {
            Log.d("USER_SCORE", "User has total points: " + total);

            tv_displaytotal.setText("Total points: " + total);

            pb_reward1.setProgress((int)total);
            pb_reward2.setProgress((int)total);
            pb_reward3.setProgress((int)total);
            pb_reward4.setProgress((int)total);

            if(total >= pb_reward1.getMax()){
                btn_reward1.setVisibility(View.VISIBLE);
            }
            if(total >= pb_reward2.getMax()){
                btn_reward2.setVisibility(View.VISIBLE);
            }
            if(total >= pb_reward3.getMax()){
                btn_reward3.setVisibility(View.VISIBLE);
            }
            if(total >= pb_reward4.getMax()){
                btn_reward4.setVisibility(View.VISIBLE);
            }

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_reward1){
            showCustomPopup(getString(R.string.btn_reward1));
        } else if (v.getId() == R.id.btn_reward2){
            showCustomPopup(getString(R.string.btn_reward2));
        } else if (v.getId() == R.id.btn_reward3){
            showCustomPopup(getString(R.string.btn_reward3));
        } else if (v.getId() == R.id.btn_reward4){
            showCustomPopup(getString(R.string.btn_reward4));
        }
    }

    private void showCustomPopup(String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(message)
                .setTitle("Reward Unlocked")  // Optional: Set a title
                .setCancelable(false)  // Makes the dialog non-cancelable if desired
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle "OK" button click (dismiss dialog)
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

}