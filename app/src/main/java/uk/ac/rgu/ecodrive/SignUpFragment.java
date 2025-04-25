package uk.ac.rgu.ecodrive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import uk.ac.rgu.ecodrive.models.UserProfile;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    EditText txtIn_email, txtIn_password, txtIn_displayName;
    Button btn_signUp;
    ProgressBar pgBar_signUp;
    TextView txt_loginHere;
    FirebaseAuth mAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance(); // initialise firebase auth
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        super.onViewCreated(view, savedInstanceState);

        txtIn_email = view.findViewById(R.id.txtIn_email);
        txtIn_password = view.findViewById(R.id.txtIn_password);
        txtIn_displayName = view.findViewById(R.id.txtIn_displayName);
        btn_signUp = view.findViewById(R.id.btn_signUp);
        pgBar_signUp = view.findViewById(R.id.pgBar_signUp);
        txt_loginHere = view.findViewById(R.id.txt_loginHere);

        btn_signUp.setOnClickListener(this::onClick);
        txt_loginHere.setOnClickListener(this::onClick);

    }

    public void onClick(View v){

        NavController navController = Navigation.findNavController(v);

        if (v.getId() == R.id.txt_loginHere) {
            System.out.println("login txt button clicked");
            navController.navigate(R.id.action_SignUpFragment_to_LoginFragment);
        }else if(v.getId() == R.id.btn_signUp) {

            pgBar_signUp.setVisibility(View.VISIBLE); // display progress bar
            String email, password;
            email = String.valueOf(txtIn_email.getText());
            password = String.valueOf(txtIn_password.getText());
            String username = String.valueOf(txtIn_displayName.getText()).trim();

            if (TextUtils.isEmpty(email)) { //popup alert if sign up field is blank
                Toast.makeText(getContext(), "enter username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(username)) {
                username = "Anonymous";
            }

            mAuth.createUserWithEmailAndPassword(email, password) //attempts to create account with details provided.
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pgBar_signUp.setVisibility(View.GONE); // hide progress bar
                            if (task.isSuccessful()) {

                                Toast.makeText(getContext(), "Account Created", Toast.LENGTH_SHORT).show();

                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    String userId = user.getUid();
                                    String username = txtIn_displayName.getText().toString().trim();

                                    if (username.isEmpty()) {
                                        username = "Anonymous";
                                    }

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    UserProfile userProfile = new UserProfile(username);

                                    db.collection("users").document(userId)
                                            .set(userProfile, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("SIGNUP", "Username saved.");
                                                navController.navigate(R.id.action_SignUpFragment_to_homePageFragment);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("SIGNUP_FAIL", "Failed to save username", e);
                                                Toast.makeText(getContext(), "Error saving user info.", Toast.LENGTH_SHORT).show();
                                            });
                                }

                                navController.navigate(R.id.action_SignUpFragment_to_homePageFragment); // on successful sign up, navigate to the home page, logged in
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("SIGN IN", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

}