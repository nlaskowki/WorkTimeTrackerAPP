package com.worktimetrackerapp.util;


import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.worktimetrackerapp.WTTApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
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

public class DB extends WTTApplication {
    private static final String SYNC_URL_HTTP = "http://wttuser.axelvh.com/wttdb/_session";
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final String ENCRYPTION_KEY = "WTTA"; //has to be login token later

    public Manager dbManager = null;
    public Database Mydb = null;
    private final Gson gson = new Gson();
    private Replication pull;
    private Replication push;
    private Throwable syncError;


    //*************************************************************** gateway start ******************************************
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
                   // if (login(username, cookies)) {
                        //completeLogin();
                    //}
                }
            }
        });
    }


    //*************************************************************** DB setup **********************************************
    private Manager StartManager(){
        try{
            AndroidContext context = new AndroidContext(getApplicationContext());
            dbManager = new Manager(context, Manager.DEFAULT_OPTIONS);
        }catch (Exception e){
                Log.e(TAG, "Cannot create Manager object", e);
            }
        return dbManager;
    }

    public Database getDatabase() {
        return Mydb;
    }

    private void setDatabase(Database database) {
        this.Mydb = database;
    }

    private Database getUserDatabase(String name) {
        try {
            String dbName = "db" + StringUtil.MD5(name);
            DatabaseOptions options = new DatabaseOptions();
            options.setCreate(true);
            options.setStorageType(Manager.SQLITE_STORAGE);
            options.setEncryptionKey(ENCRYPTION_KEY);
            return StartManager().openDatabase(dbName, options);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot create database for name: " + name, e);
        }
        return null;
    }
    //**************************************************************************** push and pull to syncgateway ***************************************************








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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.e(TAG, errorMessage, throwable);
                String msg = String.format("%s: %s",
                        errorMessage, throwable != null ? throwable : "");
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
