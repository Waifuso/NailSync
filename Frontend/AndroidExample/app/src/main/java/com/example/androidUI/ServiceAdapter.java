package com.example.androidUI;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * Adapter for displaying services in a RecyclerView with expandable items.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> serviceList;
    private Context context;
    private OnServiceSelectedListener listener;

    /**
     * Interface for service selection events.
     */
    public interface OnServiceSelectedListener {
        void onServiceSelected(int serviceId, boolean isSelected);
    }

    /**
     * Constructor for ServiceAdapter.
     *
     * @param context     The context.
     * @param serviceList List of services to display.
     * @param listener    Listener for selection events.
     */
    public ServiceAdapter(Context context, List<ServiceModel> serviceList, OnServiceSelectedListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = serviceList.get(position);
        final int pos = position; // For use in lambda expressions

        // Set the service details
        holder.serviceTitle.setText(service.getTitle());
        holder.serviceIcon.setImageResource(service.getIconResourceId());

        // Use long description instead of short description
        holder.serviceDescription.setText(service.getLongDescription());

        // Set selector state
        holder.serviceSelector.setSelected(service.isSelected());

        // Set background color based on selection
        if (service.isSelected()) {
            holder.serviceSelector.setBackgroundResource(R.drawable.service_selector_selected);
        } else {
            holder.serviceSelector.setBackgroundResource(R.drawable.service_selector_normal);
        }

        // Set elevation effect to selected items
        if (service.isSelected()) {
            holder.itemView.setElevation(8f);
        } else {
            holder.itemView.setElevation(2f);
        }

        // Set expansion state
        if (service.isExpanded()) {
            holder.expandableContent.setVisibility(View.VISIBLE);
            holder.expandArrow.setRotation(180f);
        } else {
            holder.expandableContent.setVisibility(View.GONE);
            holder.expandArrow.setRotation(0f);
        }

        // Adjust spacing between selector and icon (if needed)
        ConstraintLayout.LayoutParams iconParams = (ConstraintLayout.LayoutParams) holder.serviceIcon.getLayoutParams();
        iconParams.setMarginStart(16); // Increase margin to match the design
        holder.serviceIcon.setLayoutParams(iconParams);

        // Handle selector click
        holder.serviceSelector.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            boolean isSelected = !service.isSelected();
            service.setSelected(isSelected);

            // Update visual state
            holder.serviceSelector.setSelected(isSelected);
            if (isSelected) {
                holder.serviceSelector.setBackgroundResource(R.drawable.service_selector_selected);
                holder.itemView.setElevation(8f);
            } else {
                holder.serviceSelector.setBackgroundResource(R.drawable.service_selector_normal);
                holder.itemView.setElevation(2f);
            }

            // Notify listener
            listener.onServiceSelected(service.getId(), isSelected);
        });

        // Stop click propagation from selector to header
        holder.serviceSelector.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                return true;
            }
            return false;
        });

        // Handle arrow click to expand/collapse
        holder.expandArrow.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            toggleExpansion(holder, service);
        });

        // Handle header click to expand/collapse (excluding checkbox)
        holder.serviceHeaderLayout.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            toggleExpansion(holder, service);
        });
    }

    /**
     * Toggle the expansion state of a service item with animation.
     *
     * @param holder  The ViewHolder for the item.
     * @param service The service model.
     */
    private void toggleExpansion(ServiceViewHolder holder, ServiceModel service) {
        // Toggle the expansion state in the model
        service.toggleExpanded();

        // Animate expansion/collapse
        if (service.isExpanded()) {
            // Expand
            expandView(holder.expandableContent, holder.expandArrow);
        } else {
            // Collapse
            collapseView(holder.expandableContent, holder.expandArrow);
        }
    }

    /**
     * Expand a view with animation.
     *
     * @param view The view to expand.
     * @param arrow The arrow to rotate.
     */
    private void expandView(final View view, ImageView arrow) {
        view.setVisibility(View.VISIBLE);

        // Measure the view to determine target height
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int targetHeight = view.getMeasuredHeight();

        // Set initial height to 0
        view.getLayoutParams().height = 0;
        view.requestLayout();

        // Create height animation
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();

            // Fade in while expanding
            float fraction = animation.getAnimatedFraction();
            view.setAlpha(fraction);

            // Rotate arrow
            arrow.setRotation(fraction * 180f);
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Set height to WRAP_CONTENT after animation completes
                view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        });

        animator.start();
    }

    /**
     * Collapse a view with animation.
     *
     * @param view The view to collapse.
     * @param arrow The arrow to rotate.
     */
    private void collapseView(final View view, ImageView arrow) {
        // Get initial height
        int initialHeight = view.getMeasuredHeight();

        // Create height animation
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();

            // Fade out while collapsing
            float fraction = 1f - animation.getAnimatedFraction();
            view.setAlpha(fraction);

            // Rotate arrow back
            arrow.setRotation((1 - animation.getAnimatedFraction()) * 180f);
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });

        animator.start();
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    /**
     * ViewHolder class for service items.
     */
    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        View serviceSelector;
        ImageView serviceIcon;
        TextView serviceTitle;
        ImageView expandArrow;
        LinearLayout expandableContent;
        TextView serviceDescription;
        ConstraintLayout serviceHeaderLayout;

        ServiceViewHolder(View itemView) {
            super(itemView);
            serviceSelector = itemView.findViewById(R.id.serviceSelector);
            serviceIcon = itemView.findViewById(R.id.serviceIcon);
            serviceTitle = itemView.findViewById(R.id.serviceTitle);
            expandArrow = itemView.findViewById(R.id.expandArrow);
            expandableContent = itemView.findViewById(R.id.expandableContent);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            serviceHeaderLayout = itemView.findViewById(R.id.serviceHeaderLayout);
        }
    }
}