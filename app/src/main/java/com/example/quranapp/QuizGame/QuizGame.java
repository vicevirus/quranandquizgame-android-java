package com.example.quranapp.QuizGame;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import com.example.quranapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class QuizGame extends Activity {

    private TextView questionTextView;
    private Button optionATextView;
    private Button optionBTextView;
    private Button optionCTextView;
    private Button optionDTextView;

    private MediaPlayer correctSoundPlayer;
    private MediaPlayer incorrectSoundPlayer;


    private TextView highScoreTextView;

    private Button endButton;
    private int highScore = 0;

    private String[] questions;
    private String[][] answerOptions;
    private String[] answers;

    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_game);

        questionTextView = findViewById(R.id.questionTextView);
        optionATextView = findViewById(R.id.optionATextView);
        optionBTextView = findViewById(R.id.optionBTextView);
        optionCTextView = findViewById(R.id.optionCTextView);
        optionDTextView = findViewById(R.id.optionDTextView);
        highScoreTextView = findViewById(R.id.highScoreTextView);
        endButton = findViewById(R.id.endButton);

        // Load questions and answers from JSON file
        loadQuizDataFromJson();

        displayQuestion();

        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_answer); // Correct sound
        incorrectSoundPlayer = MediaPlayer.create(this, R.raw.wrong_answer); // Incorrect sound






        // Set click listener for the end button
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start GameOverActivity
                Intent gameOverIntent = new Intent(QuizGame.this, QuizGameOver.class);
                gameOverIntent.putExtra("score", highScore);
                startActivity(gameOverIntent);
                finish(); // Close the current activity
            }
        });
        optionATextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(optionATextView.getText().toString());
            }
        });

        optionBTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(optionBTextView.getText().toString());
            }
        });

        optionCTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(optionCTextView.getText().toString());
            }
        });

        optionDTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(optionDTextView.getText().toString());
            }
        });
    }


    private void loadQuizDataFromJson() {
        try {
            // Read the JSON file from assets folder
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("Questions.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonContent = new String(buffer, StandardCharsets.UTF_8);

            // Parse the JSON data
            JSONArray jsonArray = new JSONArray(jsonContent);
            JSONObject questionsObject = jsonArray.getJSONObject(0);
            JSONObject answerOptionsObject = jsonArray.getJSONObject(1);
            JSONObject answersObject = jsonArray.getJSONObject(2);

            Log.d("MainActivity", String.valueOf(questionsObject.length()));
            Log.d("MainActivity", String.valueOf(answerOptionsObject.length()));
            Log.d("MainActivity", String.valueOf(answersObject.length()));

            // Extract questions
            questions = new String[questionsObject.length()];
            for (int i = 0; i < questionsObject.length(); i++) {
                String questionNumber = String.valueOf(i + 1);
                questions[i] = questionsObject.getString(questionNumber);
            }

            // Extract answer options
            answerOptions = new String[answerOptionsObject.length()][4];
            for (int i = 0; i < answerOptionsObject.length(); i++) {
                String questionNumber = String.valueOf(i + 1);
                JSONObject optionsObject = answerOptionsObject.getJSONObject(questionNumber);
                answerOptions[i][0] = optionsObject.getString("a");
                answerOptions[i][1] = optionsObject.getString("b");
                answerOptions[i][2] = optionsObject.getString("c");
                answerOptions[i][3] = optionsObject.getString("d");
            }

            // Extract answers
            answers = new String[answersObject.length()];
            for (int i = 0; i < answersObject.length(); i++) {
                String questionNumber = String.valueOf(i + 1);
                answers[i] = answersObject.getString(questionNumber);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.length) {
            questionTextView.setText(questions[currentQuestionIndex]);
            optionATextView.setText(answerOptions[currentQuestionIndex][0]);
            optionBTextView.setText(answerOptions[currentQuestionIndex][1]);
            optionCTextView.setText(answerOptions[currentQuestionIndex][2]);
            optionDTextView.setText(answerOptions[currentQuestionIndex][3]);
            highScoreTextView.setText("High Score: " + highScore); // Update the high score TextView
        } else {
            // Quiz completed, show high score or perform any desired action
            Toast.makeText(this, "Quiz completed! High score: " + highScore, Toast.LENGTH_SHORT).show();

            // Start GameOverActivity
            Intent gameOverIntent = new Intent(QuizGame.this, QuizGameOver.class);
            gameOverIntent.putExtra("score", highScore); // Pass the actual high score
            startActivity(gameOverIntent);
            finish(); // Close the current activity
        }
    }



    private void checkAnswer(String selectedAnswer) {
        String correctAnswer = answers[currentQuestionIndex];
        if (selectedAnswer.equals(correctAnswer)) {
            Toast.makeText(this, "Correct answer!", Toast.LENGTH_SHORT).show();
            highScore += 5; // Increase high score by 5 points

            // Play correct sound
            if (correctSoundPlayer != null) {
                correctSoundPlayer.start();
            }
        } else {
            Toast.makeText(this, "Wrong answer!", Toast.LENGTH_SHORT).show();

            // Play incorrect sound
            if (incorrectSoundPlayer != null) {
                incorrectSoundPlayer.start();
            }
        }

        currentQuestionIndex++;
        displayQuestion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }

        if (incorrectSoundPlayer != null) {
            incorrectSoundPlayer.release();
            incorrectSoundPlayer = null;
        }
    }


}
