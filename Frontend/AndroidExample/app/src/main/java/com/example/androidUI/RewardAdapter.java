package com.example.androidUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<RewardsService.Reward> rewards;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(int position);
    }

    public RewardAdapter(List<RewardsService.Reward> rewards, OnRewardClickListener listener) {
        this.rewards = rewards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new RewardCardView
        RewardCardView cardView = new RewardCardView(parent.getContext());

        // Set layout parameters for the card view
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = (int) parent.getContext().getResources().getDimension(R.dimen.reward_card_width);
        params.height = (int) parent.getContext().getResources().getDimension(R.dimen.reward_card_height);
        params.setMarginEnd((int) parent.getContext().getResources().getDimension(R.dimen.reward_card_margin));
        cardView.setLayoutParams(params);

        return new RewardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardsService.Reward reward = rewards.get(position);

        // Set the reward to the card view
        holder.cardView.setReward(reward);

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        RewardCardView cardView;

        RewardViewHolder(View itemView) {
            super(itemView);
            cardView = (RewardCardView) itemView;
        }
    }
}