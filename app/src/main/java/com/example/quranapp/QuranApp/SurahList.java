package com.example.quranapp.QuranApp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.appcompat.view.ContextThemeWrapper;

import com.example.quranapp.DatabaseHelper;
import com.example.quranapp.R;

public class SurahList extends Activity {

    private LinearLayout linearLayoutSurahs;
    private DatabaseHelper dbHelper;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_surahs);

        linearLayoutSurahs = findViewById(R.id.linearLayoutSurahs);
        searchView = findViewById(R.id.searchView);

        // Set up the back button click listener
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        // Create an instance of the database helper
        dbHelper = new DatabaseHelper(this);

        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        // Perform a database query to fetch all surahs
        Cursor cursor = db.rawQuery("SELECT * FROM quran_surah", null);
        if (cursor != null && cursor.moveToFirst()) {
            int indexColumn = cursor.getColumnIndex("id");
            int surahNameColumn = cursor.getColumnIndex("latin");
            do {
                int surahIndex = cursor.getInt(indexColumn);
                String surahName = cursor.getString(surahNameColumn);
                addButton(surahIndex, surahName);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Close the database
        db.close();

        // Set up a listener for the search view to perform the search

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // If the search query is empty, show all surahs
                    performSearch("");
                } else {
                    // Perform the search with the new query
                    performSearch(newText);
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database helper when the activity is destroyed
        dbHelper.close();
    }

    private void addButton(final int surahIndex, String text) {
        // Create a ContextThemeWrapper with the custom button style
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(this, R.style.CustomButtonStyle);

        // Create the button using the context wrapper
        Button button = new Button(contextWrapper, null, 0);

        // Set the button's layout params, text, and padding
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16); // Add 16 pixels of bottom margin
        button.setLayoutParams(layoutParams);
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setPadding(16, 16, 16, 16);

        // Set OnClickListener to launch SurahDetailsActivity with the selected index
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SurahList.this, SurahRead.class);
                intent.putExtra("surahIndex", surahIndex);
                startActivity(intent);
            }
        });

        // Add the button to the linear layout
        linearLayoutSurahs.addView(button);
    }

    private void performSearch(String query) {
        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        // Perform the database query with the search query
        String[] selectionArgs = new String[]{"%" + query + "%"};
        Cursor cursor = db.rawQuery("SELECT * FROM quran_surah WHERE latin LIKE ?", selectionArgs);

        // Clear the existing surah buttons
        linearLayoutSurahs.removeAllViews();

        // Create new surah buttons based on the search results
        if (cursor != null && cursor.moveToFirst()) {
            int indexColumn = cursor.getColumnIndex("id");
            int surahNameColumn = cursor.getColumnIndex("latin");
            do {
                int surahIndex = cursor.getInt(indexColumn);
                String surahName = cursor.getString(surahNameColumn);
                addButton(surahIndex, surahName);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Close the database
        db.close();
    }
}
