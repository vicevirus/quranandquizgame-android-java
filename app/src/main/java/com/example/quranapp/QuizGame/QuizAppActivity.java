package com.example.quranapp.QuizGame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.quranapp.R;

public class QuizAppActivity extends Activity {

    private TextView highScoreTextView;
    private Button playButton;
    private int highScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        highScoreTextView = findViewById(R.id.highScoreTextView);
        playButton = findViewById(R.id.playButton);

        // Load the highest score from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = preferences.getInt("highest_score", 0);

        // Set the high score text
        highScoreTextView.setText("High Score: " + highScore);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start QuizGame activity when the button is clicked
                Intent intent = new Intent(QuizAppActivity.this, QuizGame.class);
                startActivity(intent);
            }
        });
    }

    // Other methods and logic specific to your Quran App activity
}
