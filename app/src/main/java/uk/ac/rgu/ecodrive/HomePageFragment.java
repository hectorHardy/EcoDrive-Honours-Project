package uk.ac.rgu.ecodrive;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    FirebaseAuth auth;
    FirebaseUser user;
    private ImageView iv_car;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomePageFragment newInstance(String param1, String param2) {
        HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        TextView txt_userInfo = getView().findViewById(R.id.txt_userInfo);


        if(user == null){
            navController.navigate(R.id.action_homePageFragment_to_LoginFragment);
        } else{
            txt_userInfo.setText(user.getEmail());
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //for navigating to the record page
        Button btn_record_drive_page = view.findViewById(R.id.btn_record_drive_page);
        btn_record_drive_page.setOnClickListener(this);

        Button btn_logout = getView().findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(this);

        Button btn_history = getView().findViewById(R.id.btn_history);
        btn_history.setOnClickListener(this);

        ViewFlipper vf_tips = view.findViewById(R.id.vf_tips);
        vf_tips.setFlipInterval(10000); // 10 seconds interval
        vf_tips.setInAnimation(getContext(), android.R.anim.slide_in_left);
        vf_tips.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        vf_tips.startFlipping();

        iv_car = view.findViewById(R.id.iv_car);
        view.getViewTreeObserver().addOnGlobalLayoutListener(this::startCarAnimation);

    }

    @Override
    public void onClick(View v) {

        NavController navController = Navigation.findNavController(v);


        if (v.getId() == R.id.btn_record_drive_page) {
            System.out.println("record nav button clicked");
            navController.navigate(R.id.action_homePageFragment_to_recordDriveFragment);
        } else if (v.getId() == R.id.btn_history) {
            System.out.println("record nav button clicked");
            navController.navigate(R.id.action_homePageFragment_to_historyFragment);
        } else if (v.getId() == R.id.btn_logout){
            FirebaseAuth.getInstance().signOut();
            navController.navigate(R.id.action_homePageFragment_to_LoginFragment);
        } else if (v.getId() == R.id.btn_history){
            navController.navigate(R.id.action_homePageFragment_to_historyFragment);
        }

    }

    private void startCarAnimation() {
        ConstraintLayout parentLayout = (ConstraintLayout) iv_car.getParent();
        int screenWidth = parentLayout.getWidth();
        int carWidth = iv_car.getWidth(); // Get the car's width

        ObjectAnimator animator = ObjectAnimator.ofFloat(iv_car, "translationX", -carWidth, screenWidth);
        animator.setDuration(5000); // Time for car to move across
        animator.setRepeatCount(ValueAnimator.INFINITE); // Infinite loop
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.start();
    }

}