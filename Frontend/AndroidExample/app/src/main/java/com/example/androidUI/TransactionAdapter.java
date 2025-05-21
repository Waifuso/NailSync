package com.example.androidUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.PointsHistoryActivity;
import com.example.androidexample.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<PointsHistoryActivity.Transaction> transactions;

    public TransactionAdapter(List<PointsHistoryActivity.Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        PointsHistoryActivity.Transaction transaction = transactions.get(position);

        // Set title and date
        holder.transactionTitle.setText(transaction.getTitle());
        holder.transactionDate.setText(transaction.getDate());

        // Set points value with appropriate color
        holder.pointsValue.setText(transaction.getFormattedPointsValue());
        if (transaction.getPointsValue() > 0) {
            holder.pointsValue.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSuccess));
        } else {
            holder.pointsValue.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorError));
        }

        // Set icon based on transaction type
        if (transaction.getType() == PointsHistoryActivity.Transaction.TYPE_EARNED) {
            holder.transactionIcon.setImageResource(R.drawable.ic_points_earned);
            holder.transactionIcon.setBackgroundResource(R.drawable.circular_icon_background_earned);
        } else {
            holder.transactionIcon.setImageResource(R.drawable.ic_points_redeemed);
            holder.transactionIcon.setBackgroundResource(R.drawable.circular_icon_background_redeemed);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView transactionIcon;
        TextView transactionTitle;
        TextView transactionDate;
        TextView pointsValue;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
            transactionTitle = itemView.findViewById(R.id.transactionTitle);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            pointsValue = itemView.findViewById(R.id.pointsValue);
        }
    }
}