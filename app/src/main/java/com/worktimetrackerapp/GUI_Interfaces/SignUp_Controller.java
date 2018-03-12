package com.worktimetrackerapp.GUI_Interfaces;


import android.app.AlertDialog;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.MainActivity;
import com.worktimetrackerapp.R;


public class SignUp_Controller extends AppCompatActivity {

    TextView company;
    TextView jobTitle;
    TextView employer;
    TextView hourlywage;
    TextView avghours;
    public static Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        company = findViewById(R.id.txt_company);
        jobTitle = findViewById(R.id.txt_job_title);
        employer= findViewById(R.id.txt_employer);
        hourlywage = findViewById(R.id.txt_hourly_wage);
        avghours = findViewById(R.id.txt_avg_hours);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SignUp_Controller.this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void dialogevent (View view){
        btn = findViewById(R.id.btn_next);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //add job to db
                final DB app = (DB) getApplication();
                try{
                    app.AddJob(company.getText().toString(), jobTitle.getText().toString(), employer.getText().toString(), Double.parseDouble(hourlywage.getText().toString()), Double.parseDouble(avghours.getText().toString()));
                }catch(Exception e){System.out.println(e);}

                AlertDialog.Builder btn_next = new AlertDialog.Builder(SignUp_Controller.this);
                btn_next.setMessage("Do you want to add another job?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //instead of using intent empty fields
                                Intent RefreshUserInfo = new Intent(getApplicationContext(), SignUp_Controller.class);
                                RefreshUserInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(RefreshUserInfo);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                app.completeLogin();
                            }
                        });

                AlertDialog alert = btn_next.create();
                alert.setTitle("WTT");
                alert.show();
            }
        });
    }

private void Reset(){
        company.setText(null);
        company.setVisibility(View.INVISIBLE);

}
private void ShowSEFields(){
    company.setVisibility(View.VISIBLE);
}
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}