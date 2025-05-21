package com.example.androidUI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class CardSliderAdapter extends RecyclerView.Adapter<CardSliderAdapter.CardViewHolder> {

    private Context context;
    private List<CardItem> cardItems;
    private long userId;

    public CardSliderAdapter(Context context, List<CardItem> cardItems, long userId) {
        this.context = context;
        this.cardItems = cardItems;
        this.userId = userId;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = cardItems.get(position);

        // Set the card image
        holder.imageView.setImageResource(item.getImageResource());

        // Set the card title if available
        if (holder.titleView != null && item.getTitle() != null) {
            holder.titleView.setText(item.getTitle());
        }

        // Set the button text and click listener
        if (holder.actionButton != null) {
            holder.actionButton.setText(item.getButtonText());
            holder.actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, item.getTargetActivity());
                intent.putExtra("userID", userId);
                context.startActivity(intent);
            });
        }

        // Make the whole card clickable
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, item.getTargetActivity());
            intent.putExtra("userID", userId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        Button actionButton;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardImage);
            titleView = itemView.findViewById(R.id.cardTitle);
            actionButton = itemView.findViewById(R.id.cardButton);
        }
    }

    // Card item model class
    public static class CardItem {
        private int imageResource;
        private String title;
        private String buttonText;
        private Class<?> targetActivity;

        public CardItem(int imageResource, String title, String buttonText, Class<?> targetActivity) {
            this.imageResource = imageResource;
            this.title = title;
            this.buttonText = buttonText;
            this.targetActivity = targetActivity;
        }

        public int getImageResource() {
            return imageResource;
        }

        public String getTitle() {
            return title;
        }

        public String getButtonText() {
            return buttonText;
        }

        public Class<?> getTargetActivity() {
            return targetActivity;
        }
    }
}