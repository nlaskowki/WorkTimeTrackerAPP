package com.worktimetrackerapp;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private OkHttpClient httpClient = new OkHttpClient();
    private static final String DATABASE_NAME = "wttdb";

    private Manager dbManager = null;
    private Database Mydb = null;
    private final Gson gson = new Gson();
    private AndroidContext context;

    private ReplicationChangeHandler changeHandler = null;
    private Replication pull;
    private Replication push;
    private Throwable syncError;
    private String username;

    private Document[] Jobs = new Document[10];
    private Document currentJob;

    public Document getcurrentJob(){
        return currentJob;
    }
    public void setCurrentJob(Document CJ){
        currentJob = CJ;
    }

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

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES).writeTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES);
        httpClient = builder.build();
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

    //************************************************************** add, remove, update the DB ****************************************************

    public boolean getAllJobs() throws Exception{
        for(int count=0; count<10; count++){
                Jobs[count] = null;
                System.out.println(count);
        }

        //set view
        View  jobview = getMydb().getView("User-Info");
        jobview.setMap(new Mapper(){
            @Override
            public void map(Map<String, Object> document, Emitter emitter){
                List<String> jobs = (List) document.get("User-Info");
                for (String job : jobs){
                    emitter.emit(job, document.get("type"));
                }
            }
        },"1");
        System.out.println("Run 1");
        Query query = getMydb().getView("jobview").createQuery();
        query.setStartKey("User-Info");
        QueryEnumerator result = query.run();
        System.out.println("Run 1");
        int i = 0;
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            System.out.println("Run 2");
            QueryRow row = it.next();
            Jobs[i] = row.getDocument();
            i++;
            Log.w("wtt", "On %s: order for $%f", row.getKey(), ((Double)row.getValue()).doubleValue());
        }
        if(!Jobs[0].equals(null)) {
            currentJob = Jobs[0];
            System.out.println("WORKS");
        }
        return true;
    }

    protected Document AddJob(String jobType, String jobTitle, String jobEmployer, Float jobWage, Float jobAveHours) throws Exception {
        DB app = (DB) getApplicationContext();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        UUID uuid = UUID.randomUUID();
        Calendar calendar = GregorianCalendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String id = currentTime + "-" + uuid.toString();

        Document document = getMydb().createDocument();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("_id", id);
        properties.put("type", "User-info");
        properties.put("owner", app.getUsername());
        properties.put("created_at", currentTimeString);

        properties.put("jobtype", jobType);
        properties.put("jobtitle", jobTitle);
        properties.put("jobemployer", jobEmployer);
        properties.put("jobwage", jobWage);
        properties.put("jobavehours", jobAveHours);





        document.putProperties(properties);

        Log.d(TAG, "Created new user item with id: %s", document.getId());

        return document;
    }

    public Document NewTask(String TaskName, String JobTitle ,Double TaskWage, String Client, String CAddress, String StartDate, String StartTime, String EndDate, String EndTime) throws Exception {
        DB app = (DB) getApplicationContext();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        UUID uuid = UUID.randomUUID();
        Calendar calendar = GregorianCalendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String id = currentTime + "-" + uuid.toString();

        Document document = getMydb().createDocument();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("_id", id);
        properties.put("type", "Task");
        properties.put("owner", app.getUsername());
        properties.put("created_at", currentTimeString);

        properties.put("taskname", TaskName);
        properties.put("jobtitle", JobTitle);
        properties.put("taskwage", TaskWage);

        properties.put("taskClient", Client);
        properties.put("ClientAddress", CAddress);

        properties.put("TaskScheduledStartDate", StartDate);
        properties.put("TaskScheduledStartTime", StartTime);

        properties.put("TaskScheduledEndDate", EndDate);
        properties.put("TaskScheduledEndTime", EndTime);


        document.putProperties(properties);

        Log.d(TAG, "Started task item with id: %s", document.getId());

        return document;
    }

    public Boolean StartTask(Document TaskDoc) throws Exception {

        //get date format
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd - HH:mm");
        Calendar calendar = GregorianCalendar.getInstance();
        String StartTime = dateFormatter.format(calendar.getTime());

        Document document = getMydb().getDocument(TaskDoc.getId());
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(TaskDoc.getProperties());

        properties.put("TaskStartTime", StartTime);

        document.putProperties(properties);

        Log.d(TAG, "Started task item with id: %s", document.getId());

        return true;
    }

    public Boolean EndTask(Document taskdocument, Double ExtraCosts, Double TaskEarnings) throws Exception {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd - HH:mm");
        Calendar calendar = GregorianCalendar.getInstance();
        String TaskEndTime = dateFormatter.format(calendar.getTime());


        Document doc = getMydb().getDocument(taskdocument.getId());
        Map<String, Object> properties = new HashMap<String, Object>();
            properties.putAll(doc.getProperties());

            properties.put("TaskEndTime", TaskEndTime);
            properties.put("extracost", ExtraCosts);
            properties.put("TaskEarnings", TaskEarnings);

        try {
            doc.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Ended task item with id: %s", taskdocument.getId());

        return true;
    }


    //**************************************************** logout *******************************************************
    public void logout() {

        stopReplication(true);
        this.username = null;
        try {
            Mydb.delete();
            Mydb = null;
        } catch (Exception e){
            Log.e(TAG, "cannot delete database", e);
        }

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
    public void runOnUiThread(Runnable runnable) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        mainHandler.post(runnable);
    }
}
