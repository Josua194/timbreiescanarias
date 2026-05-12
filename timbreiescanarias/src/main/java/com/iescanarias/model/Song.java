package com.iescanarias.model;

public class Song {
    private int id;
    private String title;
    private String filePath;
    private Category category;
    private boolean active;

    public Song(int id, String title, String filePath, Category category, boolean active) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.category = category;
        this.active = active;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public Category getCategory() { return category; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return "[" + category + "] " + title + " -> " + filePath;
    }
}