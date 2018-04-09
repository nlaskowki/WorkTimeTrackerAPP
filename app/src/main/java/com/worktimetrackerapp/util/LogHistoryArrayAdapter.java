package com.worktimetrackerapp.util;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;
import com.worktimetrackerapp.DB;
import com.worktimetrackerapp.R;

import java.util.List;

public class LogHistoryArrayAdapter extends ArrayAdapter<QueryRow>{
    private DB app;

    public LogHistoryArrayAdapter(Context context, int resource, int tasknameResourceId, int taskjobtitleResourceId, int taskinfoResourceId , List<QueryRow> objects){
        super(context, resource, tasknameResourceId, objects);
        app = (DB) (context);
    }

    private static class ViewHolder{
        TextView taskname;
        TextView taskinfo;
        TextView taskjob;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent){
        if(itemView ==null){
            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.loghistory_row_layout, null);
            ViewHolder vh = new ViewHolder();
            vh.taskname = itemView.findViewById(R.id.loghistory_row_task_name);
            vh.taskjob =  itemView.findViewById(R.id.loghistory_row_task_jobtitle);
            vh.taskinfo = itemView.findViewById(R.id.loghistory_row_task_info);
            itemView.setTag(vh);
        }

        try {
            TextView taskname = ((ViewHolder)itemView.getTag()).taskname;
            TextView taskjob = ((ViewHolder)itemView.getTag()).taskjob;
            TextView taskinfo = ((ViewHolder)itemView.getTag()).taskinfo;


            QueryRow row = getItem(position);
            SavedRevision currentRevision = row.getDocument().getCurrentRevision();

            String strtaskname = (String) currentRevision.getProperty("taskname");
            taskname.setText(strtaskname);
            String strtaskjob;
            try {
                String title = app.getMydb().getDocument(currentRevision.getProperty("jobtitle").toString()).getProperty("jobtitle").toString();
                String company = app.getMydb().getDocument(currentRevision.getProperty("jobtitle").toString()).getProperty("jobcompany").toString();
                strtaskjob = title + " @ " + company;
            }catch (Exception e){
                strtaskjob = "Job removed!";
            }
            taskjob.setText(strtaskjob);

            String TaskScheduledStartDate =(String) currentRevision.getProperty("TaskScheduledStartDate") ;
            String TaskScheduledStartTime =(String) currentRevision.getProperty("TaskScheduledStartTime") ;
            String TaskScheduledEndDate =(String) currentRevision.getProperty("TaskScheduledEndDate") ;
            String TaskScheduledEndTime =(String) currentRevision.getProperty("TaskScheduledEndTime") ;
            if(!TaskScheduledStartDate.isEmpty() && !TaskScheduledEndDate.isEmpty()) {
                String strtaskinfo = "empty";
                if (TaskScheduledStartDate.equals(TaskScheduledEndDate)) {
                    strtaskinfo = TaskScheduledStartDate + " - Time: " + TaskScheduledStartTime + " - " + TaskScheduledEndTime;
                } else {
                    strtaskinfo = TaskScheduledStartDate + " - " + TaskScheduledStartTime + " To: " + TaskScheduledEndDate + " - " + TaskScheduledEndTime;
                }
                taskinfo.setText(strtaskinfo);
                taskinfo.setText(strtaskinfo);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return itemView;
    }
}
