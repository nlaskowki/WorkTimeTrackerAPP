package com.worktimetrackerapp.util;


import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;
import com.worktimetrackerapp.WTTApplication;

public class DB extends WTTApplication {

    private static final String ENCRYPTION_KEY = "WTTA"; //has to be login token later
    public Manager dbManager = null;
    public Database Mydb = null;

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
    //**************************************************************************** push and pull to db ***************************************************









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
}
