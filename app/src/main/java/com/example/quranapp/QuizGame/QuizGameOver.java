package com.example.quranapp.QuizGame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.quranapp.QuizGame.QuizAppActivity;
import com.example.quranapp.R;

public class QuizGameOver extends Activity {

    private TextView scoreTextView;
    private Button restartButton;

    private int score;
    private int highestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_game_over);

        scoreTextView = findViewById(R.id.scoreTextView);
        restartButton = findViewById(R.id.restartButton);

        // Get the score from the intent
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        // Set the score text
        scoreTextView.setText("Score: " + score);

        // Load the highest score from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        highestScore = preferences.getInt("highest_score", 0);

        // Check if the current score is higher than the highest score
        if (score > highestScore) {
            // Save the new highest score
            highestScore = score;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("highest_score", highestScore);
            editor.apply();
        }

        // Set click listener for the restart button
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the QuizAppActivity to restart the game
                Intent restartIntent = new Intent(QuizGameOver.this, QuizAppActivity.class);
                startActivity(restartIntent);
                finish(); // Close the GameOverActivity
            }
        });
    }
}
