package com.example.worktimetracker.worktimetrackerapp;

import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.ZipUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.Math.min;

public class WTTApplication extends android.app.Application {
    public static final String TAG = "WTTA";
    private String mSyncGatewayUrl = "http://10.0.2.2:4984/WTTA/";//Still needs to be changed

    private Manager manager;
    private Database database;
    private Replication pusher;
    private Replication puller;

    private String mUsername;

    public Database getDatabase() {return database;}
    public String getUsername() {return mUsername;}

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //if login is implemented
            //login();
        //else
            startSession("todo", null, null);

        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    private void startSession(String username, String password, String newPassword) {
        enableLogging();
        InitializeDB();
        openDatabase(username, password, newPassword);
        mUsername = username;
        startGateWayReplication(username, password);
        showApp();
    }

    private void showApp() {
        Intent intent = new Intent();
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        //first screen we go to defined here
        //intent.setClass(getApplicationContext(), ListsActivity.class);
        intent.putExtra("login_flow_enabled", true);
        startActivity(intent);
    }

    //****************************************************UserSignUp/SignIN********************************************************
    public void login(String username, String password) {
        mUsername = username;
        startSession(username, password, null);
    }

    private void login() {
        Intent intent = new Intent();
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        //intent.setClass(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void logout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopGateWayReplication();
                closeDatabase();
                login();
            }
        });
    }

    //****************************************************Database****************************************************
    private void InitializeDB() {
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            database = manager.getExistingDatabase(TAG);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        if (database == null) {
            try {
                ZipUtils.unzip(getAssets().open(TAG + ".zip"), manager.getContext().getFilesDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openDatabase(String username, String key, String newKey) {
        String dbname = username;
        DatabaseOptions options = new DatabaseOptions();
        options.setCreate(true);
        options.setEncryptionKey(key);

        Manager manager = null;
        try {
            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            database = manager.openDatabase(dbname, options);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        if (newKey != null) {
            try {
                database.changeEncryptionKey(newKey);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeDatabase() {
        database.close();
    }



    //****************************************************Sync Gateway/replication****************************************************
    private void startGateWayReplication(String username, String password) {
        URL url = null;
        try {
            url = new URL(mSyncGatewayUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ReplicationChangeListener changeListener = new ReplicationChangeListener(this);

        pusher = database.createPushReplication(url);
        pusher.setContinuous(true); // Runs forever in the background
        pusher.addChangeListener(changeListener);

        puller = database.createPullReplication(url);
        puller.setContinuous(true); // Runs forever in the background
        puller.addChangeListener(changeListener);

        //enable after login flow is set
            //Authenticator authenticator = AuthenticatorFactory.createBasicAuthenticator(username, password);
            //pusher.setAuthenticator(authenticator);
            //puller.setAuthenticator(authenticator);

        pusher.start();
        puller.start();
    }

    private void stopGateWayReplication() {
        pusher.stop();
        puller.stop();
    }


    //****************************************************Logging****************************************************
    private void enableLogging() {
        Manager.enableLogging(TAG, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_QUERY, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_VIEW, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_DATABASE, Log.VERBOSE);
    }



    //****************************************************extra classes****************************************************
    public void showErrorMessage(final String errorMessage, final Throwable throwable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.e(TAG, errorMessage, throwable);
                String msg = String.format("%s",
                        errorMessage);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        mainHandler.post(runnable);
    }
}