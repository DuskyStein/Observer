package com.example.observer;

public class LocationData {
    private Double Latitude;
    private Double Longtitude;
    private Double Altitude;
    private Float Accuracy;
    private String Address;
    private Float Speed;
    private String UserDevice;
    private String Date;

    public LocationData() {
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongtitude() {
        return Longtitude;
    }

    public void setLongtitude(Double longtitude) {
        Longtitude = longtitude;
    }

    public Double getAltitude() {
        return Altitude;
    }

    public void setAltitude(Double altitude) {
        Altitude = altitude;
    }

    public Float getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(Float accuracy) {
        Accuracy = accuracy;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Float getSpeed() {
        return Speed;
    }

    public void setSpeed(Float speed) {
        Speed = speed;
    }

    public void setUserDevice(String s) { UserDevice = s;}
    public String getUserDevice() {
        return UserDevice;
    }

    public String getDate() {return Date;}

    public void setDate(String date) {Date = date; }

    public LocationData (Double Latitude, Double Longtitude, Double Altitude, Float Accuracy, String Address, Float Speed, String UserDevice, String Date ){
        this.Latitude = Latitude;
        this.Longtitude = Longtitude;
        this.Accuracy = Accuracy;
        this.Altitude = Altitude;
        this.Speed = Speed;
        this.Address = Address;
        this.UserDevice = UserDevice;
        this.Date = Date;
    }
}