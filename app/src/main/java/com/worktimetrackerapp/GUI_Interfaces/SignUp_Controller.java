package com.worktimetrackerapp.GUI_Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.worktimetrackerapp.R;
import com.worktimetrackerapp.MainActivity;

public class SignUp_Controller extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_screen);

        final Button btnSignUp = findViewById(R.id.btn_sign_up);

        //login button action
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send info to db

                //temporary redirection
                    Intent LaunchHome = new Intent(getApplicationContext(), MainActivity.class);
                    LaunchHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(LaunchHome);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}