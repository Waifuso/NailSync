package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.androidexample.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private final Context context;
    private final List<TimelinePost> posts;
    private final SimpleDateFormat inputFormat;
    private final SimpleDateFormat outputFormat;

    public TimelineAdapter(Context context, List<TimelinePost> posts) {
        this.context = context;
        this.posts = posts;

        // Date formatter for parsing server timestamp
        inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Date formatter for displaying in user's timezone
        outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimelinePost post = posts.get(position);

        // Set employee name
        holder.employeeNameTextView.setText(post.getEmployeeName());

        // Set post message/caption
        if (post.getMessage() != null && !post.getMessage().isEmpty()) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextView.setText(post.getMessage());
        } else {
            holder.messageTextView.setVisibility(View.GONE);
        }

        // Format and set timestamp
        String formattedDate = formatDate(post.getTimestamp());
        holder.timestampTextView.setText(formattedDate);

        // Load image if available
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);

            // Use Glide to load and cache image
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_warning);

            Glide.with(context)
                    .load(post.getImageUrl())
                    .apply(requestOptions)
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * Format the timestamp from server format to a user-friendly format
     */
    private String formatDate(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Unknown date";
        }

        try {
            Date date = inputFormat.parse(timestamp);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // If we can't parse the server format, try a simpler format
            try {
                SimpleDateFormat alternateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = alternateFormat.parse(timestamp);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {
                // If still can't parse, return the original string
            }
        }

        return timestamp;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView employeeNameTextView;
        TextView messageTextView;
        TextView timestampTextView;
        ImageView postImageView;

        ViewHolder(View itemView) {
            super(itemView);
            employeeNameTextView = itemView.findViewById(R.id.employee_name);
            messageTextView = itemView.findViewById(R.id.post_message);
            timestampTextView = itemView.findViewById(R.id.timestamp);
            postImageView = itemView.findViewById(R.id.post_image);
        }
    }
}