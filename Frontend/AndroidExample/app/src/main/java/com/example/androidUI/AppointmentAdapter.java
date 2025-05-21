package com.example.androidUI;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private Context context;
    private List<AppointmentModel> appointments;
    private Consumer<AppointmentModel> onDetailsClickListener;
    private BiConsumer<AppointmentModel, Integer> onCancelClickListener;
    private PaymentListener paymentListener;
    private ReviewListener reviewListener; // Review listener

    private boolean showPayButton = true;

    // Interface for payment handling
    public interface PaymentListener {
        void onPaymentRequested(AppointmentModel appointment, int position, String paymentMethod);
    }

    // Interface for review handling
    public interface ReviewListener {
        void onReviewRequested(AppointmentModel appointment, int position);
    }

    public AppointmentAdapter(Context context, List<AppointmentModel> appointments,
                              Consumer<AppointmentModel> onDetailsClickListener,
                              BiConsumer<AppointmentModel, Integer> onCancelClickListener) {
        this.context = context;
        this.appointments = appointments;
        this.onDetailsClickListener = onDetailsClickListener;
        this.onCancelClickListener = onCancelClickListener;
    }

    // Constructor with payment listener
    public AppointmentAdapter(Context context, List<AppointmentModel> appointments,
                              Consumer<AppointmentModel> onDetailsClickListener,
                              BiConsumer<AppointmentModel, Integer> onCancelClickListener,
                              PaymentListener paymentListener) {
        this.context = context;
        this.appointments = appointments;
        this.onDetailsClickListener = onDetailsClickListener;
        this.onCancelClickListener = onCancelClickListener;
        this.paymentListener = paymentListener;
    }

    // Constructor with payment and review listeners
    public AppointmentAdapter(Context context, List<AppointmentModel> appointments,
                              Consumer<AppointmentModel> onDetailsClickListener,
                              BiConsumer<AppointmentModel, Integer> onCancelClickListener,
                              PaymentListener paymentListener,
                              ReviewListener reviewListener) {
        this.context = context;
        this.appointments = appointments;
        this.onDetailsClickListener = onDetailsClickListener;
        this.onCancelClickListener = onCancelClickListener;
        this.paymentListener = paymentListener;
        this.reviewListener = reviewListener;
    }

    // Setter for payment listener in case it needs to be set later
    public void setPaymentListener(PaymentListener paymentListener) {
        this.paymentListener = paymentListener;
    }

    // Setter for review listener in case it needs to be set later
    public void setReviewListener(ReviewListener reviewListener) {
        this.reviewListener = reviewListener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AppointmentModel appointment = appointments.get(position);

        // Set day and month
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = originalFormat.parse(appointment.getDate());

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

            holder.tvAppointmentDay.setText(dayFormat.format(date));
            holder.tvAppointmentMonth.setText(monthFormat.format(date).toUpperCase());
        } catch (ParseException e) {
            // Fallback if date parsing fails
            holder.tvAppointmentDay.setText("--");
            holder.tvAppointmentMonth.setText("---");
        }

        // Set technician name
        holder.tvTechnicianName.setText(appointment.getEmployeeName());

        // Set time range
        holder.tvAppointmentTime.setText(appointment.getTimeRange());

        // Set duration
        holder.tvDuration.setText(appointment.getDuration() + " minutes");

        // Set services
        holder.tvServiceNames.setText(appointment.getServicesString());

        // Set status and style it
        String status = appointment.getStatus();
        holder.tvAppointmentStatus.setText(status);
        styleStatusView(holder.tvAppointmentStatus, status);

        // Set button actions
        holder.btnViewDetails.setOnClickListener(v ->
                onDetailsClickListener.accept(appointment));

        // Set up Pay Now button
        holder.btnPayNow.setOnClickListener(v -> {
            if (paymentListener != null) {
                // Show payment dialog first
                showPaymentDialog(appointment, holder.getAdapterPosition());
            } else {
                // Fallback to dialog only if no payment listener
                showPaymentDialog(appointment, holder.getAdapterPosition());
            }
        });

        // Set up Review button if it exists
        if (holder.btnReview != null) {

            if (holder.tvAppointmentStatus != null) {
                holder.tvAppointmentStatus.setText(status);
                styleStatusView(holder.tvAppointmentStatus, status);
            }
            // Check if the appointment status is "Paid" to determine if review button should be visible
            if ("Paid".equalsIgnoreCase(appointment.getStatus())) {
                // Show review button
                holder.btnReview.setVisibility(View.VISIBLE);

                final int adapterPosition = holder.getAdapterPosition();

                // Set click listener for review button
                holder.btnReview.setOnClickListener(v -> {
                    // Call the review callback
                    if (reviewListener != null) {
                        reviewListener.onReviewRequested(appointment, adapterPosition);
                    }
                });
            } else {
                // Hide review button if not paid
                holder.btnReview.setVisibility(View.GONE);
            }

            Log.d("AppointmentAdapter",
                    "Appointment position: " + position +
                            ", Status: " + appointment.getStatus() +
                            ", btnReview null? " + (holder.btnReview == null) +
                            ", isPaid? " + "Paid".equalsIgnoreCase(appointment.getStatus()));

        }

        // Update button visibility based on status
        if (appointment.shouldShowPayButton()) {
            updateButtonVisibility(holder, status);
        } else {
            holder.btnPayNow.setVisibility(View.GONE);
        }
    }

    private void showPaymentDialog(AppointmentModel appointment, int position) {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;

            PayDialog dialog = PayDialog.newInstance(appointment);
            dialog.setPaymentCompletionListener(new PayDialog.PaymentCompletionListener() {
                @Override
                public void onPaymentCompleted(AppointmentModel appointment, String paymentMethod) {
                    // If we have a payment listener, forward the request to the activity
                    if (paymentListener != null) {
                        paymentListener.onPaymentRequested(appointment, position, paymentMethod);
                    } else {
                        // Otherwise just update the UI
                        Toast.makeText(context,
                                "Payment processed via " + paymentMethod,
                                Toast.LENGTH_SHORT).show();

                        // Update appointment status
                        appointment.setStatus("Paid");
                        notifyItemChanged(position);
                    }
                }

                @Override
                public void onPaymentCancelled() {
                    // Handle payment cancellation
                    Toast.makeText(context,
                            "Payment cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show(activity.getSupportFragmentManager(), "PaymentDialog");
        }
    }

    private void styleStatusView(TextView statusView, String status) {
        int textColor, backgroundColor;

        switch (status.toLowerCase()) {
            case "confirmed":
                textColor = ContextCompat.getColor(context, R.color.green);
                backgroundColor = ContextCompat.getColor(context, R.color.light_green);
                break;

            case "pending":
            case "upcoming":
            case "scheduled":
            case "approved":
                textColor = ContextCompat.getColor(context, R.color.orange);
                backgroundColor = ContextCompat.getColor(context, R.color.light_orange);
                break;

            case "paid":
                textColor = ContextCompat.getColor(context, R.color.purple_primary);
                backgroundColor = ContextCompat.getColor(context, R.color.selected_time_bg);
                break;

            case "cancelled":
                textColor = ContextCompat.getColor(context, R.color.red);
                backgroundColor = ContextCompat.getColor(context, R.color.light_red);
                break;

            case "completed":
                textColor = ContextCompat.getColor(context, R.color.indigo);
                backgroundColor = ContextCompat.getColor(context, R.color.light_indigo);
                break;

            default:
                textColor = ContextCompat.getColor(context, R.color.purple_primary);
                backgroundColor = ContextCompat.getColor(context, R.color.selected_time_bg);
                break;
        }

        statusView.setTextColor(textColor);
        statusView.setBackgroundColor(backgroundColor);
    }

    private void updateButtonVisibility(AppointmentViewHolder holder, String status) {
        boolean canPay = false;

        switch (status.toLowerCase()) {
            case "confirmed":
            case "pending":
            case "upcoming":
            case "scheduled":
            case "approved":
                canPay = true;
                break;

            case "paid":
            case "cancelled":
            case "completed":
                canPay = false;
                break;

            default:
                canPay = true;
                break;
        }

        // Show or hide pay now button based on status
        holder.btnPayNow.setVisibility(canPay ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < appointments.size()) {
            appointments.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointments.size());
        }
    }

    public void updateItem(AppointmentModel appointment, int position) {
        if (position >= 0 && position < appointments.size()) {
            appointments.set(position, appointment);
            notifyItemChanged(position);
        }
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentDay, tvAppointmentMonth, tvTechnicianName;
        TextView tvAppointmentTime, tvDuration, tvServiceNames, tvAppointmentStatus;
        MaterialButton btnPayNow, btnViewDetails, btnReview; // Added btnReview
        View statusIndicator;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            tvAppointmentDay = itemView.findViewById(R.id.tvAppointmentDay);
            tvAppointmentMonth = itemView.findViewById(R.id.tvAppointmentMonth);
            tvTechnicianName = itemView.findViewById(R.id.tvTechnicianName);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvServiceNames = itemView.findViewById(R.id.tvServiceNames);
            tvAppointmentStatus = itemView.findViewById(R.id.tvAppointmentStatus);

            // Initialize buttons
            btnPayNow = itemView.findViewById(R.id.btnPayNow);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnReview = itemView.findViewById(R.id.btnReview);

            // Initialize indicators
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }

    public boolean shouldShowPayButton() {
        return showPayButton;
    }

    public void setShowPayButton(boolean showPayButton) {
        this.showPayButton = showPayButton;
    }
}