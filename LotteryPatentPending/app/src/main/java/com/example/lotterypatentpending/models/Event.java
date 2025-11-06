package com.example.lotterypatentpending.models;


import android.os.Build;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private LocalDateTime date;
    private int capacity;
    private int waitingListCapacity;
    private String location;
    private WaitingList waitingList;
    private List<User> entrants;
    private User organizer;
    private LocalDateTime regStartDate;
    private LocalDateTime regEndDate;
    private QRCode qrCode;
    private QRGenerator qrGenerator;
    private boolean active;

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
        this.date = null;
        this.location = null;
        this.waitingList = new WaitingList();
        this.entrants = new ArrayList<>();
        this.regStartDate = null;
        this.regEndDate = null;
        this.qrCode = new QRCode(this.id);
        this.active = false;
        this.waitingListCapacity = -1;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public LocalDateTime getRegStartDate() {
        return regStartDate;
    }

    public void setRegStartDate(LocalDateTime regStartDate) {
        this.regStartDate = regStartDate;
    }

    public LocalDateTime getRegEndDate() {
        return regEndDate;
    }

    public void setRegEndDate(LocalDateTime regEndDate) {
        this.regEndDate = regEndDate;
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

    public List<User> getEntrants() {
        return entrants;
    }

    public void setEntrants(List<User> entrants) {
        this.entrants = entrants;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public int getWaitingListCapacity() {
        return waitingListCapacity;
    }

    public void setWaitingListCapacity(int waitingListCapacity) {
        this.waitingListCapacity = waitingListCapacity;
    }

    public boolean isActive(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        }

        LocalDateTime now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }

        active = now.isAfter(regStartDate) && now.isBefore(regEndDate);
        return active;
    }

    public void addToWaitingList(User entrant){
        waitingList.addEntrant(entrant);
    }

    public void removeFromWaitingList(User entrant){
        waitingList.removeEntrant(entrant);
    }


   /**
     * logic to add a user/entrant to the event lottery or the waiting list
     * @param entrant
    * */
    public void joinEvent(User entrant){
       if(entrants.size() < capacity){
           entrants.add(entrant);
        }else{
            addToWaitingList(entrant);
        }
    }
    public boolean inWaitingList(User entrant) {
        return this.waitingList.checkEntrant(entrant);
    }

}
