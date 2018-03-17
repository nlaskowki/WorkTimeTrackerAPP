package com.worktimetrackerapp.GUI_Interfaces;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


class PopUpWindows {
    private boolean ended;
    private View layout;
    private DB app;
    private Database mydb;
    private boolean editing;
    private static boolean FromMain;
    private Calendar myCalendar;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd - HH:mm", Locale.US);
    private PopupWindow pw;
    //variables
    private Button btnDelete;
        private Button btnEdit;
        private Button btnDone;
    //task info
    private TextView taskName;
        private TextView startTaskInfo;
        private TextView endTaskInfo;
        private TextView clientName;
        private TextView clientAddress;
        private TextView wage;
    //other information
    private TextView otherInfoStartedTask;
        private TextView otherInfoEndedTask;
        private TextView TaskExtraCost;
        private TextView TaskEarnings;
        private TextView LabelOtherInformation;
        private TextView LabelotherInfoStartedTask;
        private TextView LabelotherInfoEndedTask;
        private TextView LabelTaskExtraCost;
        private TextView LabelTaskEarnings;

    void showInfoPopup(final Document currentdoc, Activity myActif, final Boolean frommain, final FragmentManager FM) throws Exception{
        app = (DB) myActif.getApplication();
        LayoutInflater inflater = myActif.getLayoutInflater();
        layout = inflater.inflate(R.layout.loghistory_pop, null);
        float density =myActif.getResources().getDisplayMetrics().density;
        pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
        ended = false;
        mydb = app.getMydb();
        FromMain = frommain;

        setAllFields(layout);

        DisableAllFields();

        LoadTaskInfo(currentdoc);
        HideOtherInfo();
        if(currentdoc == null){//creating new task
            editing = true;
            EnableAllFields();
            btnEdit.setVisibility(View.INVISIBLE);
            btnDelete.setText("Cancel");

            if(FromMain){//omit last part
                btnDone.setText("Start");
                Calendar calendar = Calendar.getInstance();
                String selectedDay = dateFormatter.format(calendar.getTime());
                startTaskInfo.setText(selectedDay);

            }else{
                btnDone.setText("Add");
                ShowLoadOtherInfo(currentdoc);
            }

        }else{
            if(currentdoc.getProperty("TaskStartDateTime") != null && currentdoc.getProperty("TaskEndDateTime") != null ) {
                ShowLoadOtherInfo(currentdoc);
            }
        }

        //set on click listeners
            startTaskInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //implement
                    if(editing || !btnDone.getText().toString().equals("Done")) {
                        DateTimeSetter(startTaskInfo, startTaskInfo.getText().toString());
                    }
                }
            });
            endTaskInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //implement
                    if(editing || !btnDone.getText().toString().equals("Done")) {
                        DateTimeSetter(endTaskInfo, endTaskInfo.getText().toString());
                    }
                }
            });
            otherInfoStartedTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //implement
                    if(editing || !btnDone.getText().toString().equals("Done")) {
                        DateTimeSetter(otherInfoStartedTask, otherInfoStartedTask.getText().toString());
                    }
                }
            });
            otherInfoEndedTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //implement
                    if(editing || !btnDone.getText().toString().equals("Done")) {
                        DateTimeSetter(otherInfoEndedTask, otherInfoEndedTask.getText().toString());
                    }
                }
            });
            btnEdit.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(btnEdit.getText().equals("Edit")) {
                        //rename edit button
                            btnEdit.setText("Save");
                            btnEdit.getText();
                            btnDelete.setVisibility(View.INVISIBLE);
                            btnDone.setVisibility(View.INVISIBLE);
                        //enable textfields
                            EnableAllFields();
                    }else {//buttontext is equal to save
                        //disable textfields
                            btnEdit.setText("Edit");
                            btnDelete.setVisibility(View.VISIBLE);
                            btnDone.setVisibility(View.VISIBLE);

                        DisableAllFields();

                        SendToDB(currentdoc);
                    }
                }
            });
            btnDelete.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(btnDelete.getText().toString().equals("Delete")) {
                        Document task = (Document) mydb.getDocument(currentdoc.getId());
                        try {
                            editing = false;
                            task.delete();
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
                    switch (btnDone.getText().toString()){
                        case "Done":
                            if(btnEdit.getText().equals("Edit")) {
                                editing = false;
                                pw.dismiss();
                            }
                            break;
                        case "Start":
                            //save and go to hometracking
                            Document doc = SendToDB(null);
                            try {
                                app.setTaskDoc(doc);
                                FM.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
                                pw.dismiss();
                            }catch (Exception e){
                                System.out.println(e);

                            }
                          break;
                        case "Add":
                            //save and close
                            SendToDB(null);
                            pw.dismiss();
                            break;
                    }

                }
            });
