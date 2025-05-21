package com.example.androidexample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    public static final int TYPE_DATE = 0;
    public static final int TYPE_INCOMING = 1;
    public static final int TYPE_OUTGOING = 2;
    public static final int TYPE_EMAIL = 3;
    public static final int TYPE_LIKE = 4;

    private String content;
    private long timestamp;
    private int type;

    private long userID;

    public Message(String content, long timestamp, int type, long userID) {
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
        this.userID = userID;
    }

    public long getUserID() {
        return userID;

    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp)).toUpperCase();
    }
}