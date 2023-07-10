package com.example.quranapp.QuranApp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.quranapp.DatabaseHelper;
import com.example.quranapp.R;
import com.google.android.material.textview.MaterialTextView;

public class SurahRead extends Activity {

    private Button btnSearch;
    private EditText editTextAyahNumber;
    private ScrollView scrollView;
    private int screenHeight;

    private LinearLayout linearLayoutAyahs;
    private DatabaseHelper dbHelper;
    private Typeface customTypeface;
    private int surahIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surah_read_activity);




        btnSearch = findViewById(R.id.btnSearch);
        editTextAyahNumber = findViewById(R.id.editTextAyahNumber);
        scrollView = findViewById(R.id.scrollView);
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAyah();
            }
        });



        // Load the custom font
        customTypeface = Typeface.createFromAsset(getAssets(), "me_quran.ttf");

        // Get the surah index passed from the previous activity
        surahIndex = getIntent().getIntExtra("surahIndex", 0);

        linearLayoutAyahs = findViewById(R.id.linearLayoutAyahs);

        // Create an instance of the database helper
        dbHelper = new DatabaseHelper(this);

        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        // Retrieve the Surah name
        Cursor surahCursor = db.rawQuery("SELECT latin FROM quran_surah WHERE id = ?", new String[]{String.valueOf(surahIndex)});
        if (surahCursor != null && surahCursor.moveToFirst()) {
            String surahName = surahCursor.getString(surahCursor.getColumnIndex("latin"));
            setTitle(surahName); // Set activity title to the Surah name
            surahCursor.close();
        }

        String query = "SELECT * FROM quran_text WHERE sura = ?";
        String[] selectionArgs = new String[]{String.valueOf(surahIndex)};
        Cursor ayahCursor = db.rawQuery(query, selectionArgs);
        if (ayahCursor != null && ayahCursor.moveToFirst()) {
            do {
                int ayahNumber = ayahCursor.getInt(ayahCursor.getColumnIndex("aya"));
                String ayahText = ayahCursor.getString(ayahCursor.getColumnIndex("text"));

                createAyahCard(ayahNumber, ayahText);

            } while (ayahCursor.moveToNext());

            ayahCursor.close();
        }

        // Close the database
        db.close();


        // Scroll to the specific verse (ayah) if provided in the intent
        int ayahNumber = getIntent().getIntExtra("ayahNumber", -1);
        if (ayahNumber != -1) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollToAyah(ayahNumber);
                }
            });
        }


    }

    private int lastSearchedAyah = -1;

    private void searchAyah() {
        String input = editTextAyahNumber.getText().toString().trim();
        if (!input.isEmpty()) {
            int ayahNumber = Integer.parseInt(input);
            if (ayahNumber != lastSearchedAyah) {
                scrollToAyah(ayahNumber);
                lastSearchedAyah = ayahNumber; // Update the last searched ayah
            }
        }
    }




    private void scrollToAyah(int ayahNumber) {
        View ayahView = linearLayoutAyahs.findViewWithTag(ayahNumber);
        if (ayahView != null) {
            int[] location = new int[2];
            linearLayoutAyahs.getLocationOnScreen(location);
            int parentTop = location[1];
            ayahView.getLocationOnScreen(location);
            int ayahTop = location[1];
            int desiredScrollY = ayahTop - parentTop;
            scrollView.smoothScrollTo(0, desiredScrollY);
        } else {
            Toast.makeText(this, "Ayah not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAyahCard(int ayahNumber, String ayahText) {
        // Create a CardView for the ayah number
        CardView numberCardView = new CardView(this);
        numberCardView.setTag(ayahNumber); // Assign a tag to the card
        LinearLayout.LayoutParams numberCardLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        numberCardLayoutParams.width = getResources().getDimensionPixelSize(R.dimen.number_card_width); // Set the desired width
        numberCardLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT; // Set the height to match the ayah text
        numberCardLayoutParams.setMargins(16, 0, 16, 0); // Add margins as needed
        numberCardView.setLayoutParams(numberCardLayoutParams);
        numberCardView.setCardBackgroundColor(getResources().getColor(R.color.number_card_background_color));
        numberCardView.setElevation(4);

        // Create a RelativeLayout to hold the ayah number and bookmark button
        RelativeLayout ayahNumberContainer = new RelativeLayout(this);
        RelativeLayout.LayoutParams ayahNumberContainerLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ayahNumberContainerLayoutParams.setMargins(0, 0, 0, 32); // Add bottom margin to create space for the bookmark button
        ayahNumberContainer.setLayoutParams(ayahNumberContainerLayoutParams);

        // Create a MaterialTextView for the ayah number
        MaterialTextView tvAyahNumber = new MaterialTextView(this);
        RelativeLayout.LayoutParams numberLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        numberLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvAyahNumber.setLayoutParams(numberLayoutParams);
        tvAyahNumber.setText(String.valueOf(ayahNumber));
        tvAyahNumber.setTextSize(20);
        tvAyahNumber.setTextColor(getResources().getColor(android.R.color.black));

        // Create an ImageButton for the bookmark button
        ImageButton bookmarkButton = new ImageButton(this);
        RelativeLayout.LayoutParams bookmarkButtonLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bookmarkButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bookmarkButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bookmarkButton.setLayoutParams(bookmarkButtonLayoutParams);
        bookmarkButton.setImageResource(R.drawable.bookmark_selected); // Set the bookmark icon


        // Add the click listener to the bookmark button
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the bookmarked verse in SharedPreferences
                saveBookmark(ayahNumber, surahIndex);
                Toast.makeText(SurahRead.this, "Verse bookmarked", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the ayah number to the RelativeLayout
        ayahNumberContainer.addView(tvAyahNumber);

        // Add the bookmark button to the RelativeLayout
        ayahNumberContainer.addView(bookmarkButton);

        // Add the ayah number container to the number card
        numberCardView.addView(ayahNumberContainer);

        // Create a CardView for the ayah text
        CardView textCardView = new CardView(this);
        LinearLayout.LayoutParams textCardLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textCardLayoutParams.setMargins(0, 16, 0, 16); // Add 16 pixels of top and bottom margin
        textCardView.setLayoutParams(textCardLayoutParams);
        textCardView.setCardBackgroundColor(getResources().getColor(R.color.card_background_color));
        textCardView.setRadius(8);
        textCardView.setElevation(8);

        // Create a MaterialTextView for the ayah text
        MaterialTextView tvAyahText = new MaterialTextView(this);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.setMargins(16, 16, 16, 16); // Add 16 pixels of margin on all sides
        tvAyahText.setLayoutParams(textLayoutParams);
        tvAyahText.setText(ayahText);
        tvAyahText.setTextSize(36);
        tvAyahText.setTextColor(getResources().getColor(R.color.ayah_text_color));
        tvAyahText.setTypeface(customTypeface); // Set the custom font

        // Add the ayah text to the text card
        textCardView.addView(tvAyahText);

        // Create a LinearLayout to hold the number card and text card horizontally
        LinearLayout cardContainer = new LinearLayout(this);
        LinearLayout.LayoutParams containerLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardContainer.setLayoutParams(containerLayoutParams);
        cardContainer.setOrientation(LinearLayout.HORIZONTAL);
        cardContainer.addView(numberCardView);
        cardContainer.addView(textCardView);

        // Add the card container to the linear layout
        linearLayoutAyahs.addView(cardContainer);

        CardView translationCardView = new CardView(this);
        LinearLayout.LayoutParams translationCardLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        translationCardLayoutParams.setMargins(0, 8, 0, 0); // Add 8 pixels of top margin
        translationCardView.setLayoutParams(translationCardLayoutParams);
        translationCardView.setCardBackgroundColor(getResources().getColor(R.color.darker_gray)); // Set a slightly darker gray color
        translationCardView.setRadius(8);
        translationCardView.setElevation(8);

        // Create a MaterialTextView for the translation text
        MaterialTextView tvTranslationText = new MaterialTextView(this);
        LinearLayout.LayoutParams translationLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        translationLayoutParams.setMargins(16, 8, 16, 8); // Add 8 pixels of margin on top and bottom
        tvTranslationText.setLayoutParams(translationLayoutParams);

        // Retrieve the translation text from the database
        String translationText = getTranslationText(ayahNumber);
        tvTranslationText.setText(translationText);
        tvTranslationText.setTextSize(18); // Decrease the text size for a lighter appearance
        tvTranslationText.setTextColor(getResources().getColor(R.color.lighter_gray)); // Set a lighter gray color

        // Add the translation text to the translation card
        translationCardView.addView(tvTranslationText);

        // Add the translation card to the linear layout
        linearLayoutAyahs.addView(translationCardView);
    }



    private String getTranslationText(int ayahNumber) {
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor translationCursor = db.rawQuery("SELECT text FROM en_hilali WHERE sura = ? AND aya = ?", new String[]{String.valueOf(surahIndex), String.valueOf(ayahNumber)});
        String translationText = "";
        if (translationCursor != null && translationCursor.moveToFirst()) {
            translationText = translationCursor.getString(translationCursor.getColumnIndex("text"));
            translationCursor.close();
        }
        db.close();
        return translationText;
    }


    private void saveBookmark(int ayahNumber, int surahIndex) {
        SharedPreferences sharedPreferences = getSharedPreferences("Bookmarks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the bookmarked verse using a unique key
        String bookmarkKey = "bookmark_" + surahIndex;
        editor.putInt(bookmarkKey, ayahNumber);
        editor.apply();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database helper when the activity is destroyed
        dbHelper.close();
    }
}
