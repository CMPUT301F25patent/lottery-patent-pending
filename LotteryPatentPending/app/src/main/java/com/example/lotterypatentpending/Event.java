package com.example.lotterypatentpending;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private int capacity;
    private String location;
    //waiting list declaration
    //attendees declaration
    //organizer declaration
    private LocalDate regStartDate;
    private LocalTime regStartTime;
    private LocalDate regEndDate;
    private LocalTime regEndTime;
    //QR code declaration

    public Event(String title, String description, int capacity){
        this.title = title;
        this.description = description;
        this.capacity = capacity;
    }

    public Event(String title, String description, LocalDate date, LocalTime time, int capacity, String location){
        this(title, description, capacity);
        this.date = date;
        this.time = time;
        this.capacity = capacity;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getRegStartDate() {
        return regStartDate;
    }

    public void setRegStartDate(LocalDate regStartDate) {
        this.regStartDate = regStartDate;
    }

    public LocalTime getRegStartTime() {
        return regStartTime;
    }

    public void setRegStartTime(LocalTime regStartTime) {
        this.regStartTime = regStartTime;
    }

    public LocalDate getRegEndDate() {
        return regEndDate;
    }

    public void setRegEndDate(LocalDate regEndDate) {
        this.regEndDate = regEndDate;
    }

    public LocalTime getRegEndTime() {
        return regEndTime;
    }

    public void setRegEndTime(LocalTime regEndTime) {
        this.regEndTime = regEndTime;
    }
}
