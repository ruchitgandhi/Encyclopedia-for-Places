package com.example.ruchit.placessearch;

import com.google.gson.Gson;

public class ResultLine {
    private String placeName;
    private String placeAddress;
    private String categoryImgUrl;
    private double latitude;
    private double longitude;
    private String placeId;
    private boolean isFavorite;

    public ResultLine(String placeName, String placeAddress, String categoryImgUrl, double latitude,
                      double longitude, String placeId, boolean isFavorite) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.categoryImgUrl = categoryImgUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.isFavorite = isFavorite;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getCategoryImgUrl() {
        return categoryImgUrl;
    }

    public void setCategoryImgUrl(String categoryImgUrl) {
        this.categoryImgUrl = categoryImgUrl;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
