package com.worktimetrackerapp;


import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.worktimetrackerapp.GUI_Interfaces.SignIn_Controller;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class DB extends android.app.Application implements Replication.ChangeListener{
    private static final String SYNC_URL_HTTP = "http://wttuser.axelvh.com/wttdb/_session";
    private static final String SYNC_URL_HTTP_Database= "http://wttuser.axelvh.com/wttdb/";
    private static final String USER_LOCAL_DOC_ID = "user";
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final String DATABASE_NAME = "wttdb";

    private Manager dbManager = null;
    private com.couchbase.lite.Database Mydb = null;
    private final Gson gson = new Gson();
    private AndroidContext context;

    private ReplicationChangeHandler changeHandler = null;
    private Replication pull;
    private Replication push;
    private Throwable syncError;
    private String username;



    private void completeLogin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    //*************************************************************** Google Authentication ******************************************
    public void loginWithGoogleSignIn(final String idToken) {
        Request request = new Request.Builder()
                .url(SYNC_URL_HTTP)
                .header("Authorization", "Bearer " + idToken)
                .post(new FormBody.Builder().build())
                .build();


        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                showErrorMessage("Failed to create a new SGW session with IDToken : " + idToken, e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Succes");
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> session = gson.fromJson(response.body().charStream(), type);
                    Map<String, Object> userInfo = (Map<String, Object>) session.get("userCtx");
                    final String username = (userInfo != null ? (String) userInfo.get("name") : null);
                    System.out.println(username);
                    final List<Cookie> cookies = Cookie.parseAll(HttpUrl.get(new URL("http://wttuser.axelvh.com/wttdb/")), response.headers());
                    if (login(username, cookies)) {
                        completeLogin();
                   }
                }
            }
        });
    }

    private boolean login(String username, final List<Cookie> sessionCookies) {
        if (login(username)) {
            startPull(new ReplicationSetupCallback() {
                @Override
                public void setup(Replication repl) {
                    for (Cookie cookie : sessionCookies) {
                        repl.setCookie(cookie.name(), cookie.value(), cookie.path(),
                                new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
                    }
                }
            });
            startPush(new ReplicationSetupCallback() {
                @Override
                public void setup(Replication repl) {
                    for (Cookie cookie : sessionCookies) {
                        repl.setCookie(cookie.name(), cookie.value(), cookie.path(),
                                new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
                    }
                }
            });
            return true;
        }
        return false;
    }

    private boolean login(String username) {
        if (username == null)
            return false;

        if (Mydb != null) {
            Map<String, Object> user = Mydb.getExistingLocalDocument(USER_LOCAL_DOC_ID);
            if (user != null && !username.equals(user.get("username"))) {
                stopReplication(false);
                try {
                    Mydb.delete();
                } catch (CouchbaseLiteException e) {
                    return false;
                }
                Mydb = null;
            }
        }

        if (Mydb == null) {
            if (!initializeDatabase())
                return false;
        }

        Map<String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("username", username);
        try {
            Mydb.putLocalDocument(USER_LOCAL_DOC_ID, userInfo);
        } catch (CouchbaseLiteException e) {
            return false;
        }

        this.username = username;

        return true;
    }


    //************************************************************** Sync Gateway ***********************************************
    interface ReplicationSetupCallback {
        void setup(Replication repl);
    }

    interface ReplicationChangeHandler {
        void change(Replication repl);
    }

    //replication and sync
        private void startPull(ReplicationSetupCallback callback) {
            pull = Mydb.createPullReplication(getSyncUrl());
            pull.setContinuous(true);

            if (callback != null) callback.setup(pull);

            pull.addChangeListener(this);
            pull.start();
        }

        private void startPush(ReplicationSetupCallback callback) {
            push = Mydb.createPushReplication(getSyncUrl());
            push.setContinuous(true);

            if (callback != null) callback.setup(push);

            push.addChangeListener(this);
            push.start();
        }

        private void stopReplication(boolean removeCredentials) {
            this.changeHandler = null;

            if (pull != null) {
                pull.stop();
                pull.removeChangeListener(this);
                if (removeCredentials)
                    pull.clearAuthenticationStores();
                pull = null;
            }

            if (push != null) {
                push.stop();
                push.removeChangeListener(this);
                if (removeCredentials)
                    push.clearAuthenticationStores();
                push = null;
            }
        }

        private URL getSyncUrl() {
            URL url = null;
            try {
                url = new URL(SYNC_URL_HTTP_Database);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Invalid sync url", e);
            }
            return url;
        }

        @Override
        public void changed(Replication.ChangeEvent event) {
        Replication repl = event.getSource();
        android.util.Log.d(TAG, "Replication Change Status: " + repl.getStatus() + " [ " + repl  + " ]");

        if (changeHandler != null)
            changeHandler.change(repl);

        Throwable error = null;
        if (pull != null)
            error = pull.getLastError();

        if (push != null) {
            if (error == null)
                error = push.getLastError();
        }

        if (error != syncError) {
            syncError = error;
            showErrorMessage(syncError.getMessage(), null);
        }
    }



    //*************************************************************** DB **********************************************
    public String getUsername() {
        return username;
    }

    public com.couchbase.lite.Database getDatabase() {
        return Mydb;
    }

    public boolean initializeDatabase() {
        enableLogging();
        if (dbManager == null) {
            try {
                context = new AndroidContext(getApplicationContext());
                dbManager = new Manager(context, Manager.DEFAULT_OPTIONS);
            } catch (IOException e) {
                android.util.Log.e(TAG, "Couldn't create manager object", e);
                return false;
            }
        }

        if (Mydb == null) {
            DatabaseOptions options = new DatabaseOptions();
            options.setStorageType(Manager.SQLITE_STORAGE);
            options.setCreate(true);
            try {
                Mydb = dbManager.openDatabase(DATABASE_NAME, options);
            } catch (CouchbaseLiteException e) {
                android.util.Log.e(TAG, "Couldn't open database", e);
                return false;
            }
        }
        return true;
    }

    public Database getMydb(){
        return Mydb;
    }

    //**************************************************** logout *******************************************************

    public void logout() {
        stopReplication(true);

        this.username = null;

        Intent intent = new Intent(getApplicationContext(), SignIn_Controller.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(SignIn_Controller.INTENT_ACTION_LOGOUT);
        startActivity(intent);
    }

    //******logging********

    private void enableLogging() {
            Manager.enableLogging(TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);

    }

    //display error messages from db
    public void showErrorMessage(final String errorMessage, final Throwable throwable) {
        android.util.Log.e(TAG, errorMessage, throwable);
        String msg = String.format("%s: %s",
                errorMessage, throwable != null ? throwable : "");
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void runOnUiThread(Runnable runnable) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        mainHandler.post(runnable);
    }
}
