package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryRow;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.AgendaArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

<<<<<<< Updated upstream
//calendar credit to https://github.com/prolificinteractive/material-calendarview/blob/master/docs/DECORATORS.md
//  and  https://www.youtube.com/watch?v=RN4Zmxlah_I
=======
>>>>>>> Stashed changes

public class Agenda_Controller extends Fragment {
    View currentView;
    private ListView agendalist;
    private AgendaArrayAdapter aaa;
    boolean ended;

    private Database mydb;
    private LiveQuery liveQuery;
    DB app;
    TextView agendaheader;

    com.couchbase.lite.View viewItemsByDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.agenda, container, false);
        agendalist = (ListView) currentView.findViewById(R.id.agenda_list_view);
        TextView emptyText = (TextView) currentView.findViewById(R.id.empty_agendalist);
        agendalist.setEmptyView(emptyText);
        app = (DB) getActivity().getApplication();
        agendaheader = new TextView(getContext());
        agendaheader.setTextSize(getResources().getDimension(R.dimen.listview_header));

<<<<<<< Updated upstream
        //Agenda items
                MaterialCalendarView materialCalendarView = (MaterialCalendarView) currentView.findViewById(R.id.calendarView);
=======
        final MaterialCalendarView materialCalendarView = (MaterialCalendarView) currentView.findViewById(R.id.calendarView);
>>>>>>> Stashed changes

                Calendar calendar = Calendar.getInstance();
                materialCalendarView.setSelectedDate(calendar.getTime());

                Calendar instance1 = Calendar.getInstance();
                instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);

                Calendar instance2 = Calendar.getInstance();
                instance2.set(instance2.get(Calendar.YEAR) + 2, Calendar.OCTOBER, 31);

                materialCalendarView.state().edit()
                        .setFirstDayOfWeek(Calendar.MONDAY)
                        .setMinimumDate(CalendarDay.from(1900, 1, 1))
                        .setMaximumDate(CalendarDay.from(2100, 12, 31))
                        .setCalendarDisplayMode(CalendarMode.MONTHS)
                        .commit();

        try {
            SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");
            String selectedDay = today.format(calendar.getTime());
            agendaheader.setText(selectedDay);
            agendalist.addHeaderView(agendaheader);
            agendaheader.setFocusable(false);
            startShowList();
            startLiveQuery(selectedDay);
        } catch (Exception e) {
            app.showErrorMessage("Error initializing CBLite", e);
        }

       materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // Toast.makeText(getActivity(), "" + date, Toast.LENGTH_SHORT).show();
                try {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    String selectedDay = dateFormatter.format(date.getDate());
                    agendalist.removeHeaderView(agendaheader);
                    agendaheader.setText(selectedDay);
                    agendalist.addHeaderView(agendaheader);
                    startLiveQuery(selectedDay);
                } catch (Exception e) {
                    DB app = (DB) getContext();
                    app.showErrorMessage("Error initializing CBLite", e);
                }

            }
        });

        materialCalendarView.addDecorators(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new DotSpan(Color.RED));
            }
        });
        return currentView;
    }

    protected void startShowList() throws Exception {
        DB app = (DB) getActivity().getApplication();
        mydb = app.getMydb();

        viewItemsByDate = mydb.getView("TaskScheduledStartDate");
        viewItemsByDate.setMap(new Mapper(){
            @Override
            public void map(Map<String, Object> document, Emitter emitter){
                if(document.get("type").equals("Task")) {
                    if(document.get("TaskScheduledStartDate") != null) {
                        String date = (String) document.get("TaskScheduledStartDate");
                        emitter.emit(date.toString(), null);
                    }
                }//end if
            }
        },"1");

        initItemListAdapter();
    }

    private void initItemListAdapter() {
        DB app = (DB) getActivity().getApplicationContext();
        aaa = new AgendaArrayAdapter(
                app,
                R.layout.agenda_row_layout,
                R.id.agenda_row_task_name,
                R.id.agenda_row_task_info,
                new ArrayList<QueryRow>()
        );
        agendalist.setAdapter(aaa);
        agendalist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(position != 0) {
                    QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
                    Document document = row.getDocument();
                    Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

                    try {
                        PopUpWindows ipp = new PopUpWindows();
                        ipp.showInfoPopup(document, getActivity());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }) ;
    }

    private void startLiveQuery(String SelectedDay) throws Exception {
        final DB app = (DB) getActivity().getApplication();
            Query MyQuery = viewItemsByDate.createQuery();
            MyQuery.setDescending(true);
            MyQuery.setStartKey(SelectedDay);
            MyQuery.setEndKey(SelectedDay);
            liveQuery = MyQuery.toLiveQuery();
            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    app.runOnUiThread(new Runnable() {
                        public void run() {
                            aaa.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
                                aaa.add(it.next());
                            }
                            aaa.notifyDataSetChanged();
                        }
                    });
                }
            });

            liveQuery.start();
    }
<<<<<<< Updated upstream
=======


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

>>>>>>> Stashed changes
}



