package com.example.androidUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.Message;
import com.example.androidexample.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Message> messageList;
    private final Context context;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case Message.TYPE_DATE:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_date, parent, false);
                return new DateViewHolder(view);
            case Message.TYPE_INCOMING:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_incoming, parent, false);
                return new IncomingViewHolder(view);
            case Message.TYPE_OUTGOING:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_outgoing, parent, false);
                return new OutgoingViewHolder(view);
            case Message.TYPE_EMAIL:
                view = LayoutInflater.from(context).inflate(R.layout.item_email_message, parent, false);
                return new EmailViewHolder(view);
            case Message.TYPE_LIKE:
                view = LayoutInflater.from(context).inflate(R.layout.item_like_message, parent, false);
                return new LikeViewHolder(view);
            default:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_outgoing, parent, false);
                return new OutgoingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case Message.TYPE_DATE:
                DateViewHolder dateHolder = (DateViewHolder) holder;
                dateHolder.dateSeparator.setText(message.getFormattedDate());
                break;
            case Message.TYPE_INCOMING:
                IncomingViewHolder incomingHolder = (IncomingViewHolder) holder;
                incomingHolder.messageText.setText(message.getContent());
                incomingHolder.messageTime.setText(message.getFormattedTime());
                break;
            case Message.TYPE_OUTGOING:
                OutgoingViewHolder outgoingHolder = (OutgoingViewHolder) holder;
                outgoingHolder.messageText.setText(message.getContent());
                outgoingHolder.messageTime.setText(message.getFormattedTime());
                break;
            case Message.TYPE_EMAIL:
                EmailViewHolder emailHolder = (EmailViewHolder) holder;
                emailHolder.emailText.setText(message.getContent());
                emailHolder.messageTime.setText(message.getFormattedTime());
                break;
            case Message.TYPE_LIKE:
                // No content to set for Like message
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolders for each message type
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateSeparator;

        DateViewHolder(View itemView) {
            super(itemView);
            dateSeparator = itemView.findViewById(R.id.dateSeparator);
        }
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;

        IncomingViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;

        OutgoingViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    static class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;
        TextView messageTime;

        EmailViewHolder(View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.emailText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    static class LikeViewHolder extends RecyclerView.ViewHolder {
        LikeViewHolder(View itemView) {
            super(itemView);
        }
    }
}