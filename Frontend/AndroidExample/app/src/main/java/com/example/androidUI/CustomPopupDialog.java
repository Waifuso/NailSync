package com.example.androidUI;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.androidexample.R;

public class CustomPopupDialog extends DialogFragment {

    private String title, message;

    public CustomPopupDialog(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom_popup, null);
        dialog.setContentView(view);

        // Find views
        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView messageView = view.findViewById(R.id.dialog_message);
        Button closeButton = view.findViewById(R.id.btn_close);

        // Set text
        titleView.setText(title);
        messageView.setText(message);

        // Close button action
        closeButton.setOnClickListener(v -> dismiss());

        // Apply animation
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade1_in);
        view.startAnimation(fadeIn);

        // Set background transparency
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }
}
