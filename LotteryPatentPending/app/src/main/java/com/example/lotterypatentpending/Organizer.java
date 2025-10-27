package com.example.lotterypatentpending;

import java.util.ArrayList;

public class Organizer  {  //extends user
    private ArrayList<Event> events;
    private String type; // probably put this in User class along with a function getType()

    public Organizer(){
        //super();
        events = new ArrayList<Event>();
        type = "Organizer";
    }

    public void addEvent(Event event){
        if(!events.contains(event)){
            events.add(event);
        }
    }

    public void removeEvent(Event event){
        events.remove(event);
    }

    public ArrayList<Event> getEvents(){
        return events;
    }
}
