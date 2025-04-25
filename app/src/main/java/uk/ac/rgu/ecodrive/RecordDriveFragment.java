package uk.ac.rgu.ecodrive;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.rgu.ecodrive.api.OverpassApiService;
import uk.ac.rgu.ecodrive.api.RetrofitClient;
import uk.ac.rgu.ecodrive.models.OverpassResponse;
import uk.ac.rgu.ecodrive.models.DriveData;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordDriveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordDriveFragment extends Fragment implements View.OnClickListener, SensorEventListener {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private boolean isRecording = false; // Flag to track button state
    private Button btn_record; // Declare button globally
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView txt_accX;
    private TextView txt_accY;
    private TextView txt_accZ;
    private Switch sw_startStop;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double[] location_temp = new double[3];
    private int speedingCount, idleCount, accelerationCount = 0;
    private boolean isRunning = false;
    private long startTime, endTime;
    private long totalTime;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private CollectionReference driveScoresRef = db.collection("users").document(userId).collection("driveScores");
    private final double IDLESPEED = 1.5;
    private final double ACCELERATIONLIMIT = 8;
    private double driveScore;
    private final double WEIGHTSPEED = 10;
    private final double WEIGHTIDLE = 3;
    private final double WEIGHTACC = 5;
    private static final long COOLDOWN = 2000; // 2 seconds
    private long lastAccelerationTime = 0;

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


    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        FirebaseFirestore db = FirebaseFirestore.getInstance(); //get database instance
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); //get userID
        CollectionReference driveScoresRef = db.collection("users").document(userId).collection("driveScores"); // specific reference to the driveScores collection for saving recorded drive data to.

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission using Activity Result API
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_drive, container, false);


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //initialise views
        btn_record = view.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(this);

        sw_startStop = view.findViewById(R.id.sw_startStop);

        txt_accX = view.findViewById(R.id.txt_accX);
        txt_accY = view.findViewById(R.id.txt_accY);
        txt_accZ = view.findViewById(R.id.txt_accZ);


        sensorManager = (SensorManager) requireActivity().getSystemService(getContext().SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }

    }


    @Override
    public void onClick(View v) {


        if (v.getId() == R.id.btn_record) { // mix of chatGPT and appDev module code
            System.out.println("record button clicked");
            isRecording = !isRecording; // Toggle state


            if (isRecording) {
                btn_record.setText(getString(R.string.btn_record_end));
                Log.d("RecordDriveFragment", "Recording Started");
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                startLocationUpdates();
                startTimer();
            } else {
                btn_record.setText(getString(R.string.btn_record_start));
                Log.d("RecordDriveFragment", "Recording Stopped");
                sensorManager.unregisterListener(this);
                stopLocationUpdates();
                stopTimer();
                totalTime = (endTime - startTime)/1000;
                Log.d("TOTAL", "number of times speeding: " + speedingCount + ". drive time: " + totalTime + " seconds" + ". Idle count: " + idleCount + ". acceleration faults: " + accelerationCount);
                String date = getCurrentDate();
                calculateScore();
                DriveData driveData = new DriveData(driveScore, date);
                Log.d("DRIVEDATA", "" + driveData.toString());
                updateDb(driveData);
            }


        }
    }

    public String getCurrentDate() {
        Log.d("date method called successfully", "getCurrentDate:11111111111111 ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Log.d("DATE ", "date info success");
        return sdf.format(new Date());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRecording){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x*x + y*y + z*z);
            long currentTime = System.currentTimeMillis();

            if (acceleration > ACCELERATIONLIMIT && (currentTime - lastAccelerationTime > COOLDOWN)) {
                accelerationCount++;
                lastAccelerationTime = currentTime;
                Log.d("ACCELERATION", "High acceleration detected: " + acceleration);
            }
            txt_accX.setText(String.format("acceleration: %.2f m/s²", acceleration));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


    private void startLocationUpdates() {
        // Create LocationRequest using LocationRequest.Builder with the new Priority enum
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();

        // Set up a LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d("LocationUpdates", "Location count: " + locationResult.getLocations().size());

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double speed = location.getSpeed(); // Speed in meters/second
                        if(speed < IDLESPEED){
                            idleCount++;
                        }
                        double speedMph = speed * 2.23694; // Convert to km/h
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Save location to list
                        location_temp[0] = latitude;
                        location_temp[1] = longitude;
                        location_temp[2] = speedMph;

                        // Log & Display Location Data
                        Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);
                        Log.d("Speed", "Current Speed: " + speedMph + " mph");


                        // Update UI with current speed
                        txt_accY.setText("Speed: " + speedMph + " mph");
                        txt_accZ.setText("Lat: " + latitude + ", Lng: " + longitude);

                        fetchSpeedLimits();
                    }
                }
            }
        };


        // Check if the app has permission before requesting location updates
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                    .addOnFailureListener(e -> {
                        Log.e("LocationUpdates", "Failed to request location updates: " + e.getMessage());
                    });
            Log.d("LocationUpdates", "Location updates requested");
        } else {
            // ... (permission request)
            Log.d("LocationUpdates", "Location permission not granted");
        }
    }


    private void fetchSpeedLimits() {
        StringBuilder query = new StringBuilder("[out:json];("); // Start query

        // Add each location to the query

        double lat = location_temp[0];
        double lon = location_temp[1];
        query.append("way(around:20,").append(lat).append(",").append(lon).append(")[maxspeed];");


        query.append("); out tags;"); // End query

        // Log query for debugging
        Log.d("SpeedLimit", "Query: " + query.toString());

        OverpassApiService apiService = RetrofitClient.getClient();
        Call<OverpassResponse> call = apiService.getSpeedLimit(query.toString());

        call.enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OverpassResponse.Element> elements = response.body().elements;

                    if (elements != null && !elements.isEmpty()) {
                        for (OverpassResponse.Element element : elements) {
                            if (element.tags != null && element.tags.maxspeed != null) {
                                try {
                                    // Regular expression to extract numeric part from the string (e.g., "30 mph" -> "30")
                                    String maxSpeedString = element.tags.maxspeed;
                                    String numericPart = maxSpeedString.replaceAll("[^0-9.]", "");  // Remove non-numeric characters except for '.'

                                    if (!numericPart.isEmpty()) {
                                        double maxSpeedValue = Double.parseDouble(numericPart); // Convert to double
                                        Log.d("SpeedLimit", "Speed Limit (in double): " + maxSpeedValue);
                                        if(maxSpeedValue < location_temp[2]){
                                            speedingCount++;
                                        }
                                    } else {
                                        Log.d("SpeedLimit", "Invalid maxspeed format: " + maxSpeedString);
                                    }
                                } catch (NumberFormatException e) {
                                    // Handle the case where the number format is invalid
                                    Log.d("SpeedLimit", "Invalid number in maxspeed: " + element.tags.maxspeed);
                                }
                            } else {
                                Log.d("SpeedLimit", "No speed limit found");
                            }
                        }
                    } else {
                        Log.d("SpeedLimit", "No speed limit data found");
                    }
                } else {
                    Log.d("SpeedLimit", "API Response Failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Log.e("SpeedLimit", "API call failed: " + t.getMessage());
            }
        });
    }


    private void startTimer() {
        isRunning = true;
        startTime = System.currentTimeMillis();
    }

    private void stopTimer() {
        isRunning = false;
        endTime = System.currentTimeMillis();
        long elapsedTime = (endTime - startTime) / 1000;
        Log.d("TIMER", "Elapsed Time: " + elapsedTime + " seconds");
    }

    private void stopLocationUpdates() {
        Log.d("SATOPPED", "stopped updates");
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void calculateScore(){
        DecimalFormat df = new DecimalFormat("#.0");

        if(sw_startStop.isChecked()){
            driveScore = 10 - (WEIGHTSPEED*speedingCount/totalTime) - (WEIGHTACC*accelerationCount/totalTime);
        } else{
            driveScore = 10 - (WEIGHTSPEED*speedingCount/totalTime) - (WEIGHTACC*accelerationCount/totalTime) - (WEIGHTIDLE*idleCount/totalTime);
        }

        if(driveScore < 0){ driveScore = 0;}
        driveScore = Double.parseDouble(df.format(driveScore));
        Log.d("-------SCORE-------", "" + driveScore);

    }

    private void updateDb(DriveData driveData){
        Log.d("UPDATE DB CALLED SUCCESSFULLY", "updateDb: ");
        driveScoresRef.add(driveData)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added the drive score
                    Log.d("document reference 234432", "yippe?");
                    updateTotalPoints(userId);
                })
                .addOnFailureListener(e -> {
                    Log.d("error in updateDb", "updateDb: error");
                });
    }

    public void updateTotalPoints(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        CollectionReference driveScoresRef = userRef.collection("driveScores");

        Log.d("DATABASE", "Fetching drive scores for user: " + userId);

        driveScoresRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            double totalPoints = 0;
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Double score = document.getDouble("score");
                if (score != null) {
                    totalPoints += score;
                }
            }

            Log.d("DATABASE", "Total Points Calculated: " + totalPoints);

            // 🔥 Use set() with merge to ensure document is created if missing
            double finalTotalPoints = totalPoints;
            userRef.set(Collections.singletonMap("totalPoints", totalPoints), SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("DATABASEUPDATED", "Total Points Updated: " + finalTotalPoints))
                    .addOnFailureListener(e -> Log.e("DATABASEFAIL", "Failed to update total points", e));

        }).addOnFailureListener(e -> {
            Log.e("DATABASEFAIL", "Failed to fetch drive scores", e);
        });
    }


    @Override
    public void onDestroyView() { //  stop listening for sensor input when fragment is left
        Log.d("STOPPED", "stopped");
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        } else if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


}


