package com.example.quranapp.QuranApp;// MainActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quranapp.R;


public class QuranAppActivity extends AppCompatActivity {

    private Button btnSurahList;
    private Button btnBookmarkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quran_app);

        btnSurahList = findViewById(R.id.btnSurahList);
        btnBookmarkList = findViewById(R.id.btnBookmarkSurah);
        btnSurahList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SurahList activity when the button is clicked
                Intent intent = new Intent(QuranAppActivity.this, SurahList.class);
                startActivity(intent);
            }
        });

        btnBookmarkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start BookmarkList activity when the button is clicked
                Intent intent = new Intent(QuranAppActivity.this, SurahBookmarks.class);
                startActivity(intent);
            }
        });
    }
}
