package uk.ac.rgu.ecodrive;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment{

    EditText txtIn_email, txtIn_password;
    Button btn_login;
    ProgressBar pgBar_login;
    TextView txt_signUpHere;
    FirebaseAuth mAuth;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() { //check if user is already logged in before loading fragment.
        super.onStart();

        // Get NavController
        NavController navController = NavHostFragment.findNavController(this);

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            navController.navigate(R.id.action_LoginFragment_to_homePageFragment);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        super.onViewCreated(view, savedInstanceState);

        // initialise all views
        txtIn_email = view.findViewById(R.id.txtIn_email);
        txtIn_password = view.findViewById(R.id.txtIn_password);
        btn_login = view.findViewById(R.id.btn_login);
        pgBar_login = view.findViewById(R.id.pgBar_login);
        txt_signUpHere = view.findViewById(R.id.txt_signUpHere);

        btn_login.setOnClickListener(this::onClick); // set listener for buttons
        txt_signUpHere.setOnClickListener(this::onClick);

    }

    public void onClick(View v){

        NavController navController = Navigation.findNavController(v);

        if(v.getId() == R.id.btn_login){
            pgBar_login.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(txtIn_email.getText());
            password = String.valueOf(txtIn_password.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "enter username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password) //sign in with details provided
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("SIGN IN", "signInWithEmail:success");
                                navController.navigate(R.id.action_LoginFragment_to_homePageFragment);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("SIGN IN", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else if(v.getId() == R.id.txt_signUpHere){
            navController.navigate(R.id.action_LoginFragment_to_SignUpFragment);
        }

    }

}