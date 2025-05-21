package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private final List<Note> notesList;
    private final Context context;
    private final NoteActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface NoteActionListener {
        void onNoteClick(Note note);
        void onNoteEdit(Note note);
        void onNoteDelete(Note note);
    }

    public NotesAdapter(Context context, List<Note> notesList, NoteActionListener listener) {
        this.context = context;
        this.notesList = notesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void updateNotes(List<Note> newNotes) {
        notesList.clear();
        notesList.addAll(newNotes);
        notifyDataSetChanged();
    }

    public void addNote(Note note) {
        notesList.add(0, note);
        notifyItemInserted(0);
    }

    public void updateNote(Note updatedNote) {
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getId().equals(updatedNote.getId())) {
                notesList.set(i, updatedNote);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void deleteNote(String noteId) {
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getId().equals(noteId)) {
                notesList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView previewTextView;
        private final ImageButton menuButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Match the IDs from note_item_layout.xml
            titleTextView = itemView.findViewById(R.id.noteTitle);
            dateTextView = itemView.findViewById(R.id.noteDate);
            previewTextView = itemView.findViewById(R.id.notePreview);
            menuButton = itemView.findViewById(R.id.noteMenuButton);
        }

        public void bind(final Note note) {
            titleTextView.setText(note.getTitle());
            dateTextView.setText(dateFormat.format(note.getModifiedDate()));

            // Create a preview of the content (first 50 characters or less)
            String content = note.getContent();
            String preview = content.length() > 50 ?
                    content.substring(0, 50) + "..." : content;
            previewTextView.setText(preview);

            // Set click listener for the whole item to view full note
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            // Set click listener for the menu button (three dots)
            menuButton.setOnClickListener(v -> showPopupMenu(menuButton, note));
        }

        private void showPopupMenu(View view, Note note) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.note_options_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onNoteEdit(note);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onNoteDelete(note);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}