package com.example.androidUI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.androidexample.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PayDialog extends BottomSheetDialogFragment {

    private static final String ARG_APPOINTMENT = "appointment";

    private AppointmentModel appointment;
    private PaymentCompletionListener listener;

    public interface PaymentCompletionListener {
        void onPaymentCompleted(AppointmentModel appointment, String paymentMethod);
        void onPaymentCancelled();
    }

    public static PayDialog newInstance(AppointmentModel appointment) {
        PayDialog fragment = new PayDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPOINTMENT, appointment);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPaymentCompletionListener(PaymentCompletionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointment = (AppointmentModel) getArguments().getSerializable(ARG_APPOINTMENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_dialog, container, false);

        // Initialize views
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnProceed = view.findViewById(R.id.btnProceed);
        ImageButton btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        RadioGroup radioGroupPayment = view.findViewById(R.id.radioGroupPaymentOptions);
        TextView txtPaymentDescription = view.findViewById(R.id.txtPaymentDescription);

        // Set payment description
        if (appointment != null) {
            txtPaymentDescription.setText("Payment for appointment with " + appointment.getEmployeeName() +
                    " on " + appointment.getFormattedDate() + " at " + appointment.getTimeRange());
        }

        // Set click listeners
        btnCloseDialog.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentCancelled();
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentCancelled();
            }
            dismiss();
        });

        btnProceed.setOnClickListener(v -> {
            // Get selected payment method
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();
            String paymentMethod = "Credit/Debit Card"; // Default

            if (selectedId == R.id.radioPaypal) {
                paymentMethod = "PayPal";
            } else if (selectedId == R.id.radioBankTransfer) {
                paymentMethod = "Bank Transfer";
            } else if (selectedId == R.id.radioOther) {
                paymentMethod = "Other";
            }

            // Process payment
            if (listener != null && appointment != null) {
                listener.onPaymentCompleted(appointment, paymentMethod);
            }

            // Show confirmation
            Toast.makeText(getContext(),
                    "Payment processed via " + paymentMethod,
                    Toast.LENGTH_SHORT).show();

            dismiss();
        });

        return view;
    }
}