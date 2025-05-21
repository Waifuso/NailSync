package com.example.androidUI;

public class NailDesignModel
{
    private String name;
    private String imageUrl;

    public NailDesignModel(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}