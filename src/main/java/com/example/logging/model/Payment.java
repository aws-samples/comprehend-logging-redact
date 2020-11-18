package com.example.logging.model;

public class Payment {

    private final long id;
    private final String content;

    public Payment(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}