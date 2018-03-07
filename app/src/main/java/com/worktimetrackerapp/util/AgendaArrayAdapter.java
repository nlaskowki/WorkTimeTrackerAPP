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

public class AgendaArrayAdapter extends ArrayAdapter<QueryRow>{
    private List<QueryRow> historylist;
    private final Context context;

    public AgendaArrayAdapter(Context context, int resource, int tasknameResourceId, int taskinfoResourceId ,List<QueryRow> objects){
        super(context, resource, tasknameResourceId, objects);
        this.context = context;
    }

    private static class ViewHolder{
        TextView taskname;
        TextView taskinfo;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent){
        if(itemView ==null){
            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.agenda_row_layout, null);
            ViewHolder vh = new ViewHolder();
            vh.taskname = (TextView) itemView.findViewById(R.id.agenda_row_task_name);
            vh.taskinfo = (TextView) itemView.findViewById(R.id.agenda_row_task_info);
            itemView.setTag(vh);
        }

        try {
            TextView taskname = ((ViewHolder)itemView.getTag()).taskname;
            TextView taskinfo = ((ViewHolder)itemView.getTag()).taskinfo;

            QueryRow row = getItem(position);
            SavedRevision currentRevision = row.getDocument().getCurrentRevision();

            String strtaskname = (String) currentRevision.getProperty("taskname");
            taskname.setText(strtaskname);

            String strtaskinfo = (String) currentRevision.getProperty("created_at");
            taskinfo.setText(strtaskinfo);


        } catch (Exception e){
            e.printStackTrace();
        }

        return itemView;
    }
}
