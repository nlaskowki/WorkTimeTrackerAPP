package com.worktimetrackerapp.GUI_Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.worktimetrackerapp.R;
import com.worktimetrackerapp.WTTApplication;


public class SignIn_Controller extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);
        final Button btnLogin = findViewById(R.id.btn_login);
        final Button btnSignUp = findViewById(R.id.btn_Login_signUp);

        //login button action
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent LaunchHome = new Intent(getApplicationContext(), WTTApplication.class);
                LaunchHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LaunchHome);
            }
        });

        //SignUp Button Action
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent GoToSignUp = new Intent(getApplicationContext(), SignUp_Controller.class);
                GoToSignUp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(GoToSignUp);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}


