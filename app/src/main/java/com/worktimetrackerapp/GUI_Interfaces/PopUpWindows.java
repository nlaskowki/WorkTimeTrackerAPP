package com.worktimetrackerapp.GUI_Interfaces;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;


class PopUpWindows {
    boolean ended;
    DB app;
    private Database mydb;
    boolean editing;
    //variables
        Button btnDelete;
        Button btnEdit;
        Button btnDone;
    //task info
        TextView taskName;
        TextView startTaskInfo;
        TextView endTaskInfo;
        TextView clientName;
        TextView clientAddress;
        TextView wage;
    //other information
        TextView otherInfoStartedTask;
        TextView otherInfoEndedTask;
        TextView TaskExtraCost;
        TextView TaskEarnings;



    void showInfoPopup(final Document currentdoc, Activity myActif) throws Exception{
        app = (DB) myActif.getApplication();
        LayoutInflater inflater = myActif.getLayoutInflater();
        View layout = inflater.inflate(R.layout.loghistory_pop, null);
        float density =myActif.getResources().getDisplayMetrics().density;
        final PopupWindow pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
        ended = false;
        mydb = app.getMydb();

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


        DisableAllFields();

        //set text fields
            taskName.setText(currentdoc.getProperty("taskname").toString());
            String startTask = currentdoc.getProperty("TaskScheduledStartDate").toString() + " - " + currentdoc.getProperty("TaskScheduledStartTime");
            startTaskInfo.setText(startTask);
            String endTask = currentdoc.getProperty("TaskScheduledEndDate").toString() + " - " + currentdoc.getProperty("TaskScheduledEndTime");
            endTaskInfo.setText(endTask);
            clientName.setText(currentdoc.getProperty("taskClient").toString());
            clientAddress.setText(currentdoc.getProperty("ClientAddress").toString());
            wage.setText(currentdoc.getProperty("taskwage").toString());

            if(currentdoc.getProperty("TaskStartDateTime") != null) {
                otherInfoStartedTask.setText(currentdoc.getProperty("TaskStartDateTime").toString());
                ended = true;
            }
            if(currentdoc.getProperty("TaskEndDateTime") != null) {
                otherInfoEndedTask.setText(currentdoc.getProperty("TaskEndDateTime").toString());
                ended = true;
            }
            if(currentdoc.getProperty("extracost") != null) {
                TaskExtraCost.setText(currentdoc.getProperty("extracost").toString());
                ended = true;
            }
            if(currentdoc.getProperty("TaskEarnings") != null) {
                TaskEarnings.setText(currentdoc.getProperty("TaskEarnings").toString());
                ended = true;
            }

        //set on click listeners
        startTaskInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement
                if(editing) {
                    System.out.println("Clicked");
                }
            }
        });
        endTaskInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement
                if(editing) {
                    System.out.println("Clicked");
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

                    //save edited fields
                    //format some fields first
                    String startDate ="";
                    String startTime ="";
                    String endDate ="";
                    String endTime ="";
                    if (!ended) {//omit 4 fields
                        try {
                            app.UpdateTask(currentdoc, ended, taskName.getText().toString(), Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime,
                                    null, null, null, null);
                        } catch (Exception e){
                            System.out.println(e);
                        }
                    } else {//all fields
                        try {
                            app.UpdateTask(currentdoc, ended, taskName.getText().toString(), Double.parseDouble(wage.getText().toString()), clientName.getText().toString(), clientAddress.getText().toString(), startDate, startTime, endDate, endTime,
                                    otherInfoStartedTask.getText().toString(), otherInfoEndedTask.getText().toString(), Double.parseDouble(TaskExtraCost.getText().toString()), Double.parseDouble(TaskEarnings.getText().toString()));
                        } catch (Exception e){
                            System.out.println(e);
                        }
                    }
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Document task = (Document) mydb.getDocument(currentdoc.getId());
                try {
                    task.delete();
                    pw.dismiss();
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnEdit.getText().equals("Edit")) {
                    System.out.println("Test");
                    pw.dismiss();
                }
            }
        });
//set up touch closing outside of pop-up
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.getBackground().setAlpha(128);
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("Touch");
                System.out.println(event.getAction());
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
        otherInfoStartedTask.setFocusableInTouchMode(true);
        otherInfoEndedTask.setFocusableInTouchMode(true);
        TaskExtraCost.setFocusableInTouchMode(true);
        TaskEarnings.setFocusableInTouchMode(true);
    }
}
