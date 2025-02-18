package uk.ac.rgu.ecodrive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordDriveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordDriveFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private boolean isRecording = false; // Flag to track button state
    private Button btn_record; // Declare button globally

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RecordDriveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordDriveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordDriveFragment newInstance(String param1, String param2) {
        RecordDriveFragment fragment = new RecordDriveFragment();
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
        return inflater.inflate(R.layout.fragment_record_drive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //for initiating recording
        Button btn_record = view.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_record) { // mix of chatGPT and appDev module code
            System.out.println("record button clicked");
            isRecording = !isRecording; // Toggle state

            if (isRecording) {
                btn_record.setText(getString(R.string.btn_record_end));
                Log.d("RecordDriveFragment", "Recording Started");
                // TODO: Start recording logic
            } else {
                btn_record.setText(getString(R.string.btn_record_start));
                Log.d("RecordDriveFragment", "Recording Stopped");
                // TODO: Stop recording logic
            }

        }
    }
}