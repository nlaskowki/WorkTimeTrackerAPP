package com.worktimetrackerapp.gui_controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;



public class JobSettingsPopUp {
    private Database mydb;
    private DB app;
    private PopupWindow pw;
    private Activity globalActivity;
    private Spinner spinner;
    private TextView company;
    private TextView jobTitle;
    private TextView hourlywage;
    private TextView avghours;
    private  Button btnDelete;
    private  Button btnDone;
    private  Button btnEdit;

    //DB input values
    private String DBjobType = null;
    private String DBjobCompany = null;
    private String DBjobTitle = null;
    private String DBjobEmployer = null;
    private double DBjobWage = 0.0;
    private double DBjobAveHours = 0.0;


    void showJobInfoPopup(final Document currentdoc, Activity myActif) throws Exception {
        app = (DB) myActif.getApplication();
        LayoutInflater inflater = myActif.getLayoutInflater();
        View layout = inflater.inflate(R.layout.jobsetting_popup, null);
        float density =myActif.getResources().getDisplayMetrics().density;
        pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
        mydb = app.getMydb();
        globalActivity = myActif;

        setAllFields(layout);

        DisableAllFields();

        LoadTaskInfo(currentdoc);

        if(currentdoc == null) {//new job
            EnableAllFields();
            btnEdit.setVisibility(View.INVISIBLE);
            btnDelete.setText("Cancel");
            btnDone.setText("Save");
        }
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btnDelete.getText().toString().equals("Cancel")) {
                    pw.dismiss();
                }else
                    if(btnDelete.getText().toString().equals("Delete")){
                        assert currentdoc != null;
                        Document job = mydb.getDocument(currentdoc.getId());
                        try {
                            job.delete();
                            app.reloadMenu();
                            pw.dismiss();
                        } catch (Exception e) {
                           e.printStackTrace();
                        }
                }
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnEdit.getText().equals("Edit")) {
                    spinner.setEnabled(true);
                    EnableAllFields();
                    btnEdit.setVisibility(View.INVISIBLE);
                    btnDone.setText("Save");
                    btnDelete.setText("Cancel");

                }else {
                    btnDone.setText("Done");
                    btnDelete.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDone.setVisibility(View.VISIBLE);
                    DisableAllFields();
                }
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btnDone.getText().toString().equals("Save")) {
                    if(SendToDB(currentdoc)) { // update the job info
                        app.reloadMenu();
                        pw.dismiss();
                    }
                } else if (btnDone.getText().toString().equals("Done")) {
                    pw.dismiss();
                }


            }
        });

        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.getBackground().setAlpha(128);
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        pw.setOutsideTouchable(true);
        pw.showAtLocation(layout, Gravity.CENTER, 0,0);
    }


    private void DisableAllFields(){
        spinner.setFocusable(false);
        spinner.setEnabled(false);
        company.setFocusable(false);
        jobTitle.setFocusable(false);

        hourlywage.setFocusable(false);
        avghours.setFocusable(false);
    }//checked

    private void EnableAllFields(){
        spinner.setFocusableInTouchMode(true);
        spinner.setEnabled(true);
        company.setFocusableInTouchMode(true);
        jobTitle.setFocusableInTouchMode(true);
        hourlywage.setFocusableInTouchMode(true);
        avghours.setFocusableInTouchMode(true);
    }//checked

    private void LoadTaskInfo(Document currentdoc){
        if(currentdoc != null) {
            if (currentdoc.getProperty("jobtype") != null) {
                if(currentdoc.getProperty("jobtype").toString().equals("Self-Employed")) {
                    spinner.setSelection(1);
                }else {
                    spinner.setSelection(2);
                }
            }
            if(currentdoc.getProperty("jobcompany") != null) {
                company.setText(currentdoc.getProperty("jobcompany").toString());
            }
            if(currentdoc.getProperty("jobtitle")  != null){
                jobTitle.setText(currentdoc.getProperty("jobtitle").toString());
            }
            if(currentdoc.getProperty("jobwage") != null){
                hourlywage.setText(currentdoc.getProperty("jobwage").toString());
            }
            if(currentdoc.getProperty("jobavehours") != null){
                avghours.setText(currentdoc.getProperty("jobavehours").toString());
            }
        }
    } //checked + edit

    private boolean SendToDB(Document currentdoc){
        Boolean bln = false;
        if(CheckInputs()) {
            if (currentdoc != null) { //update doc
                try {
                    app.UpdateJob(currentdoc, DBjobCompany, DBjobType , DBjobTitle, DBjobWage, DBjobAveHours);
                    bln = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else { //create new job
                try{
                    app.AddJob(DBjobCompany, DBjobType, DBjobTitle, DBjobWage, DBjobAveHours);
                    bln = true;
                }catch(Exception e){e.printStackTrace();}
            }
        }else{
            bln=false;
        }
        return bln;
    }//checked + edit

    private void setAllFields(View layout){
        //set button fields in popup
        btnDelete = layout.findViewById(R.id.popupjob_btn_delete);
        btnEdit = layout.findViewById(R.id.popupjob_btn_edit);
        btnDone = layout.findViewById(R.id.popupjob_btn_done);

        //set other fields in popup
        company = layout.findViewById(R.id.popup_txt_company);
        jobTitle = layout.findViewById(R.id.popup_txt_job_title);
        hourlywage = layout.findViewById(R.id.popup_txt_hourly_wage);
        avghours = layout.findViewById(R.id.popup_txt_avg_hours);
        spinner = layout.findViewById(R.id.popup_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(globalActivity.getApplicationContext(),
                R.array.spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    } //checked


    private Boolean CheckInputs() {
        String reqFields = "Following fields are required: ";

        if(spinner.getSelectedItem() != null){
            DBjobType = spinner.getSelectedItem().toString();
        }else{
            reqFields = reqFields + "Job Type: (Drop Down)";
        }
        if (!jobTitle.getText().toString().isEmpty()) {
            DBjobTitle = jobTitle.getText().toString();
        } else {
            reqFields = reqFields + "Job Title";
        }
        if (!hourlywage.getText().toString().isEmpty()) {
            DBjobWage = Double.parseDouble(hourlywage.getText().toString());
        } else {
            reqFields = reqFields + "Hourly Wage";
        }
        if (!avghours.getText().toString().isEmpty()) {
            DBjobAveHours = Double.parseDouble(avghours.getText().toString());
        }
        if (!company.getText().toString().isEmpty()) {
            DBjobCompany = company.getText().toString();
        }else {
            reqFields = reqFields + "Company Name";
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
    }//checked + edit

}