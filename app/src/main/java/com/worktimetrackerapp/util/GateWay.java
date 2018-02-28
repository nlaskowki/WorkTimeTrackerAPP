package com.worktimetrackerapp.util;

import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.worktimetrackerapp.WTTApplication;

import java.net.MalformedURLException;
import java.net.URL;


public class GateWay extends DB implements Replication.ChangeListener{

    private static final String SYNC_URL_HTTP = "http://wttuser.axelvh.com/wttdb/";
    private Replication dbPull;
    private Replication dbPush;
    private Throwable dbReplError;
    
    private URL getSyncUrl() {
        URL url = null;
        try {
            url = new URL(SYNC_URL_HTTP);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid sync url", e);
        }
        return url;
    }

    private void startReplication(Authenticator auth) {
        if (dbPull == null) {
            dbPull = Mydb.createPullReplication(getSyncUrl());
            dbPull.setContinuous(true);
            dbPull.setAuthenticator(auth);
            dbPull.addChangeListener(this);
        }

        if (dbPush == null) {
            dbPush = Mydb.createPushReplication(getSyncUrl());
            dbPush.setContinuous(true);
            dbPush.setAuthenticator(auth);
            dbPush.addChangeListener(this);
        }

        dbPull.stop();
        dbPull.start();

        dbPush.stop();
        dbPush.start();
    }

    private void stopReplication() {
        if (dbPull != null) {
            dbPull.removeChangeListener(this);
            dbPull.stop();
            dbPull = null;
        }

        if (dbPush != null) {
            dbPush.removeChangeListener(this);
            dbPush.stop();
            dbPush = null;
        }
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        Throwable error = null;
        if (dbPull != null) {
            if (error == null)
                error = dbPull.getLastError();
        }

        if (error == null || error == dbReplError)
            error = dbPush.getLastError();

        if (error != dbReplError) {
            dbReplError = error;
            if (dbReplError != null)
                showErrorMessage(dbReplError.getMessage(), null);
        }
    }

}
