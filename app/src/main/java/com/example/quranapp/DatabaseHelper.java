package com.example.quranapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quran.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to implement this method since the database already exists
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle any changes to the database schema if needed
    }

    public void copyDatabaseFromAssets() {
        try {
            // Get the database file path in the application's data directory
            String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();

            // Check if the database already exists
            if (checkDatabaseExists(dbPath)) {
                Log.d(TAG, "Database already exists. Skipping copy from assets.");
                return;
            }

            // Make sure the database directory exists
            File file = new File(dbPath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Copy the database file from assets
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(DATABASE_NAME);
            OutputStream outputStream = new FileOutputStream(dbPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            Log.d(TAG, "Database copied from assets.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDatabaseExists(String dbPath) {
        File file = new File(dbPath);
        return file.exists();
    }

    public SQLiteDatabase openDatabase() {
        copyDatabaseFromAssets();
        return SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(),
                null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        // Close the database helper and release resources
        super.close();
    }
}
