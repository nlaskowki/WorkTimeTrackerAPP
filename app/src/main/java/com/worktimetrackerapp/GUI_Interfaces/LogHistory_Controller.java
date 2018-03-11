package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.MainActivity;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.LogHistoryArrayAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Delayed;

public class LogHistory_Controller extends Fragment implements AdapterView.OnItemClickListener{
    View currentView;
    private ListView HistoryList;
    private LogHistoryArrayAdapter lhaa;

    private Database mydb;
    private LiveQuery liveQuery;
    boolean ended;
    DB app;
    public static final String designDocName = "Task";
    public static final String byDateViewName = "byDate";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.log_history, container, false);
        HistoryList = (ListView) currentView.findViewById(R.id.log_history_list);
        app = (DB) getActivity().getApplication();

        try {
            startShowList();
        } catch (Exception e) {
            app.showErrorMessage("Error initializing CBLite", e);
        }

        return currentView;
    }

    protected void startShowList() throws Exception {
        DB app = (DB) getActivity().getApplication();
        mydb = app.getMydb();

        com.couchbase.lite.View viewItemsByDate =
                mydb.getView(String.format("%s/%s", designDocName, byDateViewName));
        if (viewItemsByDate.getMap() == null) {
            viewItemsByDate.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    Object createdAt = document.get("created_at");
                    if(document.get("type").equals("Task")) {
                        if (createdAt != null) {
                            emitter.emit(createdAt.toString(), null);
                        }
                    }
                }
            }, "1.0");
        }

        initItemListAdapter();

        startLiveQuery(viewItemsByDate);
    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        lhaa = new LogHistoryArrayAdapter(
                app,
                R.layout.loghistory_row_layout,
                R.id.row_task_name,
                R.id.row_task_info,
                new ArrayList<QueryRow>()
        );
        HistoryList.setAdapter(lhaa);
        HistoryList.setOnItemClickListener(LogHistory_Controller.this);
    }

    private void startLiveQuery(com.couchbase.lite.View view) throws Exception {
        final DB app = (DB) getActivity().getApplication();

        if (liveQuery == null) {
            liveQuery = view.createQuery().toLiveQuery();
            liveQuery.setDescending(true);
            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    app.runOnUiThread(new Runnable() {
                        public void run() {
                            lhaa.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
                                lhaa.add(it.next());
                            }
                            lhaa.notifyDataSetChanged();
                        }
                    });
                }
            });

            liveQuery.start();
        }
    }

    //Handle click on item in list
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        Document document = row.getDocument();
        Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

        try {
            showPopup(document);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void showPopup(final Document currentdoc) throws Exception{
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.loghistory_pop, null);
        float density =getActivity().getResources().getDisplayMetrics().density;
        final PopupWindow pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);
        ended = false;

        //set fields from popup
            final Button btnDelete = (Button) layout.findViewById(R.id.popup_deletetask);
            final Button btnEdit = (Button) layout.findViewById(R.id.popup_edittask);
            final Button btnDone = (Button) layout.findViewById(R.id.popup_donetask);
            //task info
            final TextView taskName = (TextView) layout.findViewById(R.id.popup_taskname);
            final TextView startTaskInfo = (TextView) layout.findViewById(R.id.popup_startdatetime);
            final TextView endTaskInfo = (TextView) layout.findViewById(R.id.popup_enddatetime);
            final TextView clientName = (TextView) layout.findViewById(R.id.popup_clientname);
            final TextView clientAddress = (TextView) layout.findViewById(R.id.popup_clientaddress);
            final TextView wage = (TextView) layout.findViewById(R.id.popup_wagehr);
            //other information
            final TextView otherInfoStartedTask = (TextView) layout.findViewById(R.id.popup_startedtask);
            final TextView otherInfoEndedTask = (TextView) layout.findViewById(R.id.popup_endedtask);
            final TextView TaskExtraCost = (TextView) layout.findViewById(R.id.popup_extracosts);
            final TextView TaskEarnings = (TextView) layout.findViewById(R.id.popup_earnings);


        //disable textfields
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
        //set text fields
            taskName.setText(currentdoc.getProperty("taskname").toString());
            //startTaskInfo.setText(currentdoc.getProperty("").toString());
            //endTaskInfo.setText(currentdoc.getProperty("").toString());
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
        btnEdit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnEdit.getText().equals("Edit")) {
                    //rename edit button
                        btnEdit.setText("Save");
                        btnEdit.getText();
                        btnDelete.setVisibility(View.INVISIBLE);
                        btnDone.setVisibility(View.INVISIBLE);
                    //enable textfields
                        taskName.setFocusableInTouchMode(true);
                        startTaskInfo.setFocusableInTouchMode(true);
                        endTaskInfo.setFocusableInTouchMode(true);
                        clientName.setFocusableInTouchMode(true);
                        clientAddress.setFocusableInTouchMode(true);
                        wage.setFocusableInTouchMode(true);
                        otherInfoStartedTask.setFocusableInTouchMode(true);
                        otherInfoEndedTask.setFocusableInTouchMode(true);
                        TaskExtraCost.setFocusableInTouchMode(true);
                        TaskEarnings.setFocusableInTouchMode(true);
                }else {//buttontext is equal to save
                    //disable textfields
                        btnEdit.setText("Edit");
                        btnDelete.setVisibility(View.VISIBLE);
                        btnDone.setVisibility(View.VISIBLE);

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
        pw.showAtLocation(layout,Gravity.CENTER, 0,0);
    }
}
