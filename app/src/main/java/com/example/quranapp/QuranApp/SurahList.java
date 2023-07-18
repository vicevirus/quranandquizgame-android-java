package com.example.quranapp.QuranApp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

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
        Cursor cursor = db.rawQuery("SELECT id, latin, english FROM quran_surah", null);
        if (cursor != null && cursor.moveToFirst()) {
            int indexColumn = cursor.getColumnIndex("id");
            int surahNameColumn = cursor.getColumnIndex("latin");
            int englishNameColumn = cursor.getColumnIndex("english");
            do {
                int surahIndex = cursor.getInt(indexColumn);
                String surahName = cursor.getString(surahNameColumn);
                String englishName = cursor.getString(englishNameColumn);
                addCardView(surahIndex, surahName, englishName);
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

    private void addCardView(final int surahIndex, String latinText, String englishText) {
        // Create a ContextThemeWrapper with the custom card style
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(this, R.style.CustomCardStyle);

        // Create the CardView using the context wrapper
        CardView cardView = new CardView(contextWrapper);
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background_color));
        cardView.setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        cardView.setRadius(getResources().getDimension(R.dimen.card_corner_radius));

        // Set the CardView's layout params
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16); // Add 16 pixels of bottom margin
        cardView.setLayoutParams(layoutParams);

        // Set OnClickListener to launch SurahDetailsActivity with the selected index
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SurahList.this, SurahRead.class);
                intent.putExtra("surahIndex", surahIndex);
                startActivity(intent);
            }
        });

        // Create a vertical LinearLayout for the content
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        // Create the TextView for the 'latin' text
        TextView textViewLatin = new TextView(this);
        textViewLatin.setText(latinText);
        textViewLatin.setGravity(Gravity.CENTER);
        textViewLatin.setPadding(16, 16, 16, 16);
        textViewLatin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        textViewLatin.setTypeface(null, Typeface.BOLD);
        // Set custom padding values in pixels
        int paddingStart = 24; // Adjust the padding value as needed
        int paddingTop = 16; // Adjust the padding value as needed
        int paddingEnd = 24; // Adjust the padding value as needed
        int paddingBottom = 0; // No bottom padding for the 'latin' text
        textViewLatin.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);

        // Add the 'latin' TextView to the content layout
        contentLayout.addView(textViewLatin);

        // Create the TextView for the 'english' text
        TextView textViewEnglish = new TextView(this);
        textViewEnglish.setText(englishText);
        textViewEnglish.setGravity(Gravity.CENTER);
        textViewEnglish.setPadding(16, 0, 16, 16);
        textViewEnglish.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        // Add the 'english' TextView to the content layout
        contentLayout.addView(textViewEnglish);

        // Add the content layout to the CardView
        cardView.addView(contentLayout);

        // Add the CardView to the linear layout
        linearLayoutSurahs.addView(cardView);
    }


    private void performSearch(String query) {
        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        // Perform the database query with the search query
        String[] selectionArgs = new String[]{"%" + query + "%"};
        Cursor cursor = db.rawQuery("SELECT * FROM quran_surah WHERE latin LIKE ?", selectionArgs);

        // Clear the existing surah CardViews
        linearLayoutSurahs.removeAllViews();

        // Create new surah CardViews based on the search results
        if (cursor != null && cursor.moveToFirst()) {
            int indexColumn = cursor.getColumnIndex("id");
            int surahNameColumn = cursor.getColumnIndex("latin");
            int englishColumn = cursor.getColumnIndex("english");
            do {
                int surahIndex = cursor.getInt(indexColumn);
                String surahName = cursor.getString(surahNameColumn);
                String englishName = cursor.getString(englishColumn);
                addCardView(surahIndex, surahName, englishName);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Close the database
        db.close();
    }
}
