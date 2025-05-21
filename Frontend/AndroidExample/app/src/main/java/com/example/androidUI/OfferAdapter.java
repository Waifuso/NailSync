package com.example.androidUI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private static final String TAG = "OfferAdapter";
    private List<RewardsService.Offer> offers;
    private OnOfferClickListener listener;

    public interface OnOfferClickListener {
        void onOfferClick(int position);
    }

    public OfferAdapter(List<RewardsService.Offer> offers, OnOfferClickListener listener) {
        this.offers = offers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer_card, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        RewardsService.Offer offer = offers.get(position);

        holder.titleTextView.setText(offer.getTitle());
        holder.descriptionTextView.setText(offer.getDescription());
        holder.discountTextView.setText(offer.getDiscountText());
        holder.tagTextView.setText(offer.getTagText());
        holder.validityTextView.setText(offer.getValidityPeriod());

        // Load offer image - prioritize URL over resource ID
        if (offer.getImageUrl() != null && !offer.getImageUrl().isEmpty()) {
            // Log for debugging
            Log.d(TAG, "Loading offer image from URL: " + offer.getImageUrl());

            // Show placeholder while loading
            holder.offerImageView.setImageResource(R.drawable.stockplaceholderimage);

            // Load image in background
            new LoadImageTask(holder.offerImageView).execute(offer.getImageUrl());
        } else if (offer.getImageResId() != -1) {
            holder.offerImageView.setImageResource(offer.getImageResId());
            Log.d(TAG, "Loading offer image from resource ID: " + offer.getImageResId());
        } else {
            holder.offerImageView.setImageResource(R.drawable.stockplaceholderimage);
            Log.d(TAG, "Using placeholder for offer image");
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOfferClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView offerImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView discountTextView;
        TextView tagTextView;
        TextView validityTextView;

        OfferViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.offerCardView);
            offerImageView = itemView.findViewById(R.id.offerImage);
            titleTextView = itemView.findViewById(R.id.offerTitle);
            descriptionTextView = itemView.findViewById(R.id.offerDescription);
            discountTextView = itemView.findViewById(R.id.discountBadge);
            tagTextView = itemView.findViewById(R.id.offerTag);
            validityTextView = itemView.findViewById(R.id.validityPeriod);
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
                imageView.setImageResource(R.drawable.stockplaceholderimage);
            }
        }
    }
}