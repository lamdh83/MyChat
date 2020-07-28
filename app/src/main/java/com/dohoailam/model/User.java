package com.dohoailam.model;

public class User {
    private String id;
    private String username;
    private String imageURL = "default";
    private String status;
    private String search;
    private String longlng = "0";
    private String latlng = "0";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public User(String id, String username, String imageURL, String status, String search, String longlng, String latlng) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.longlng = longlng;
        this.latlng = latlng;
    }

    public String getLonglng() {
        return longlng;
    }

    public void setLonglng(String longlng) {
        this.longlng = longlng;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public User(String id, String username, String imageURL) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
