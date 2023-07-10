package com.example.quranapp.QuranApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quranapp.DatabaseHelper;
import com.example.quranapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurahBookmarks extends AppCompatActivity {

    private ListView listView;
    private Button clearButton;
    private Button syncButton;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surah_bookmarks_activity);

        listView = findViewById(R.id.listBookmarks);
        clearButton = findViewById(R.id.clearButton);
        syncButton = findViewById(R.id.syncButton);

        // Create an instance of the database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences for offline storage
        sharedPreferences = getSharedPreferences("Bookmarks", MODE_PRIVATE);

        // Initialize the adapter with an empty list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());

        // Set the adapter to the ListView
        listView.setAdapter(adapter);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBookmarks();
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncBookmarks();
            }
        });

        // Check if the user is logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Retrieve and combine bookmarks from SharedPreferences and Firestore
        retrieveBookmarks();

        // Set item click listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bookmarkedItem = adapter.getItem(position);
                if (bookmarkedItem != null) {
                    openSurahReadActivity(bookmarkedItem);
                }
            }
        });
    }

    private void openSurahReadActivity(String bookmarkedItem) {
        String[] parts = bookmarkedItem.split(", ");
        if (parts.length == 2) {
            String surahName = parts[0].replace("Surah ", "");
            String ayahNumberString = parts[1].replace("Ayah ", "");
            int ayahNumber = Integer.parseInt(ayahNumberString);

            // Open SurahRead activity and pass the surah index and ayah number
            Intent intent = new Intent(SurahBookmarks.this, SurahRead.class);
            intent.putExtra("surahIndex", getSurahIndex(surahName));
            intent.putExtra("ayahNumber", ayahNumber);
            startActivity(intent);
        }
    }


    private int getSurahIndex(String surahName) {
        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.query("quran_surah", new String[]{"id"}, "latin = ?",
                new String[]{surahName}, null, null, null);

        int surahIndex = 0;
        if (cursor.moveToFirst()) {
            surahIndex = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();

        // Close the database
        db.close();

        return surahIndex;
    }


    private List<String> retrieveBookmarks() {
        Set<String> bookmarkedItems = new HashSet<>();

        // Retrieve bookmarks from SharedPreferences
        Map<String, ?> bookmarksMap = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : bookmarksMap.entrySet()) {
            String bookmarkKey = entry.getKey();
            int surahIndex = Integer.parseInt(bookmarkKey.replace("bookmark_", ""));
            int ayahNumber = (int) entry.getValue();

            String surahName = getSurahName(surahIndex);
            String bookmarkedItem = "Surah " + surahName + ", Ayah " + ayahNumber;
            bookmarkedItems.add(bookmarkedItem);
        }

        // Retrieve bookmarks from Firestore
        if (userId != null) {
            CollectionReference bookmarksCollection = firestore.collection("bookmarks").document(userId).collection("surahs");
            bookmarksCollection.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            int surahIndex = document.getLong("surahIndex").intValue();
                            int ayahNumber = document.getLong("ayahNumber").intValue();

                            String surahName = getSurahName(surahIndex);
                            String bookmarkedItem = "Surah " + surahName + ", Ayah " + ayahNumber;
                            bookmarkedItems.add(bookmarkedItem);
                        }

                        // Update the ListView with combined bookmarks
                        adapter.clear();
                        adapter.addAll(bookmarkedItems);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch bookmarks from Firestore", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update the ListView with bookmarks from SharedPreferences only
            adapter.clear();
            adapter.addAll(bookmarkedItems);
            adapter.notifyDataSetChanged();
        }

        return new ArrayList<>(bookmarkedItems);
    }



    private String getSurahName(int surahIndex) {
        // Open the existing database
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.query("quran_surah", new String[]{"latin"}, "id = ?",
                new String[]{String.valueOf(surahIndex)}, null, null, null);

        String surahName = "";
        if (cursor.moveToFirst()) {
            surahName = cursor.getString(cursor.getColumnIndex("latin"));
        }
        cursor.close();

        // Close the database
        db.close();

        return surahName;
    }

    private void saveBookmark(int surahIndex, int ayahNumber) {
        boolean useFirebase = true; // Set this flag based on user preference

        if (useFirebase) {
            saveBookmarkToFirestore(surahIndex, ayahNumber);
        } else {
            saveBookmarkToSharedPreferences(surahIndex, ayahNumber);

            // Add the bookmarked item to the adapter
            String surahName = getSurahName(surahIndex);
            String bookmarkedItem = "Surah " + surahName + ", Ayah " + ayahNumber;
            adapter.add(bookmarkedItem);
            adapter.notifyDataSetChanged();
        }
    }

    private void saveBookmarkToFirestore(int surahIndex, int ayahNumber) {
        if (firebaseAuth.getCurrentUser() == null) {
            // User not logged in, show login dialog
            showLoginDialog();
            return;
        }

        Map<String, Object> bookmarkData = new HashMap<>();
        bookmarkData.put("surahIndex", surahIndex);
        bookmarkData.put("ayahNumber", ayahNumber);

        CollectionReference bookmarksCollection = firestore.collection("bookmarks").document(userId).collection("surahs");
        bookmarksCollection.add(bookmarkData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SurahBookmarks.this, "Bookmark synced successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SurahBookmarks.this, "Failed to sync bookmark", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveBookmarkToSharedPreferences(int surahIndex, int ayahNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("bookmark_" + surahIndex, ayahNumber);
        editor.apply();
    }

    private void clearBookmarks() {
        // Show a confirmation dialog before clearing bookmarks
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to clear all bookmarks?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearBookmarksFromSharedPreferences();
                clearBookmarksFromFirestore();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearBookmarksFromSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Clear the adapter and update the ListView
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    private void clearBookmarksFromFirestore() {
        if (userId != null) {
            CollectionReference bookmarksCollection = firestore.collection("bookmarks").document(userId).collection("surahs");
            bookmarksCollection.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot document : documents) {
                            bookmarksCollection.document(document.getId()).delete();
                        }

                        Toast.makeText(this, "Bookmarks cleared successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to clear bookmarks", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void syncBookmarks() {
        if (firebaseAuth.getCurrentUser() == null) {
            // User not logged in, show login dialog
            showLoginDialog();
            return;
        }

        // Retrieve bookmarks from SharedPreferences
        Map<String, ?> bookmarksMap = sharedPreferences.getAll();

        if (bookmarksMap.isEmpty()) {
            Toast.makeText(this, "No bookmarks to sync", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve bookmarks from Firestore
        CollectionReference bookmarksCollection = firestore.collection("bookmarks").document(userId).collection("surahs");
        bookmarksCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookmarkedItems = new ArrayList<>();
                    Set<String> firestoreBookmarks = new HashSet<>();

                    // Open the existing database
                    SQLiteDatabase db = dbHelper.openDatabase();

                    // Fetch existing bookmarks from Firestore
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        int surahIndex = document.getLong("surahIndex").intValue();
                        int ayahNumber = document.getLong("ayahNumber").intValue();

                        String surahName = getSurahName(surahIndex);
                        String bookmarkedItem = "Surah " + surahName + ", Ayah " + ayahNumber;
                        bookmarkedItems.add(bookmarkedItem);

                        // Add Firestore bookmarks to a set
                        firestoreBookmarks.add(surahIndex + "_" + ayahNumber);
                    }

                    // Close the database
                    db.close();

                    // Add new bookmarks from SharedPreferences to Firestore and the bookmarked items list
                    for (Map.Entry<String, ?> entry : bookmarksMap.entrySet()) {
                        String bookmarkKey = entry.getKey();
                        int surahIndex = Integer.parseInt(bookmarkKey.replace("bookmark_", ""));
                        int ayahNumber = (int) entry.getValue();

                        String surahName = getSurahName(surahIndex);
                        String bookmarkedItem = "Surah " + surahName + ", Ayah " + ayahNumber;

                        // Check for duplicates between SharedPreferences and Firestore
                        if (!firestoreBookmarks.contains(surahIndex + "_" + ayahNumber)) {
                            // Add the new bookmark to Firestore
                            saveBookmarkToFirestore(surahIndex, ayahNumber);
                            // Add the new bookmark to the bookmarked items list
                            bookmarkedItems.add(bookmarkedItem);
                        }
                    }

                    // Update the ListView with combined bookmarks
                    adapter.clear();
                    adapter.addAll(bookmarkedItems);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, "Bookmarks synced successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to sync bookmarks", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SurahBookmarks.this);
        builder.setTitle("Login Required");
        builder.setMessage("Please login to sync your bookmarks");
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Start the login activity
                startActivity(new Intent(SurahBookmarks.this, SurahLoginActivity.class));
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
