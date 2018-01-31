package com.example.worktimetracker.worktimetrackerapp;

import android.util.Log;

import com.couchbase.lite.replicator.RemoteRequestResponseException;
import com.couchbase.lite.replicator.Replication;

class ReplicationChangeListener implements Replication.ChangeListener {

    private WTTApplication application;

    ReplicationChangeListener(WTTApplication application) {
        this.application = application;
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        if (event.getError() != null) {
            Throwable lastError = event.getError();
            Log.d(WTTApplication.TAG, String.format("Replication Error: %s", lastError.getMessage()));
            if (lastError instanceof RemoteRequestResponseException) {
                RemoteRequestResponseException exception = (RemoteRequestResponseException) lastError;
                if (exception.getCode() == 401) {
                    application.showErrorMessage("Your username or password is not correct.", null);
                    application.logout();
                }
            }
        }
    }
}
