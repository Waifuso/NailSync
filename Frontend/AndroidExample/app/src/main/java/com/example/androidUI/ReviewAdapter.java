package com.example.androidUI;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<ReviewModel> reviewList;

    public ReviewAdapter(Context context, List<ReviewModel> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);

        // Set employee name
        holder.textEmployee.setText(review.getEmployee());

        // Set the services
        String services = TextUtils.join(", ", review.getServiceName());
        holder.textServices.setText(services);

        // Set the date
        holder.textDate.setText(review.getDate());

        // Set the comment (replace '+' with spaces if needed)
        String comment = review.getRating().getComment().replace("+", " ");
        holder.textComment.setText(comment);

        // Set the stars based on rating
        setStars(holder, review.getRating().getStar());
    }

    private void setStars(ReviewViewHolder holder, int rating) {
        // Reset all stars to empty
        holder.star1.setImageResource(R.drawable.ic_star_empty);
        holder.star2.setImageResource(R.drawable.ic_star_empty);
        holder.star3.setImageResource(R.drawable.ic_star_empty);
        holder.star4.setImageResource(R.drawable.ic_star_empty);
        holder.star5.setImageResource(R.drawable.ic_star_empty);

        // Fill stars based on rating
        switch (rating) {
            case 5:
                holder.star5.setImageResource(R.drawable.ic_star_filled);
            case 4:
                holder.star4.setImageResource(R.drawable.ic_star_filled);
            case 3:
                holder.star3.setImageResource(R.drawable.ic_star_filled);
            case 2:
                holder.star2.setImageResource(R.drawable.ic_star_filled);
            case 1:
                holder.star1.setImageResource(R.drawable.ic_star_filled);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    // Method to update the review list
    public void updateReviews(List<ReviewModel> newReviews) {
        this.reviewList = newReviews;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView textEmployee, textServices, textDate, textComment;
        ImageView star1, star2, star3, star4, star5;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            textEmployee = itemView.findViewById(R.id.textEmployee);
            textServices = itemView.findViewById(R.id.textServices);
            textDate = itemView.findViewById(R.id.textDate);
            textComment = itemView.findViewById(R.id.textComment);

            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}