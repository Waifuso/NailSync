package com.example.androidUI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.androidexample.R;

import java.util.List;

public class ImageSlideAdapter extends RecyclerView.Adapter<ImageSlideAdapter.ImageViewHolder> {

    private List<SlideItem> slideItems;
    private Context context;
    private long userID;

    public ImageSlideAdapter(Context context, List<SlideItem> slideItems) {
        this.context = context;
        this.slideItems = slideItems;
        // The userID will be set in each SlideItem
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slide, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        SlideItem slideItem = slideItems.get(position);

        // Load image using Glide with rounded corners
        Glide.with(context)
                .load(slideItem.getImageResource())
                .fitCenter()
                .transform(new RoundedCorners(24))  // 24dp corner radius
                .into(holder.imageView);

        // Set click listener to navigate to the destination activity
        holder.itemView.setOnClickListener(v -> {
            long itemUserID = slideItem.getUserID();
            Log.d("ImageSlideAdapter", "Slide item clicked with userID: " + itemUserID);

            Intent intent = new Intent(context, slideItem.getActivityClass());
            intent.putExtra("userID", itemUserID);  // Pass userID as an int

            // Add flags to ensure clean navigation
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slideImageView);
        }
    }

    // Class to hold slide item data
    public static class SlideItem {
        private int imageResource;
        private Class<?> activityClass;
        private long userID; // Changed from float to int for consistency

        public SlideItem(int imageResource, Class<?> activityClass, long userID) {
            this.imageResource = imageResource;
            this.activityClass = activityClass;
            this.userID = userID;
        }

        public int getImageResource() {
            return imageResource;
        }

        public Class<?> getActivityClass() {
            return activityClass;
        }

        public long getUserID() {
            return userID;
        }
    }
}