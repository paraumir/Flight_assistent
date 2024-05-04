package com.example.kurs;

public class ListData {
    private String name;
    private String time;
    private String road;
    private String info;
    private String imageUrl;

    private String status;

    private String Flight;

    public ListData() {
    }

    public ListData(String name, String time, String road, String info, String imageUrl, String status) {
        this.name = name;
        this.time = time;
        this.road = road;
        this.info = info;
        this.imageUrl = imageUrl;
        this.status = status;
    }
    public String getstatus() {return status;}
    public void setstatus(String status) {this.status = status;}
    public String getFlight() {return Flight;}
    public void setFlight(String Flight) {this.Flight = Flight;}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

