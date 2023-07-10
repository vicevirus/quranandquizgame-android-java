package com.example.quranapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quranapp.QuizGame.QuizAppActivity;
import com.example.quranapp.QuranApp.QuranAppActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnQuranApp;
    private Button btnIslamQuizApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the buttons in the layout
        btnQuranApp = findViewById(R.id.btnQuranApp);
        btnIslamQuizApp = findViewById(R.id.btnIslamQuizApp);

        // Set click listeners for the buttons
        btnQuranApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openQuranApp();
            }
        });

        btnIslamQuizApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openIslamQuizApp();
            }
        });
    }

    private void openQuranApp() {
        // Launch the Quran App activity
        Intent intent = new Intent(this, QuranAppActivity.class);
        startActivity(intent);
    }

    private void openIslamQuizApp() {
        // Launch the Islam Quiz App activity
        Intent intent = new Intent(this, QuizAppActivity.class);
        startActivity(intent);
    }
}
