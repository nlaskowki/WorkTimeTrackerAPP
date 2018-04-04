package com.worktimetrackerapp.GUI_Interfaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class JobSettingsPopUp extends AppCompatActivity {
    private boolean editing;
    private Database mydb;
    private View layout;
    private DB app;
    private PopupWindow pw;
    private Activity globalActivity;
    Spinner spinner;
    TextView company;
    TextView jobTitle;
    TextView employer;
    TextView hourlywage;
    TextView avghours;
    public Button btnDelete;
    public Button btnDone;
    public Button btnEdit;

    //DB input values
    private String DBjobCompany = null;
    private String DBjobType = null;
    private String DBjobTitle = null;
    private String DBjobEmployer = null;
    private double DBjobWage = 0.0;
    private double DBjobAveHours = 0.0;


    void showJobInfoPopup(final Document currentdoc, Activity myActif, final FragmentManager FM) throws Exception {
        app = (DB) myActif.getApplication();
        LayoutInflater inflater = myActif.getLayoutInflater();
        layout = inflater.inflate(R.layout.jobsetting_popup, null);
        float density =myActif.getResources().getDisplayMetrics().density;
        pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
      /*  spinner = layout.findViewById(R.id.popup_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);*/
        mydb = app.getMydb();
        globalActivity = myActif;
        setAllFields(layout);

        DisableAllFields();

        LoadTaskInfo(currentdoc);

        if(currentdoc == null){
            EnableAllFields();

        btnEdit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnEdit.getText().equals("Edit")) {
                    btnEdit.setVisibility(View.INVISIBLE);
                    btnDone.setText("Save");
                    btnDelete.setText("Cancel");
                    btnDone.getText();
                    //enable textfields
                    EnableAllFields();
                }else {//button is equal to save

                    SendToDB(currentdoc);
                    btnDone.setText("Done");
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDone.setVisibility(View.VISIBLE);
                    DisableAllFields();
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnDelete.getText().toString().equals("Delete")) {
                    Document job = (Document) mydb.getDocument(currentdoc.getId());
                    try {
                        editing = false;
                        job.delete();
                        pw.dismiss();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }else{
                    pw.dismiss();
                }
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnDone.getText().toString().equals("Done")) {
                    if (SendToDB(null) !=null);{
                        pw.dismiss();
                    }

                }

            }
        });

        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.getBackground().setAlpha(128);
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if(event.getAction() == 0){
                //System.out.println("Test");
                // pw.dismiss();
                // return true;
                //}
                return false;
            }
        });
        pw.setOutsideTouchable(true);
        pw.showAtLocation(layout, Gravity.CENTER, 0,0);
    }
    }
    private void DisableAllFields(){
        editing = false;
        company.setFocusable(false);
        jobTitle.setFocusable(false);
        employer.setFocusable(false);
        hourlywage.setFocusable(false);
        avghours.setFocusable(false);
    }//done

    private void EnableAllFields(){
        editing = true;
        company.setFocusableInTouchMode(true);
        jobTitle.setFocusableInTouchMode(true);
        employer.setFocusableInTouchMode(true);
        hourlywage.setFocusableInTouchMode(true);
        avghours.setFocusableInTouchMode(true);
    }//done


    private void LoadTaskInfo(Document currentdoc){
        if(currentdoc != null) {
            if(currentdoc.getProperty("jobcompany") != null) {
                company.setText(currentdoc.getProperty("jobcompany").toString());
            }
            if(currentdoc.getProperty("jobtitle")  != null || currentdoc.getProperty("jobtitle") != null){
                String startTask = currentdoc.getProperty("jobtitle").toString();
                jobTitle.setText(startTask);
            }
            if(currentdoc.getProperty("jobemployer")  != null || currentdoc.getProperty("jobemployer") != null){
                String endTask = currentdoc.getProperty("jobemployer").toString() + " - " + currentdoc.getProperty("jobemployer");
                employer.setText(endTask);
            }
            if(currentdoc.getProperty("jobwage") != null){
                hourlywage.setText(currentdoc.getProperty("jobwage").toString());
            }
            if(currentdoc.getProperty("jobavehours") != null){
                avghours.setText(currentdoc.getProperty("jobavehours").toString());
            }
        }
    } //done


    private Document SendToDB(Document currentdoc){
        Document doc = null;

        if(CheckInputs(currentdoc)) {

            if (currentdoc != null) { //update doc
                try {
                    app.UpdateJob(currentdoc, DBjobCompany, DBjobType,  DBjobTitle, DBjobEmployer, DBjobWage, DBjobAveHours);
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else { //create new job
                //final DB app = (DB) getApplication();
                Double dblavghours = 0.0;
                String cmp = null;
                try{
                    if(!avghours.getText().toString().isEmpty()) {
                        dblavghours = Double.parseDouble(avghours.getText().toString());
                    }
                    if(!company.getText().toString().isEmpty()){
                        cmp = company.getText().toString();
                    }
                    System.out.println(hourlywage.getText().toString());
                    app.AddJob(cmp, spinner.getSelectedItem().toString(), jobTitle.getText().toString(), employer.getText().toString(), Double.parseDouble(hourlywage.getText().toString()), dblavghours);
                }catch(Exception e){System.out.println(e);}

                return doc;
            }

        }
        return doc;
    }

    private void setAllFields(View layout){
        //set button fields in popup
        btnDelete = layout.findViewById(R.id.popup_btn_delete);
        btnEdit = layout.findViewById(R.id.popup_btn_edit);
        btnDone = layout.findViewById(R.id.popup_btn_done);

        //set other fields in popup
        company = layout.findViewById(R.id.popup_txt_company);
        jobTitle = layout.findViewById(R.id.popup_txt_job_title);
        employer= layout.findViewById(R.id.popup_txt_employer);
        hourlywage = layout.findViewById(R.id.popup_txt_hourly_wage);
        avghours = layout.findViewById(R.id.popup_txt_avg_hours);
        spinner = layout.findViewById(R.id.popup_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }//done


    private Boolean CheckInputs(Document currentdoc) {
        String reqFields = "Following fields are required: ";

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position ==0) {
                    company.setVisibility(View.INVISIBLE);
                    jobTitle.setVisibility(View.INVISIBLE);
                    employer.setVisibility(View.INVISIBLE);
                    hourlywage.setVisibility(View.INVISIBLE);
                    avghours.setVisibility(View.INVISIBLE);
                }
                else if (position == 1){
                    company.setVisibility(View.VISIBLE);
                    jobTitle.setVisibility(View.VISIBLE);
                    employer.setVisibility(View.VISIBLE);
                    hourlywage.setVisibility(View.VISIBLE);
                    avghours.setVisibility(View.VISIBLE);
                } else {
                    company.setText(null);
                    company.setVisibility(View.INVISIBLE);
                    jobTitle.setVisibility(View.VISIBLE);
                    employer.setVisibility(View.VISIBLE);
                    hourlywage.setVisibility(View.VISIBLE);
                    avghours.setVisibility(View.VISIBLE);
                }
                //Toast.makeText(SignUp_Controller.this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (!jobTitle.getText().toString().isEmpty()) {
            DBjobTitle = jobTitle.getText().toString();
        } else {
            reqFields = reqFields + " ,Job Title";
        }
        if (!hourlywage.getText().toString().isEmpty()) {
            DBjobWage = Double.parseDouble(hourlywage.getText().toString());
        } else {
            reqFields = reqFields + " ,Hourly Wage";
        }
        if (!employer.getText().toString().isEmpty()) {
            DBjobEmployer = employer.getText().toString();
        }
        if (!avghours.getText().toString().isEmpty()) {
            DBjobAveHours = Double.parseDouble(avghours.getText().toString());
        }
        if (!company.getText().toString().isEmpty()) {
            DBjobCompany = company.getText().toString();
        }


        if (reqFields.equals("Following fields are required: ")) {
            return true;
        }else{
            //show message
            reqFields = reqFields + "!!!";
            AlertDialog.Builder btnStart = new AlertDialog.Builder(globalActivity);

            btnStart.setMessage(reqFields).setCancelable(false)
                    .setPositiveButton("close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = btnStart.create();
            alert.setTitle("WTT");
            alert.show();
            return false;
        }
    }

}
