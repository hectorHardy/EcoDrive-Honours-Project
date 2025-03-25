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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.rgu.ecodrive.api.OverpassApiService;
import uk.ac.rgu.ecodrive.api.RetrofitClient;
import uk.ac.rgu.ecodrive.models.OverpassResponse;
import uk.ac.rgu.ecodrive.models.LocationData;

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest; // check correct import
    private LocationCallback locationCallback;
    private List<LocationData> locationList = new ArrayList<>(); // Store recorded locations

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
                if (isGranted) {
//                    startLocationUpdates();
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission using Activity Result API
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            //startLocationUpdates();
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_drive, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //for initiating recording
        btn_record = view.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(this);

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
            } else {
                btn_record.setText(getString(R.string.btn_record_start));
                Log.d("RecordDriveFragment", "Recording Stopped");
                sensorManager.unregisterListener(this);
                stopLocationUpdates();
                Log.d("locationList", locationList.toString());
                fetchSpeedLimits(locationList);
            }

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRecording){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x*x + y*y + z*z);
            txt_accX.setText(String.format("acceleration: %.2f m/s²", acceleration));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startLocationUpdates() {
        // Create LocationRequest using LocationRequest.Builder with the new Priority enum
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)  // Optional: Set the max wait time
                .build();

        // Set up a LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        float speed = location.getSpeed(); // Speed in meters/second
                        float speedKmh = speed * 3.6f; // Convert to km/h
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Save location to list
                        locationList.add(new LocationData(latitude, longitude, speedKmh));

                        // Log & Display Location Data
                        Log.d("Location", "Lat: " + latitude + ", Lng: " + longitude);
                        Log.d("Speed", "Current Speed: " + speedKmh + " km/h");

                        // Update UI with current speed
                        txt_accY.setText("Speed: " + speedKmh + " km/h");
                        txt_accZ.setText("Lat: " + latitude + ", Lng: " + longitude);
                    }
                }
            }
        };

        // Check if the app has permission before requesting location updates
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void fetchSpeedLimits(List<LocationData> locations) {
        if (locations.isEmpty()) {
            Log.d("SpeedLimit", "No locations available to fetch speed limits.");
            return;
        }

        // Build the Overpass API Query for Multiple Locations
        StringBuilder query = new StringBuilder("[out:json];");
        for (int i = 0; i < locations.size(); i++) {
            // Only get every 5th location (i % 5 == 0)
            if (i % 5 == 0) {
                LocationData location = locations.get(i);
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                query.append("way(around:20,").append(lat).append(",").append(lon).append(")[maxspeed];");
            }
        }
        query.append("out;"); // Complete the query

        OverpassApiService apiService = RetrofitClient.getClient();
        Call<OverpassResponse> call = apiService.getSpeedLimit(query.toString());

        // Log the full query URL before making the request
        Log.d("SpeedLimit", "Query: " + query.toString());

        call.enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                Log.d("SpeedLimit", "Raw Response: " + response.raw()); // Debug raw response
                if (response.isSuccessful() && response.body() != null) {
                    List<OverpassResponse.Element> elements = response.body().elements;
                    if (elements != null && !elements.isEmpty()) {
                        for (int i = 0; i < elements.size(); i++) {
                            OverpassResponse.Element element = elements.get(i);
                            if (element.tags != null && element.tags.maxspeed != null) {
                                Log.d("SpeedLimit", "Location " + (i + 1) + ": " + element.tags.maxspeed + " km/h");
                            } else {
                                Log.d("SpeedLimit", "Location " + (i + 1) + ": No speed limit found");
                            }
                        }
                    } else {
                        Log.d("SpeedLimit", "No speed limit data found for any location");
                    }
                } else {
                    Log.d("SpeedLimit", "API Response Failed"+ response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Log.e("SpeedLimit", "API call failed: " + t.getMessage());
            }
        });
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroyView() { //  stop listening for sensor input when fragment is left
        super.onDestroyView();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        } else if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

}

