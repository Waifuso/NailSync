package com.example.androidUI;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidUI.AppointmentModel;
import com.example.androidexample.R;
import com.example.androidexample.VolleySingleton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppointmentDetailsDialog extends BottomSheetDialogFragment {

    private static final String TAG = "AppointmentDetailsDialog";
    private static final String BASE_URL = "http://coms-3090-020.class.las.iastate.edu:8080/api";
    private static final String CANCEL_ENDPOINT = "/appointments/cancel/";
    private static final String RESCHEDULE_ENDPOINT = "/appointments/reschedule/";
    private static final int REQUEST_CODE_RESCHEDULE = 102;

    private AppointmentModel appointment;
    private TextView tvTechnicianName, tvDateValue, tvTimeValue, tvServicesValue;
    private TextView tvDurationValue, tvStatusValue, tvNotesValue, tvNotesLabel;
    private Button btnClose, btnReschedule, btnCancel;
    private ImageButton btnEditTime;
    private View statusIndicator;
    private LinearLayout layoutTime, layoutEditTime;
    private EditText etNewTime;
    private Button btnSaveTime, btnCancelEdit;

    private AppointmentActionListener actionListener;

    public interface AppointmentActionListener {
        void onAppointmentCancelled(AppointmentModel appointment, int position);
        void onAppointmentRescheduled(AppointmentModel appointment, int position);
        // Add this new method:
        void onRescheduleRequested(AppointmentModel appointment, int position);
    }

    public AppointmentDetailsDialog(AppointmentModel appointment) {
        this.appointment = appointment;
    }

    public void setActionListener(AppointmentActionListener listener) {
        this.actionListener = listener;
    }

    private int adapterPosition = -1;

    public void setAdapterPosition(int position) {
        this.adapterPosition = position;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.AppointmentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_appointment_details, container, false);
        initializeViews(view);
        setupListeners();
        populateData();
        return view;
    }

    private void initializeViews(View view) {
        tvTechnicianName = view.findViewById(R.id.tvTechnicianName);
        tvDateValue = view.findViewById(R.id.tvDateValue);
        tvTimeValue = view.findViewById(R.id.tvTimeValue);
        tvServicesValue = view.findViewById(R.id.tvServicesValue);
        tvDurationValue = view.findViewById(R.id.tvDurationValue);
        tvStatusValue = view.findViewById(R.id.tvStatusValue);
        tvNotesValue = view.findViewById(R.id.tvNotesValue);
        tvNotesLabel = view.findViewById(R.id.tvNotesLabel);
        statusIndicator = view.findViewById(R.id.statusIndicator);

        btnClose = view.findViewById(R.id.btnClose);
        btnReschedule = view.findViewById(R.id.btnReschedule);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnEditTime = view.findViewById(R.id.btnEditTime);

        // Time editing views
        layoutTime = view.findViewById(R.id.layoutTime);
        layoutEditTime = view.findViewById(R.id.layoutEditTime);
        etNewTime = view.findViewById(R.id.etNewTime);
        btnSaveTime = view.findViewById(R.id.btnSaveTime);
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnReschedule.setOnClickListener(v -> {
            // Call the rescheduling method on the listener
            if (actionListener != null && adapterPosition >= 0) {
                actionListener.onRescheduleRequested(appointment, adapterPosition);
                dismiss(); // Close dialog after initiating reschedule
            } else {
                Toast.makeText(getContext(), "Unable to reschedule appointment", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            // Show confirmation dialog
            showCancelConfirmation();
        });

        btnEditTime.setOnClickListener(v -> {
            // Show time editing UI
            showTimeEditUI();
        });

        btnSaveTime.setOnClickListener(v -> {
            // Save new time
            if (validateNewTime()) {
                updateAppointmentTime();
            }
        });

        btnCancelEdit.setOnClickListener(v -> {
            // Hide edit UI
            hideTimeEditUI();
        });
    }

    private void populateData() {
        // Set technician name
        tvTechnicianName.setText(appointment.getEmployeeName());

        // Set date
        tvDateValue.setText(appointment.getFormattedDate());

        // Set time
        tvTimeValue.setText(appointment.getTimeRange());

        // Set services
        tvServicesValue.setText(appointment.getServicesString());

        // Set duration
        tvDurationValue.setText(appointment.getDuration() + " minutes");

        // Set status
        String status = appointment.getStatus();
        tvStatusValue.setText(status);

        // Style status based on value
        styleStatusIndicator(status);

        // Set notes if available
        String notes = appointment.getNotes();
        if (notes != null && !notes.isEmpty()) {
            tvNotesValue.setText(notes);
            tvNotesValue.setVisibility(View.VISIBLE);
            tvNotesLabel.setVisibility(View.VISIBLE);
        } else {
            tvNotesValue.setVisibility(View.GONE);
            tvNotesLabel.setVisibility(View.GONE);
        }

        // Hide or show buttons based on appointment status
        updateButtonVisibility(status);
    }

    private void styleStatusIndicator(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
                break;

            case "pending":
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_orange));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange));
                break;

            case "cancelled":
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_red));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
                break;

            case "completed":
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.indigo));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_indigo));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.indigo));
                break;

            case "cancelling...":
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_orange));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange));
                break;

            default:
                tvStatusValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                tvStatusValue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_time_bg));
                statusIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
                break;
        }
    }

    private void updateButtonVisibility(String status) {
        // Logic for showing/hiding buttons based on status
        switch (status.toLowerCase()) {
            case "cancelled":
            case "completed":
            case "cancelling...":  // Also hide buttons when cancelling
                btnReschedule.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnEditTime.setVisibility(View.GONE);
                break;

            case "pending":
            case "confirmed":
                btnReschedule.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnEditTime.setVisibility(View.VISIBLE);
                break;

            default:
                btnReschedule.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnEditTime.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showTimeEditUI() {
        layoutTime.setVisibility(View.GONE);
        layoutEditTime.setVisibility(View.VISIBLE);
        etNewTime.setText(appointment.getTime());
    }

    private void hideTimeEditUI() {
        layoutTime.setVisibility(View.VISIBLE);
        layoutEditTime.setVisibility(View.GONE);
    }

    private boolean validateNewTime() {
        String timeText = etNewTime.getText().toString().trim();
        if (timeText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid time", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check time format (HH:MM)
        if (!timeText.matches("^([01]?[0-9]|2[0-3]):([0-5][0-9])$")) {
            Toast.makeText(getContext(), "Please use format HH:MM (e.g., 14:30)", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateAppointmentTime() {
        String newTime = etNewTime.getText().toString().trim();

        // Create JSON payload for the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("appointmentId", appointment.getId());
            requestBody.put("newTime", newTime);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make API call to reschedule appointment
        String url = BASE_URL + RESCHEDULE_ENDPOINT;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    // Handle successful response
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            // Update the appointment model
                            appointment.setTime(newTime);

                            // Update UI
                            tvTimeValue.setText(appointment.getTimeRange());
                            hideTimeEditUI();

                            // Notify listener
                            if (actionListener != null && adapterPosition >= 0) {
                                actionListener.onAppointmentRescheduled(appointment, adapterPosition);
                            }

                            Toast.makeText(getContext(), "Appointment time updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = response.optString("message", "Failed to update appointment time");
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error response
                    Log.e(TAG, "Error updating appointment time: " + error.toString());
                    Toast.makeText(getContext(), "Error updating appointment time. Please try again.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add request to queue
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void showCancelConfirmation() {
        // First, update UI to show "Cancelling..." status
        appointment.setStatus("Cancelling...");
        tvStatusValue.setText("Cancelling...");
        styleStatusIndicator("cancelling...");
        updateButtonVisibility("cancelling...");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
        builder.setTitle("Cancel Appointment");
        builder.setMessage("Are you sure you want to cancel this appointment?");
        builder.setPositiveButton("Cancel Appointment", (dialog, which) -> {
            cancelAppointment();
        });
        builder.setNegativeButton("Keep Appointment", (dialog, which) -> {
            // Reset the status if user decides not to cancel
            appointment.setStatus("Confirmed"); // Or whatever the original status was
            tvStatusValue.setText(appointment.getStatus());
            styleStatusIndicator(appointment.getStatus());
            updateButtonVisibility(appointment.getStatus());
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void cancelAppointment() {
        // Make API call to cancel appointment
        String url = BASE_URL + CANCEL_ENDPOINT + appointment.getId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    // Handle successful response
                    try {
                        // Try to get the success flag, default to true if not found
                        boolean success = true;
                        if (response.has("success")) {
                            success = response.getBoolean("success");
                        }

                        if (success) {
                            // Update the appointment model
                            appointment.setStatus("Cancelled");

                            // Notify listener with the position for proper UI update
                            if (actionListener != null && adapterPosition >= 0) {
                                // This is the critical call that ensures the item is removed from the UI
                                actionListener.onAppointmentCancelled(appointment, adapterPosition);

                                // Make sure to log this callback
                                Log.d(TAG, "Notifying activity to remove appointment at position: " + adapterPosition);
                            } else {
                                Log.w(TAG, "Unable to notify activity: actionListener=" +
                                        (actionListener != null) + ", position=" + adapterPosition);
                            }

                            Toast.makeText(getContext(), "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();

                            // Dismiss the dialog to return to the appointments list
                            dismiss();
                        } else {
                            String message = response.optString("message", "Failed to cancel appointment");
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                            // Reset the status since cancellation failed
                            appointment.setStatus("Confirmed"); // Or whatever the original status was
                            tvStatusValue.setText(appointment.getStatus());
                            styleStatusIndicator(appointment.getStatus());
                            updateButtonVisibility(appointment.getStatus());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        // We'll still consider this as success since we got a response
                        if (actionListener != null && adapterPosition >= 0) {
                            appointment.setStatus("Cancelled");
                            actionListener.onAppointmentCancelled(appointment, adapterPosition);
                        }

                        Toast.makeText(getContext(), "Appointment cancelled successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                },
                error -> {
                    // Handle error response
                    Log.e(TAG, "Error cancelling appointment: " + error.toString());
                    Toast.makeText(getContext(), "Error cancelling appointment. Please try again.", Toast.LENGTH_SHORT).show();

                    // Reset the status since cancellation failed
                    appointment.setStatus("Confirmed"); // Or whatever the original status was
                    tvStatusValue.setText(appointment.getStatus());
                    styleStatusIndicator(appointment.getStatus());
                    updateButtonVisibility(appointment.getStatus());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add request to queue
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }
}