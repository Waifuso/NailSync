package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying service items in the payment receipt.
 * Uses the existing ServiceModel class from the project.
 */
public class ReceiptServiceAdapter extends RecyclerView.Adapter<ReceiptServiceAdapter.ServiceViewHolder> {

    private final Context context;
    private final List<ServiceModel> serviceItems;
    private final List<Double> prices;
    private boolean animationsShown = false;

    /**
     * Constructor for ReceiptServiceAdapter.
     *
     * @param context The context.
     * @param serviceItems List of services to display.
     * @param prices List of prices for each service.
     */
    public ReceiptServiceAdapter(Context context, List<ServiceModel> serviceItems, List<Double> prices) {
        this.context = context;
        this.serviceItems = serviceItems;
        this.prices = prices;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = serviceItems.get(position);
        double price = position < prices.size() ? prices.get(position) : 0.00;

        // Set the service name and price
        holder.tvServiceName.setText(service.getTitle());
        holder.tvServicePrice.setText(String.format(Locale.US, "$%.2f", price));

        // Set the service icon
        holder.ivServiceIcon.setImageResource(service.getIconResourceId());

        // Add animation for items appearing
        if (!animationsShown) {
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationX(50f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setStartDelay(position * 100) // Staggered animation
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // If we're at the last item, mark animations as shown
            if (position == serviceItems.size() - 1) {
                animationsShown = true;
            }
        }
    }

    @Override
    public int getItemCount() {
        return serviceItems.size();
    }

    /**
     * ViewHolder class for service items.
     */
    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServicePrice;
        ImageView ivServiceIcon;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
        }
    }
}