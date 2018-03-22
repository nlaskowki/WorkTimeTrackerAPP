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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridLayout;
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
    private Activity globalActivity;
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
        private TextView WageExtraTime; // /TODO: set is done
        private TextView labelWageExtraTime;// /TODO: set is done

    //other information
        private TextView otherInfoStartedTask;
        private TextView otherInfoOvertimeStartedTask; // /TODO: set is done
        private TextView otherInfoEndedTask;
        private TextView TaskExtraCost;
        private TextView TaskEarnings;
        private TextView LabelOtherInformation;
        private TextView LabelotherInfoStartedTask;
        private TextView LabelotherInfoOvertimeStartedTask; // /TODO: set is done
        private TextView LabelotherInfoEndedTask;
        private TextView LabelTaskExtraCost;
        private TextView LabelTaskEarnings;

    //DB input values
    private String DBTaskName = null;
    private Double DBTaskWage = null;
    private String DBClient = null;
    private String DBCAddress = null;
    private String DBStartDate = null;
    private String DBStartTime = null;
    private String DBEndDate = null;
    private String DBEndTime = null;
    private String DBStartDateTime = null;
    private String DBEndDateTime = null;
    private Double DBExtraCosts = null;
    private Double DBEarnings = null;
    private Double DBTaskWageOvertime = null;
    private String DBStartOverTimeDateTime = null;

    void showInfoPopup(final Document currentdoc, Activity myActif, final Boolean frommain, final FragmentManager FM) throws Exception{
        app = (DB) myActif.getApplication();
        LayoutInflater inflater = myActif.getLayoutInflater();
        layout = inflater.inflate(R.layout.loghistory_pop, null);
        float density =myActif.getResources().getDisplayMetrics().density;
        pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
        ended = false;
        mydb = app.getMydb();
        FromMain = frommain;
        globalActivity = myActif;
        setAllFields(layout);

        DisableAllFields();

        HideOtherInfo();

        LoadTaskInfo(currentdoc);

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
            otherInfoOvertimeStartedTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //implement
                    if(editing || !btnDone.getText().toString().equals("Done")) {
                        DateTimeSetter(otherInfoOvertimeStartedTask, otherInfoOvertimeStartedTask.getText().toString());
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

                            SendToDB(currentdoc);
                            btnEdit.setText("Edit");
                            btnDelete.setVisibility(View.VISIBLE);
                            btnDone.setVisibility(View.VISIBLE);
                            DisableAllFields();
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
                            if(doc !=null) {
                                try {
                                    app.setTaskDoc(doc);
                                    FM.beginTransaction().replace(R.id.content_frame, new HomeTracking_Controller()).commit();
                                    pw.dismiss();
                                } catch (Exception e) {
                                    System.out.println(e);

                                }
                            }
                          break;
                        case "Add":
                            //save and close
                            if( SendToDB(null) !=null) {
                                pw.dismiss();
                            }
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
        WageExtraTime.setFocusable(false);
        otherInfoStartedTask.setFocusable(false);
        otherInfoOvertimeStartedTask.setFocusable(false);
        otherInfoEndedTask.setFocusable(false);
        TaskExtraCost.setFocusable(false);
        TaskEarnings.setFocusable(false);
    }//done

    private void EnableAllFields(){
        editing = true;
        taskName.setFocusableInTouchMode(true);
        clientName.setFocusableInTouchMode(true);
        clientAddress.setFocusableInTouchMode(true);
        wage.setFocusableInTouchMode(true);
        WageExtraTime.setFocusableInTouchMode(true);
        TaskExtraCost.setFocusableInTouchMode(true);
        TaskEarnings.setFocusableInTouchMode(true);
    }//done

    private void HideOtherInfo(){
        //hide extra wage hr
            WageExtraTime.setVisibility(View.INVISIBLE);
            labelWageExtraTime.setVisibility(View.INVISIBLE);
            LabelotherInfoOvertimeStartedTask.setVisibility(View.INVISIBLE);
            otherInfoOvertimeStartedTask.setVisibility(View.INVISIBLE);
        otherInfoStartedTask.setVisibility(View.INVISIBLE);
        otherInfoEndedTask.setVisibility(View.INVISIBLE);
        TaskExtraCost.setVisibility(View.INVISIBLE);
        TaskEarnings.setVisibility(View.INVISIBLE);
        LabelOtherInformation.setVisibility(View.INVISIBLE);
        LabelotherInfoStartedTask.setVisibility(View.INVISIBLE);
        LabelotherInfoEndedTask.setVisibility(View.INVISIBLE);
        LabelTaskExtraCost.setVisibility(View.INVISIBLE);
        LabelTaskEarnings.setVisibility(View.INVISIBLE);
    }//done

    private void LoadTaskInfo(Document currentdoc){
        if(currentdoc != null) {
            if(currentdoc.getProperty("taskname") != null) {
                taskName.setText(currentdoc.getProperty("taskname").toString());
            }
            if(currentdoc.getProperty("TaskScheduledStartDate")  != null || currentdoc.getProperty("TaskScheduledStartTime") != null){
                String startTask = currentdoc.getProperty("TaskScheduledStartDate").toString() + " - " + currentdoc.getProperty("TaskScheduledStartTime");
                startTaskInfo.setText(startTask);
            }
            if(currentdoc.getProperty("TaskScheduledEndDate")  != null || currentdoc.getProperty("TaskScheduledEndTime") != null){
                String endTask = currentdoc.getProperty("TaskScheduledEndDate").toString() + " - " + currentdoc.getProperty("TaskScheduledEndTime");
                endTaskInfo.setText(endTask);
            }
            if(currentdoc.getProperty("taskClient") != null){
                clientName.setText(currentdoc.getProperty("taskClient").toString());
            }
            if(currentdoc.getProperty("ClientAddress") != null){
                clientAddress.setText(currentdoc.getProperty("ClientAddress").toString());
            }
            if(currentdoc.getProperty("taskwage") != null) {
                wage.setText(currentdoc.getProperty("taskwage").toString());
            }
        }
    } //done

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

        ShowOvertimeFields(currentdoc);

        if(currentdoc != null){
            ended = true;
            if(currentdoc.getProperty("TaskStartDateTime") != null) {
                otherInfoStartedTask.setText(currentdoc.getProperty("TaskStartDateTime").toString());
            }
            if(currentdoc.getProperty("TaskEndDateTime") != null){
                otherInfoEndedTask.setText(currentdoc.getProperty("TaskEndDateTime").toString());
            }
            if(currentdoc.getProperty("extracost") != null){
                TaskExtraCost.setText(currentdoc.getProperty("extracost").toString());
            }
            if(currentdoc.getProperty("TaskEarnings") != null) {
                TaskEarnings.setText(currentdoc.getProperty("TaskEarnings").toString());
            }
        }
    } //done

    private Document SendToDB(Document currentdoc){
        Document doc = null;

        if(CheckInputs(currentdoc)) {

            if (currentdoc != null) { //update doc
                try {
                    app.UpdateTask(currentdoc, ended, DBTaskName, DBTaskWage, DBClient, DBCAddress, DBStartDate, DBStartTime, DBEndDate, DBEndTime,
                            DBStartDateTime, DBEndDateTime, DBExtraCosts, DBEarnings, DBTaskWageOvertime, DBStartOverTimeDateTime);
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else { //create new document
                try {
                    String jobTitle = mydb.getDocument(app.getcurrentJob().toString()).getProperty("jobtitle").toString();
                    doc = app.NewTask(DBTaskName, jobTitle, DBTaskWage, DBClient, DBCAddress, DBStartDate, DBStartTime, DBEndDate, DBEndTime);
                } catch (Exception e) {
                    System.out.println(e);
                }
                if (!FromMain) {
                    try {
                        app.UpdateTask(doc, true, DBTaskName, DBTaskWage, DBClient, DBCAddress, DBStartDate, DBStartTime, DBEndDate, DBEndTime,
                                DBStartDateTime, DBEndDateTime, DBExtraCosts, DBEarnings, DBTaskWageOvertime, DBStartOverTimeDateTime);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            return doc;
        }else{
            return null;
        }

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
        WageExtraTime = layout.findViewById(R.id.popup_wageovertime);
        labelWageExtraTime = layout.findViewById(R.id.popup_labelwageovertime);

        //other information
        otherInfoStartedTask = layout.findViewById(R.id.popup_startedtask);
        otherInfoOvertimeStartedTask = layout.findViewById(R.id.popup_startovertime);
        otherInfoEndedTask = layout.findViewById(R.id.popup_endedtask);
        TaskExtraCost = layout.findViewById(R.id.popup_extracosts);
        TaskEarnings = layout.findViewById(R.id.popup_earnings);
        //other information labels
        LabelOtherInformation = layout.findViewById(R.id.popup_txtotherinformation);
        LabelotherInfoStartedTask = layout.findViewById(R.id.popup_txtstartedtask);
        LabelotherInfoOvertimeStartedTask = layout.findViewById(R.id.popup_labelstartovertime);
        LabelotherInfoEndedTask = layout.findViewById(R.id.popup_txtendedtask);
        LabelTaskExtraCost = layout.findViewById(R.id.popup_txtextracosts);
        LabelTaskEarnings = layout.findViewById(R.id.popup_txtearnings);

        //load wage information
            String jobTitle = mydb.getDocument(app.getcurrentJob().toString()).getProperty("jobwage").toString();
            wage.setText(jobTitle);
    }//done

    private void ShowOvertimeFields(Document currentdoc){
        if(currentdoc == null){
            WageExtraTime.setVisibility(View.VISIBLE);
            labelWageExtraTime.setVisibility(View.VISIBLE);
            LabelotherInfoOvertimeStartedTask.setVisibility(View.VISIBLE);
            otherInfoOvertimeStartedTask.setVisibility(View.VISIBLE);
        }else {
            if (currentdoc.getProperty("taskwageovertime") != null) {
                WageExtraTime.setVisibility(View.VISIBLE);
                labelWageExtraTime.setVisibility(View.VISIBLE);
                LabelotherInfoOvertimeStartedTask.setVisibility(View.VISIBLE);
                otherInfoOvertimeStartedTask.setVisibility(View.VISIBLE);
                WageExtraTime.setText(currentdoc.getProperty("taskwageovertime").toString());
                if(currentdoc.getProperty("TaskStartOvertimeDateTime") != null) {
                    otherInfoOvertimeStartedTask.setText(currentdoc.getProperty("TaskStartOvertimeDateTime").toString());
                }
            }
        }
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

    private Boolean CheckInputs(Document currentdoc) {
        SimpleDateFormat dateFormatterday = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormatterTime = new SimpleDateFormat("HH:mm");
        String reqFields = "Following fields are required: ";

        if (!taskName.getText().toString().isEmpty()) {
            DBTaskName = taskName.getText().toString();
        } else {
            reqFields = reqFields + " ,taskName";
        }
        if (!wage.getText().toString().isEmpty()) {
            DBTaskWage = Double.parseDouble(wage.getText().toString());
        } else {
            reqFields = reqFields + " ,wage";
        }
        if (!clientName.getText().toString().isEmpty()) {
            DBClient = clientName.getText().toString();
        }
        if (!clientAddress.getText().toString().isEmpty()) {
            DBCAddress = clientAddress.getText().toString();
        }
        try {
            if (!startTaskInfo.getText().toString().isEmpty()) {
                Date startDateFormat = dateFormatter.parse(startTaskInfo.getText().toString());
                DBStartDate = dateFormatterday.format(startDateFormat.getTime());
                DBStartTime = dateFormatterTime.format(startDateFormat.getTime());
            } else {
                reqFields = reqFields + " start, ";
            }
            if (!endTaskInfo.getText().toString().isEmpty()) {
                Date endDateFormat = dateFormatter.parse(endTaskInfo.getText().toString());
                DBEndDate = dateFormatterday.format(endDateFormat.getTime());
                DBEndTime = dateFormatterTime.format(endDateFormat.getTime());
            } else {
                reqFields = reqFields + "end, ";
            }
        } catch (Exception e) {
            System.out.println(e);
            reqFields = reqFields + "start, end, ";
        }

        if (!otherInfoStartedTask.getText().toString().isEmpty()) {
            DBStartDateTime = otherInfoStartedTask.getText().toString();
        } else {
            if(ended) {
                reqFields = reqFields + "otherInfoStartedTask, ";
            }
        }
        if (!otherInfoEndedTask.getText().toString().isEmpty()) {
            DBEndDateTime = otherInfoEndedTask.getText().toString();
        } else {
            if(ended) {
                reqFields = reqFields + "otherInfoEndedTask, ";
            }
        }
        if (!TaskExtraCost.getText().toString().isEmpty()) {
            DBExtraCosts = Double.parseDouble(TaskExtraCost.getText().toString());
        }
        if (!TaskEarnings.getText().toString().isEmpty()) {
            DBEarnings = Double.parseDouble(TaskEarnings.getText().toString());
        }
        if (!WageExtraTime.getText().toString().isEmpty()) {
            DBTaskWageOvertime = Double.parseDouble(WageExtraTime.getText().toString());
        } else {
            reqFields = reqFields + "";
        }
        if (!otherInfoOvertimeStartedTask.getText().toString().isEmpty()) {
            DBStartOverTimeDateTime = otherInfoOvertimeStartedTask.getText().toString();
        } else {
            reqFields = reqFields + "";
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
