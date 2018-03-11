package com.worktimetrackerapp.GUI_Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.worktimetrackerapp.MainActivity;
import com.worktimetrackerapp.R;

public class SignUp_Controller extends AppCompatActivity {
    RadioGroup radioGroup;
    RadioButton radioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}