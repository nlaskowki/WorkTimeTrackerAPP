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

    public static final String designDocName = "Task";
    public static final String byDateViewName = "byDate";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.log_history, container, false);
        HistoryList = (ListView) currentView.findViewById(R.id.log_history_list);
        DB app = (DB) getActivity().getApplication();

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
        //boolean checked = ((Boolean) newProperties.get("check")).booleanValue();
        //newProperties.put("check", !checked);

       // try {
            //document.putProperties(newProperties);
            //lhaa.notifyDataSetChanged();
        //} catch (Exception e) {
           // DB app = (DB) getContext();
            //app.showErrorMessage("Error updating database", e);
        //}
    }

    public void showPopup(final Document currentdoc) throws Exception{
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        /*btnDelete.setOnClickListener(new View.OnClickListener(){
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
        });*/


        //set up touch closing outside of pop-up
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
                public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                        System.out.println("Test");
                        //pw.dismiss();
                        return true;
                }
                return false;
            }
        });
        pw.setOutsideTouchable(true);
        pw.showAtLocation(layout,Gravity.CENTER, 0,0);
    }
}
