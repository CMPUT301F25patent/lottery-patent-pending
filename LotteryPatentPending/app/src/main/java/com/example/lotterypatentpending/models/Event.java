package com.example.lotterypatentpending.models;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class model for Events
 * @maintainer Ebuka
 * @author Ebuka
 */
public class Event {
    private String id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private int capacity;
    private String location;
    private WaitingList waitingList;
    private User organizer;
    private LocalDate regStartDate;
    private LocalTime regStartTime;
    private LocalDate regEndDate;
    private LocalTime regEndTime;
    private QRCode qrcode;

    /**
     * Constructor instantiates the minimal basic information for an event and sets the rest to the default
     * You can then set the other variables with the setter functions for better efficiency
     * @param title title of event
     * @param description event description
     * @param capacity event capacity
     * @param organizer organizer of the event
     */
    public Event(String title, String description, int capacity, User organizer){
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.organizer = organizer;
        this.waitingList = new WaitingList();
        this.qrcode = new QRCode(this.id);
    }


//    public Event(String title, String description, LocalDate date, LocalTime time, int capacity, User organizer, String location){
//        this(title, description, capacity, organizer);
//        this.date = date;
//        this.time = time;
//        this.capacity = capacity;
//        this.location = location;
//    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WaitingList getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public QRCode getQrcode() {
        return qrcode;
    }

    public void setQrcode(QRCode qrcode) {
        this.qrcode = qrcode;
    }

    public void addToWaitingList(User entrant) {
        this.waitingList.addEntrant(entrant);
    }

    public void removeFromWaitingList(User entrant) {
        this.waitingList.removeEntrant(entrant);
    }

}
