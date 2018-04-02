package com.worktimetrackerapp.util;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;
import com.worktimetrackerapp.R;

import java.util.List;

public class JobArrayAdapter extends ArrayAdapter<QueryRow>{
    private List<QueryRow> historylist;
    private final Context context;
    //String displayjobinfo = "empty";
   // String displayjobwage = "empty";

    public JobArrayAdapter(Context context, int resource, int jobTitleResourceId, int hourlywageResourceId , List<QueryRow> objects){
        super(context, resource, jobTitleResourceId, objects);
        this.context = context;
    }

    private static class ViewHolder{
        TextView jobTitle;
        TextView hourlywage;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent){
        if(itemView ==null){
            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.agenda_row_layout, null);
            ViewHolder vh = new ViewHolder();
            vh.jobTitle = itemView.findViewById(R.id.agenda_row_task_name);
            vh.hourlywage = itemView.findViewById(R.id.agenda_row_task_info);
            itemView.setTag(vh);
        }

        try {
            TextView jobTitle = ((ViewHolder)itemView.getTag()).jobTitle;
            TextView hourlywage = ((ViewHolder)itemView.getTag()).hourlywage;

            QueryRow row = getItem(position);
            SavedRevision currentRevision = row.getDocument().getCurrentRevision();

            String displayjobinfo = (String) currentRevision.getProperty("jobTitle");
            jobTitle.setText(displayjobinfo);

            String displayjobwage = (String)currentRevision.getProperty("hourlywage");
            hourlywage.setText(displayjobwage);


        } catch (Exception e){
            e.printStackTrace();
        }

        return itemView;
    }
}
