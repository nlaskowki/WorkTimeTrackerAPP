package com.worktimetrackerapp.GUI_Interfaces;


import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
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
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;
import com.worktimetrackerapp.util.AgendaArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class Agenda_Controller extends Fragment {
    View currentView;
    private ListView agendalist;
    private AgendaArrayAdapter aaa;

    private Database mydb;
    private LiveQuery liveQuery;

    public static final String designDocName = "Task";
    public static final String byDateViewName = "byDate";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.agenda, container, false);
        agendalist = (ListView) currentView.findViewById(R.id.agenda_list_view);
        DB app = (DB) getActivity().getApplication();

        //calendar credit to https://github.com/prolificinteractive/material-calendarview/blob/master/docs/DECORATORS.md
                    //  and  https://www.youtube.com/watch?v=RN4Zmxlah_I

        MaterialCalendarView materialCalendarView = (MaterialCalendarView) currentView.findViewById(R.id.calendarView);

        // Add a decorator to disable prime numbered days
        materialCalendarView.addDecorator(new PrimeDayDisableDecorator());

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

       materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Toast.makeText(getActivity(), "" + date, Toast.LENGTH_SHORT).show();
            }
        });

        try {
            //app.StartTask("1", "" ,0.0, "ou", 0.0, "");
            //app.StartTask("2", "" ,0.0, "ou", 0.0, "");
            //app.StartTask("3", "" ,0.0, "ou", 0.0, "");
            startShowList();
        } catch (Exception e) {
            //DB app = (DB) getContext();
            app.showErrorMessage("Error initializing CBLite", e);
        }

        return currentView;
    }

    private static class PrimeDayDisableDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return PRIME_TABLE[day.getDay()];
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }

        private static boolean[] PRIME_TABLE = {
                false,  // 0?
                false,
                true, // 2
                true, // 3
                false,
                true, // 5
                false,
                true, // 7
                false,
                false,
                false,
                true, // 11
                false,
                true, // 13
                false,
                false,
                false,
                true, // 17
                false,
                true, // 19
                false,
                false,
                false,
                true, // 23
                false,
                false,
                false,
                false,
                false,
                true, // 29
                false,
                true, // 31
                false,
                false,
                false, //PADDING
        };
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
                QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
                Document document = row.getDocument();
                Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

                try {
                    showPopup(document);
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        }) ;
    }

    private void startLiveQuery(com.couchbase.lite.View view) throws Exception {
        final DB app = (DB) getActivity().getApplication();

        if (liveQuery == null) {
            liveQuery = view.createQuery().toLiveQuery();
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
    }


    public void showPopup(final Document currentdoc) throws Exception{
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.loghistory_pop, null);

        //calculate size of popup
        float density =getActivity().getResources().getDisplayMetrics().density;
        final PopupWindow pw = new PopupWindow(layout, (int)density*400, (int)density*600,true);

        final Button btnDelete = (Button) layout.findViewById(R.id.popup_deletetask);
        final Button btnEdit = (Button) layout.findViewById(R.id.popup_edittask);
        final Button btnDone = (Button) layout.findViewById(R.id.popup_donetask);
        final TextView task = (TextView) layout.findViewById(R.id.popup_task);
        final TextView start = (TextView) layout.findViewById(R.id.popup_start);
        final TextView end = (TextView) layout.findViewById(R.id.popup_end);
        final TextView client = (TextView) layout.findViewById(R.id.popup_client);
        final TextView wage = (TextView) layout.findViewById(R.id.popup_wage);

        //disable textfields
        task.setFocusable(false);
        start.setFocusable(false);
        end.setFocusable(false);
        client.setFocusable(false);
        wage.setFocusable(false);
        //set text fields
        task.setText(currentdoc.getProperty("taskname").toString());


        //set on click listeners
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
        btnEdit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(btnEdit.getText().equals("Edit")) {
                    //rename edit button
                    btnEdit.setText("Save");
                    btnEdit.getText();
                    btnDelete.setVisibility(View.INVISIBLE);
                    btnDone.setVisibility(View.INVISIBLE);
                    //enable textfields
                    task.setFocusableInTouchMode(true);
                    start.setFocusableInTouchMode(true);
                    end.setFocusableInTouchMode(true);
                    client.setFocusableInTouchMode(true);
                    wage.setFocusableInTouchMode(true);
                }else{
                    //disable textfields
                    btnEdit.setText("Edit");
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDone.setVisibility(View.VISIBLE);
                    task.setFocusable(false);
                    start.setFocusable(false);
                    end.setFocusable(false);
                    client.setFocusable(false);
                    wage.setFocusable(false);
                    //save edited items
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
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("Touch");
                System.out.println(event.getAction());
                System.out.println("Action outside");
                System.out.println(MotionEvent.ACTION_OUTSIDE);
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    System.out.println("Test");
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });
        pw.setOutsideTouchable(true);
        pw.showAtLocation(layout, Gravity.CENTER, 0,0);
    }
}



