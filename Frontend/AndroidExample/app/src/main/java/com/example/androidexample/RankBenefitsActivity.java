package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the benefits of each rank
 */
public class RankBenefitsActivity extends AppCompatActivity {

    // UI components
    private TextView rankTitle;
    private TextView rankDescription;
    private ImageButton backButton;
    private RecyclerView benefitsRecyclerView;

    // Data
    private String rankName;
    private List<RankBenefit> benefits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_benefits);

        // Get rank name from intent
        if (getIntent() != null) {
            rankName = getIntent().getStringExtra("RANK_NAME");
        }

        // Initialize views
        initializeViews();

        // Set up rank data
        setupRankData();

        // Set up benefits list
        setupBenefitsList();
    }

    private void initializeViews() {
        rankTitle = findViewById(R.id.rankTitle);
        rankDescription = findViewById(R.id.rankDescription);
        backButton = findViewById(R.id.backButton);
        benefitsRecyclerView = findViewById(R.id.benefitsRecyclerView);

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRankData() {
        // Set rank title
        rankTitle.setText(rankName + " Rank Benefits");

        // Set rank description based on rank name
        switch (rankName.toLowerCase()) {
            case "bronze":
                rankDescription.setText("Bronze is our entry-level rank. Enjoy these special perks as you begin your journey with NailSync.");
                break;
            case "silver":
                rankDescription.setText("Silver members have shown their loyalty and enjoy enhanced benefits. Keep earning points to reach Platinum!");
                break;
            case "platinum":
                rankDescription.setText("As a Platinum member, you're among our most valued clients. Enjoy premium perks and exclusive offers.");
                break;
            case "diamond":
                rankDescription.setText("Diamond is our highest tier. These exclusive benefits are our way of thanking you for your exceptional loyalty.");
                break;
            default:
                rankDescription.setText("Enjoy these special benefits as a valued member of NailSync.");
                break;
        }
    }

    private void setupBenefitsList() {
        // Load benefits based on rank
        loadBenefitsForRank();

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        benefitsRecyclerView.setLayoutManager(layoutManager);

        // Create and set adapter
        BenefitsAdapter adapter = new BenefitsAdapter(benefits);
        benefitsRecyclerView.setAdapter(adapter);
    }

    private void loadBenefitsForRank() {
        benefits.clear();

        // Add common benefits for all ranks
        benefits.add(new RankBenefit(
                "Points on Every Visit",
                "Earn points with every service booking.",
                true));

        benefits.add(new RankBenefit(
                "Birthday Bonus",
                "Special bonus points during your birthday month.",
                true));

        // Add rank-specific benefits
        switch (rankName.toLowerCase()) {
            case "bronze":
                benefits.add(new RankBenefit(
                        "Welcome Reward",
                        "One-time 100 bonus points for new members.",
                        true));
                benefits.add(new RankBenefit(
                        "Referral Bonus",
                        "Earn 100 points for each friend you refer.",
                        true));
                benefits.add(new RankBenefit(
                        "Priority Booking",
                        "Priority slot allocation for appointments.",
                        false));
                benefits.add(new RankBenefit(
                        "Exclusive Offers",
                        "Access to exclusive promotions and offers.",
                        false));
                break;

            case "silver":
                benefits.add(new RankBenefit(
                        "Welcome Reward",
                        "One-time 100 bonus points for new members.",
                        true));
                benefits.add(new RankBenefit(
                        "Referral Bonus",
                        "Earn 100 points for each friend you refer.",
                        true));
                benefits.add(new RankBenefit(
                        "Priority Booking",
                        "Priority slot allocation for appointments.",
                        true));
                benefits.add(new RankBenefit(
                        "Exclusive Offers",
                        "Access to exclusive promotions and offers.",
                        true));
                benefits.add(new RankBenefit(
                        "Free Add-ons",
                        "Complimentary add-on service once per month.",
                        false));
                break;

            case "platinum":
                benefits.add(new RankBenefit(
                        "Welcome Reward",
                        "One-time 100 bonus points for new members.",
                        true));
                benefits.add(new RankBenefit(
                        "Referral Bonus",
                        "Earn 150 points for each friend you refer.",
                        true));
                benefits.add(new RankBenefit(
                        "Priority Booking",
                        "Priority slot allocation for appointments.",
                        true));
                benefits.add(new RankBenefit(
                        "Exclusive Offers",
                        "Access to exclusive promotions and offers.",
                        true));
                benefits.add(new RankBenefit(
                        "Free Add-ons",
                        "Complimentary add-on service once per month.",
                        true));
                benefits.add(new RankBenefit(
                        "Anniversary Gift",
                        "Special gift on your membership anniversary.",
                        true));
                benefits.add(new RankBenefit(
                        "VIP Waiting List",
                        "First priority on cancellation waiting list.",
                        false));
                break;

            case "diamond":
                benefits.add(new RankBenefit(
                        "Welcome Reward",
                        "One-time 100 bonus points for new members.",
                        true));
                benefits.add(new RankBenefit(
                        "Referral Bonus",
                        "Earn 200 points for each friend you refer.",
                        true));
                benefits.add(new RankBenefit(
                        "Priority Booking",
                        "Priority slot allocation for appointments.",
                        true));
                benefits.add(new RankBenefit(
                        "Exclusive Offers",
                        "Access to exclusive promotions and offers.",
                        true));
                benefits.add(new RankBenefit(
                        "Free Add-ons",
                        "Complimentary add-on service once per month.",
                        true));
                benefits.add(new RankBenefit(
                        "Anniversary Gift",
                        "Special gift on your membership anniversary.",
                        true));
                benefits.add(new RankBenefit(
                        "VIP Waiting List",
                        "First priority on cancellation waiting list.",
                        true));
                benefits.add(new RankBenefit(
                        "Dedicated Stylist",
                        "Option to book with the same stylist every time.",
                        true));
                benefits.add(new RankBenefit(
                        "Free Annual Service",
                        "One complete free service of your choice annually.",
                        true));
                break;
        }
    }

    /**
     * Model class for rank benefits
     */
    private static class RankBenefit {
        private String title;
        private String description;
        private boolean isAvailable;

        public RankBenefit(String title, String description, boolean isAvailable) {
            this.title = title;
            this.description = description;
            this.isAvailable = isAvailable;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean isAvailable() { return isAvailable; }
    }

    /**
     * Adapter for benefits list
     */
    private class BenefitsAdapter extends RecyclerView.Adapter<BenefitsAdapter.BenefitViewHolder> {

        private List<RankBenefit> benefits;

        public BenefitsAdapter(List<RankBenefit> benefits) {
            this.benefits = benefits;
        }

        @Override
        public BenefitViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_rank_benefit, parent, false);
            return new BenefitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BenefitViewHolder holder, int position) {
            RankBenefit benefit = benefits.get(position);

            // Set benefit data
            holder.benefitTitle.setText(benefit.getTitle());
            holder.benefitDescription.setText(benefit.getDescription());

            // Set availability status
            if (benefit.isAvailable()) {
                holder.benefitCard.setAlpha(1.0f);
                holder.availabilityIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.benefitCard.setAlpha(0.6f);
                holder.availabilityIndicator.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return benefits.size();
        }

        class BenefitViewHolder extends RecyclerView.ViewHolder {
            CardView benefitCard;
            TextView benefitTitle;
            TextView benefitDescription;
            View availabilityIndicator;

            public BenefitViewHolder(View itemView) {
                super(itemView);
                benefitCard = itemView.findViewById(R.id.benefitCard);
                benefitTitle = itemView.findViewById(R.id.benefitTitle);
                benefitDescription = itemView.findViewById(R.id.benefitDescription);
                availabilityIndicator = itemView.findViewById(R.id.availabilityIndicator);
            }
        }
    }
}