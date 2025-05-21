package com.example.androidUI;

import java.util.Date;

public class Note {
    private String id;
    private String title;
    private String content;
    private Date createdDate;
    private Date modifiedDate;

    // default constructor required for JSON
    public Note() {
    }

    public Note(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    // constructor for creating a new note (ID will be assigned by the backend)
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    // methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    // update the modified date when editing a note
    public void updateModifiedDate() {
        this.modifiedDate = new Date();
    }
}
