package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReviewPagerAdapter extends RecyclerView.Adapter<ReviewPagerAdapter.ViewHolder> {

    private Context context;
    private String technicianName;
    private String appointmentDate;
    private String appointmentTime;
    private int duration;
    private ArrayList<String> serviceNames;

    // Store views for each stage to reference later
    private Map<Integer, View> stageViews = new HashMap<>();

    // Rating value that will be shared across pages
    private float ratingValue = 0;

    public ReviewPagerAdapter(Context context, String technicianName, String appointmentDate,
                              String appointmentTime, int duration, ArrayList<String> serviceNames) {
        this.context = context;
        this.technicianName = technicianName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.duration = duration;
        this.serviceNames = serviceNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        // Inflate layout based on position
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.page_review_stage1, parent, false);
        } else if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.page_review_stage2, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.page_review_stage3, parent, false);
        }

        // Store the view for later access
        stageViews.put(viewType, view);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Setup each page
        if (position == 0) {
            setupStage1(holder.itemView);
        } else if (position == 1) {
            setupStage2(holder.itemView);
        } else {
            setupStage3(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // We have 3 stages
    }

    @Override
    public int getItemViewType(int position) {
        return position; // Use position as the view type
    }

    // Method to retrieve a specific stage view
    public View getStageView(int stage) {
        return stageViews.get(stage);
    }

    // Method to set rating value - can be called from activity
    public void setRating(float rating) {
        this.ratingValue = rating;

        // If stage 2 and 3 views exist, update their rating bars
        updateRatingInViews();
    }

    // Method to get current rating
    public float getRating() {
        // First try to get from stage 1
        View stage1View = stageViews.get(0);
        if (stage1View != null) {
            RatingBar ratingBar = stage1View.findViewById(R.id.ratingBar);
            if (ratingBar != null) {
                return ratingBar.getRating();
            }
        }

        // Fall back to stored value
        return ratingValue;
    }

    // Helper method to update ratings in all views
    private void updateRatingInViews() {
        // Update stage 2 rating if view exists
        View stage2View = stageViews.get(1);
        if (stage2View != null) {
            RatingBar ratingBarReadOnly = stage2View.findViewById(R.id.ratingBarReadOnly);
            if (ratingBarReadOnly != null) {
                ratingBarReadOnly.setRating(ratingValue);
            }
        }

        // Update stage 3 rating if view exists
        View stage3View = stageViews.get(2);
        if (stage3View != null) {
            RatingBar ratingBarSummary = stage3View.findViewById(R.id.ratingBarSummary);
            if (ratingBarSummary != null) {
                ratingBarSummary.setRating(ratingValue);
            }
        }
    }

    private void setupStage1(View view) {
        // Setup appointment details
        TextView tvTechnicianName = view.findViewById(R.id.tvTechnicianName);
        TextView tvAppointmentTime = view.findViewById(R.id.tvAppointmentTime);
        TextView tvDuration = view.findViewById(R.id.tvDuration);
        TextView tvServiceNames = view.findViewById(R.id.tvServiceNames);
        TextView tvAppointmentDay = view.findViewById(R.id.tvAppointmentDay);
        TextView tvAppointmentMonth = view.findViewById(R.id.tvAppointmentMonth);

        if (tvTechnicianName != null) tvTechnicianName.setText(technicianName);
        if (tvAppointmentTime != null) tvAppointmentTime.setText(appointmentTime);
        if (tvDuration != null) tvDuration.setText(duration + " minutes");

        // Format and set services
        if (tvServiceNames != null && serviceNames != null && !serviceNames.isEmpty()) {
            StringBuilder servicesText = new StringBuilder();
            for (int i = 0; i < serviceNames.size(); i++) {
                servicesText.append(serviceNames.get(i));
                if (i < serviceNames.size() - 1) {
                    servicesText.append(", ");
                }
            }
            tvServiceNames.setText(servicesText.toString());
        }

        // Format and set date
        if (tvAppointmentDay != null && tvAppointmentMonth != null && appointmentDate != null) {
            try {
                String[] dateParts = appointmentDate.split("-");
                if (dateParts.length >= 3) {
                    tvAppointmentDay.setText(dateParts[2]); // Day

                    // Convert month number to abbreviation
                    String[] monthAbbreviations = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                    int monthIndex = Integer.parseInt(dateParts[1]) - 1;
                    if (monthIndex >= 0 && monthIndex < 12) {
                        tvAppointmentMonth.setText(monthAbbreviations[monthIndex]);
                    }
                }
            } catch (Exception e) {
                // If date parsing fails, use raw date
                String[] dateParts = appointmentDate.split(" ");
                if (dateParts.length > 0) {
                    tvAppointmentDay.setText(dateParts[0]);
                    if (dateParts.length > 1) {
                        tvAppointmentMonth.setText(dateParts[1]);
                    }
                }
            }
        }

        // Setup rating bar
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        if (ratingBar != null) {
            // Set initial rating if available
            if (ratingValue > 0) {
                ratingBar.setRating(ratingValue);
            }

            // Add rating change listener to update shared rating
            ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
                if (fromUser) {
                    ratingValue = rating;
                    updateRatingInViews();
                }
            });
        }
    }

    private void setupStage2(View view) {
        // Setup rating display
        RatingBar ratingBarReadOnly = view.findViewById(R.id.ratingBarReadOnly);
        if (ratingBarReadOnly != null) {
            ratingBarReadOnly.setRating(ratingValue);
        }

        // Setup suggested prompts
        TextView chipPrompt1 = view.findViewById(R.id.chipPrompt1);
        TextView chipPrompt2 = view.findViewById(R.id.chipPrompt2);
        TextView chipPrompt3 = view.findViewById(R.id.chipPrompt3);

        if (chipPrompt1 != null) {
            chipPrompt1.setOnClickListener(v -> {
                android.widget.EditText etReviewText = view.findViewById(R.id.etReviewText);
                if (etReviewText != null) {
                    etReviewText.setText(chipPrompt1.getText());
                }
            });
        }

        if (chipPrompt2 != null) {
            chipPrompt2.setOnClickListener(v -> {
                android.widget.EditText etReviewText = view.findViewById(R.id.etReviewText);
                if (etReviewText != null) {
                    etReviewText.setText(chipPrompt2.getText());
                }
            });
        }

        if (chipPrompt3 != null) {
            chipPrompt3.setOnClickListener(v -> {
                android.widget.EditText etReviewText = view.findViewById(R.id.etReviewText);
                if (etReviewText != null) {
                    etReviewText.setText(chipPrompt3.getText());
                }
            });
        }
    }

    private void setupStage3(View view) {
        // Setup technician info
        TextView tvTechnicianNameSummary = view.findViewById(R.id.tvTechnicianNameSummary);
        TextView tvServicesSummary = view.findViewById(R.id.tvServicesSummary);
        RatingBar ratingBarSummary = view.findViewById(R.id.ratingBarSummary);

        if (tvTechnicianNameSummary != null) {
            tvTechnicianNameSummary.setText(technicianName);
        }

        if (tvServicesSummary != null && serviceNames != null && !serviceNames.isEmpty()) {
            StringBuilder servicesText = new StringBuilder();
            for (int i = 0; i < serviceNames.size(); i++) {
                servicesText.append(serviceNames.get(i));
                if (i < serviceNames.size() - 1) {
                    servicesText.append(", ");
                }
            }
            tvServicesSummary.setText(servicesText.toString());
        }

        if (ratingBarSummary != null) {
            ratingBarSummary.setRating(ratingValue);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}