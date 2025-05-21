package com.example.androidUI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.androidexample.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Custom CardView for displaying reward items in the rewards list
 */
public class RewardCardView extends CardView {

    private static final String TAG = "RewardCardView";

    private ImageView rewardImage;
    private TextView rewardTitle;
    private TextView rewardDescription;
    private TextView pointsRequired;

    // Default values
    private String titleText = "Reward Title";
    private String descriptionText = "Reward Description";
    private String pointsText = "0 POINTS";
    private int imageResource = R.drawable.reward_placeholder;

    public RewardCardView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public RewardCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RewardCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward_card, this, true);

        // Find views
        rewardImage = view.findViewById(R.id.rewardImage);
        rewardTitle = view.findViewById(R.id.rewardTitle);
        rewardDescription = view.findViewById(R.id.rewardDescription);
        pointsRequired = view.findViewById(R.id.pointsRequired);

        // Apply styling and attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RewardCardView);

            // Apply attributes if defined
            titleText = a.getString(R.styleable.RewardCardView_rewardTitle) != null ?
                    a.getString(R.styleable.RewardCardView_rewardTitle) : titleText;

            descriptionText = a.getString(R.styleable.RewardCardView_rewardDescription) != null ?
                    a.getString(R.styleable.RewardCardView_rewardDescription) : descriptionText;

            pointsText = a.getString(R.styleable.RewardCardView_pointsText) != null ?
                    a.getString(R.styleable.RewardCardView_pointsText) : pointsText;

            imageResource = a.getResourceId(R.styleable.RewardCardView_rewardImage, imageResource);

            a.recycle();
        }

        // Set default values
        setRewardTitle(titleText);
        setRewardDescription(descriptionText);
        setPointsRequired(pointsText);
        setRewardImage(imageResource);

        // Set card properties
        setRadius(context.getResources().getDimension(R.dimen.card_corner_radius));
        setCardElevation(context.getResources().getDimension(R.dimen.card_elevation));
        setUseCompatPadding(true);
    }

    /**
     * Set the reward title
     */
    public void setRewardTitle(String title) {
        titleText = title;
        rewardTitle.setText(title);
    }

    /**
     * Set the reward description
     */
    public void setRewardDescription(String description) {
        descriptionText = description;
        rewardDescription.setText(description);
    }

    /**
     * Set the points required text
     */
    public void setPointsRequired(String points) {
        pointsText = points;
        pointsRequired.setText(points);
    }

    /**
     * Set the points required as an integer (will format as "X POINTS")
     */
    public void setPointsRequired(int points) {
        pointsText = points + " POINTS";
        pointsRequired.setText(pointsText);
    }

    /**
     * Set the reward image from a resource ID
     */
    public void setRewardImage(int resourceId) {
        imageResource = resourceId;
        rewardImage.setImageResource(resourceId);
    }

    /**
     * Set the reward image from a URL (uses AsyncTask to load image)
     */
    public void setRewardImageUrl(String url) {
        if (url != null && !url.isEmpty()) {
            // Log the URL for debugging
            Log.d(TAG, "Loading image from URL: " + url);

            // Show placeholder while loading
            rewardImage.setImageResource(R.drawable.reward_placeholder);

            // Load image in background
            new LoadImageTask(rewardImage).execute(url);
        } else {
            // If URL is empty or null, use placeholder
            rewardImage.setImageResource(R.drawable.reward_placeholder);
        }
    }

    /**
     * Set a reward object to populate the card
     */
    public void setReward(RewardsService.Reward reward) {
        setRewardTitle(reward.getTitle());
        setRewardDescription(reward.getDescription());
        setPointsRequired(reward.getPointsRequired());

        // Set image - prefer image URL over resource ID
        if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
            setRewardImageUrl(reward.getImageUrl());
            Log.d(TAG, "Setting image URL: " + reward.getImageUrl());
        } else if (reward.getImageResId() != -1) {
            setRewardImage(reward.getImageResId());
            Log.d(TAG, "Setting image resource ID: " + reward.getImageResId());
        } else {
            setRewardImage(R.drawable.reward_placeholder);
            Log.d(TAG, "Setting placeholder image");
        }
    }

    /**
     * AsyncTask to load images from URLs
     */
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                Log.d(TAG, "Image loaded successfully from: " + imageUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from URL: " + imageUrl, e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                // Set placeholder if loading failed
                imageView.setImageResource(R.drawable.reward_placeholder);
            }
        }
    }
}