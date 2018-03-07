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
        radioGroup = findViewById(R.id.rgroup);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SignUp_Controller.this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Button btnSignUp = findViewById(R.id.btn_sign_up);

        //login button action
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send info to db

                //DB app = (DB) getApplicationContext();
                //NewJob(String jobType, String jobTitle, String jobEmployer, Double jobWage, Double jobAveHours)


                //temporary redirection
                Intent LaunchHome = new Intent(getApplicationContext(), MainActivity.class);
                LaunchHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LaunchHome);

                int rbid = radioGroup.getCheckedRadioButtonId();
                radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(rbid);

                Toast.makeText(getBaseContext(), radioButton.getText(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // public void rbclick (View v){
    //     int rbid = radioGroup.getCheckedRadioButtonId();
    //     radioButton = findViewById(rbid);

    //    Toast.makeText(getBaseContext(), radioButton.getText(), Toast.LENGTH_LONG).show();
    //}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}