package com.worktimetrackerapp.util;


public class CurrentJob {
    private Object CJ = null;
    private OnObjectChangeListener listener;

    public void setOnObjectChangeListener(OnObjectChangeListener listener){
        this.listener = listener;
    }

    public Object getcurrentJob(){
        return this.CJ;
    }

    public void setCurrentJob(Object currentjob){
        this.CJ = currentjob;

        if(listener != null){
            listener.OnObjectChanged(currentjob);
        }
    }

}
