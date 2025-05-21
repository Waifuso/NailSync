package com.example.androidexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidUI.Note;
import com.example.androidUI.NotesAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmployeeNotesActivity extends AppCompatActivity implements NotesAdapter.NoteActionListener {

    private NotesAdapter notesAdapter;
    private List<Note> notesList;
    private RecyclerView recyclerView;
    private AlertDialog noteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_notes);

        // Initialize components
        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notesList = new ArrayList<>();

        // Add some sample notes if needed for testing
        // Comment out or remove in production
        addSampleNotes();

        // Set up adapter
        notesAdapter = new NotesAdapter(this, notesList, this);
        recyclerView.setAdapter(notesAdapter);

        // Set up FAB for adding new notes
        FloatingActionButton addNoteFab = findViewById(R.id.addNoteFab);
        addNoteFab.setOnClickListener(v -> showNoteDialog(null));

        // Set up search button
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            // Implement search functionality
            // This is a placeholder for future implementation
        });
    }

    // Sample notes for testing
    private void addSampleNotes() {
        notesList.add(new Note(UUID.randomUUID().toString(), "Welcome Note", "This is your notes section where you can add important reminders and information."));
        notesList.add(new Note(UUID.randomUUID().toString(), "Appointment Notes", "Any important information you need to add like colors, choices, etc."));
    }

    private void showNoteDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.note_dialog_layout, null);
        builder.setView(dialogView);

        // Get references to dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText titleInput = dialogView.findViewById(R.id.noteTitleInput);
        EditText contentInput = dialogView.findViewById(R.id.noteContentInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        // Set dialog title and button text based on whether we're editing or creating
        boolean isEdit = note != null;
        if (isEdit) {
            dialogTitle.setText("Edit Note");
            titleInput.setText(note.getTitle());
            contentInput.setText(note.getContent());
            saveButton.setText("Update");
        } else {
            dialogTitle.setText("Add New Note");
            saveButton.setText("Save");
        }

        // Create and show the dialog
        noteDialog = builder.create();
        noteDialog.show();

        // Set up button click listeners
        cancelButton.setOnClickListener(v -> noteDialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (title.isEmpty()) {
                titleInput.setError("Title cannot be empty");
                return;
            }

            if (isEdit) {
                // Update existing note
                note.setTitle(title);
                note.setContent(content);
                note.updateModifiedDate();
                notesAdapter.updateNote(note);
            } else {
                // Create new note
                Note newNote = new Note(UUID.randomUUID().toString(), title, content);
                notesAdapter.addNote(newNote);
                recyclerView.smoothScrollToPosition(0);
            }

            noteDialog.dismiss();
        });
    }

    private void showDeleteConfirmation(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    notesAdapter.deleteNote(note.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onNoteClick(Note note) {
        showNoteDialog(note);
    }

    @Override
    public void onNoteEdit(Note note) {
        showNoteDialog(note);
    }

    @Override
    public void onNoteDelete(Note note) {
        showDeleteConfirmation(note);
    }
}