//set up touch closing outside of pop-up
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
    private void DisableAllFields(){
        editing = false;
        taskName.setFocusable(false);
        startTaskInfo.setFocusable(false);
        endTaskInfo.setFocusable(false);
        clientName.setFocusable(false);
        clientAddress.setFocusable(false);
        wage.setFocusable(false);
        otherInfoStartedTask.setFocusable(false);
        otherInfoEndedTask.setFocusable(false);
        TaskExtraCost.setFocusable(false);
        TaskEarnings.setFocusable(false);
    }

    private void EnableAllFields(){
        editing = true;
        taskName.setFocusableInTouchMode(true);
        clientName.setFocusableInTouchMode(true);
        clientAddress.setFocusableInTouchMode(true);
        wage.setFocusableInTouchMode(true);
        TaskExtraCost.setFocusableInTouchMode(true);
        TaskEarnings.setFocusableInTouchMode(true);
    }

    private void HideOtherInfo(){
        otherInfoStartedTask.setVisibility(View.INVISIBLE);
        otherInfoEndedTask.setVisibility(View.INVISIBLE);
        TaskExtraCost.setVisibility(View.INVISIBLE);
        TaskEarnings.setVisibility(View.INVISIBLE);
        LabelOtherInformation.setVisibility(View.INVISIBLE);
        LabelotherInfoStartedTask.setVisibility(View.INVISIBLE);
        LabelotherInfoEndedTask.setVisibility(View.INVISIBLE);
        LabelTaskExtraCost.setVisibility(View.INVISIBLE);
        LabelTaskEarnings.setVisibility(View.INVISIBLE);
    }

    private void LoadTaskInfo(Document currentdoc){
        if(currentdoc != null) {
            taskName.setText(currentdoc.getProperty("taskname").toString());
            String startTask = currentdoc.getProperty("TaskScheduledStartDate").toString() + " - " + currentdoc.getProperty("TaskScheduledStartTime");
            startTaskInfo.setText(startTask);
            String endTask = currentdoc.getProperty("TaskScheduledEndDate").toString() + " - " + currentdoc.getProperty("TaskScheduledEndTime");
            endTaskInfo.setText(endTask);
            clientName.setText(currentdoc.getProperty("taskClient").toString());
            clientAddress.setText(currentdoc.getProperty("ClientAddress").toString());
            wage.setText(currentdoc.getProperty("taskwage").toString());
        }
    }

    private void ShowLoadOtherInfo(Document currentdoc){
        otherInfoStartedTask.setVisibility(View.VISIBLE);
        otherInfoEndedTask.setVisibility(View.VISIBLE);
        TaskExtraCost.setVisibility(View.VISIBLE);
        TaskEarnings.setVisibility(View.VISIBLE);
        LabelOtherInformation.setVisibility(View.VISIBLE);
        LabelotherInfoStartedTask.setVisibility(View.VISIBLE);
        LabelotherInfoEndedTask.setVisibility(View.VISIBLE);
        LabelTaskExtraCost.setVisibility(View.VISIBLE);
        LabelTaskEarnings.setVisibility(View.VISIBLE);
        if(currentdoc != null){
            ended = true;
            otherInfoStartedTask.setText(currentdoc.getProperty("TaskStartDateTime").toString());
            otherInfoEndedTask.setText(currentdoc.getProperty("TaskEndDateTime").toString());
            TaskExtraCost.setText(currentdoc.getProperty("extracost").toString());
            TaskEarnings.setText(currentdoc.getProperty("TaskEarnings").toString());
        }
    }

    private Document SendToDB(Document currentdoc){
        Document doc = null;
        String startDate = "";
        String startTime = "";
        String endDate = "";
        String endTime ="";
        SimpleDateFormat dateFormatterday = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormatterTime = new SimpleDateFormat("HH:mm");
        try {
            if(!startTaskInfo.getText().toString().isEmpty()) {
                Date startDateFormat = dateFormatter.parse(startTaskInfo.getText().toString());
                startDate = dateFormatterday.format(startDateFormat.getTime());
                startTime = dateFormatterTime.format(startDateFormat.getTime());
            }
            if(!endTaskInfo.getText().toString().isEmpty()) {
                Date endDateFormat = dateFormatter.parse(endTaskInfo.getText().toString());
                endDate = dateFormatterday.format(endDateFormat.getTime());
                endTime = dateFormatterTime.format(endDateFormat.getTime());
            }
        }catch (Exception e){
            System.out.println(e);
        }
        if(currentdoc != null) { //update doc
            if (!ended) {//omit 4 fields
                try {
                    app.UpdateTask(currentdoc, ended, taskName.getText().toString(), Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime,
                            null, null, null, null);
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else {//all fields
                try {
                    app.UpdateTask(currentdoc, ended, taskName.getText().toString(), Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime,
                            otherInfoStartedTask.getText().toString(), otherInfoEndedTask.getText().toString(), Double.parseDouble(TaskExtraCost.getText().toString()), Double.parseDouble(TaskEarnings.getText().toString()));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }else{ //create new document
            try{
                String jobTitle = mydb.getDocument(app.getcurrentJob().toString()).getProperty("jobtitle").toString();
               doc = app.NewTask(taskName.getText().toString(),jobTitle ,Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime);
            }catch (Exception e) {
                System.out.println(e);
            }
            if(!FromMain){
                try {
                    app.UpdateTask(doc, ended, taskName.getText().toString(), Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime,
                            otherInfoStartedTask.getText().toString(), otherInfoEndedTask.getText().toString(), Double.parseDouble(TaskExtraCost.getText().toString()), Double.parseDouble(TaskEarnings.getText().toString()));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        return doc;
    }

    private void setAllFields(View layout){
        //set fields from popup
        btnDelete = layout.findViewById(R.id.popup_deletetask);
        btnEdit = layout.findViewById(R.id.popup_edittask);
        btnDone = layout.findViewById(R.id.popup_donetask);
        //task info
        taskName = layout.findViewById(R.id.popup_taskname);
        startTaskInfo = layout.findViewById(R.id.popup_startdatetime);
        endTaskInfo = layout.findViewById(R.id.popup_enddatetime);
        clientName = layout.findViewById(R.id.popup_clientname);
        clientAddress = layout.findViewById(R.id.popup_clientaddress);
        wage = layout.findViewById(R.id.popup_wagehr);
        //other information
        otherInfoStartedTask = layout.findViewById(R.id.popup_startedtask);
        otherInfoEndedTask = layout.findViewById(R.id.popup_endedtask);
        TaskExtraCost = layout.findViewById(R.id.popup_extracosts);
        TaskEarnings = layout.findViewById(R.id.popup_earnings);
        //other information labels
        LabelOtherInformation = layout.findViewById(R.id.popup_txtotherinformation);
        LabelotherInfoStartedTask = layout.findViewById(R.id.popup_txtstartedtask);
        LabelotherInfoEndedTask = layout.findViewById(R.id.popup_txtendedtask);
        LabelTaskExtraCost = layout.findViewById(R.id.popup_txtextracosts);
        LabelTaskEarnings = layout.findViewById(R.id.popup_txtearnings);

        //load wage information
            String jobTitle = mydb.getDocument(app.getcurrentJob().toString()).getProperty("jobwage").toString();
            wage.setText(jobTitle);
    }

    private void DateTimeSetter(final TextView v, String dateTime){
        myCalendar = Calendar.getInstance();
        try {
            if(!dateTime.isEmpty()) {
                Date date = dateFormatter.parse(dateTime);
                myCalendar.setTime(date);
            }else{
                myCalendar = Calendar.getInstance();
            }
        }catch(Exception e){

        }
        new DatePickerDialog(layout.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(layout.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        myCalendar.set(Calendar.MINUTE, selectedMinute);
                        v.setText(dateFormatter.format(myCalendar.getTime()));
                    }
                }, myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true).show();
            }
        },myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

}
