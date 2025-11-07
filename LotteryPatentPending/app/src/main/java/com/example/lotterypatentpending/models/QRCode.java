package com.example.lotterypatentpending.models;


import android.graphics.Bitmap;

/**
 * Class to represent a QR code
 * @maintainer Erik
 * @author Erik
 */
public class QRCode {

    //Helpful for identifying if QRCode is ours
    private static final String PREFIX = "EVT:";
    private String eventId; //app data

    /**
     * @param eventId Event ID associated with QRCode
     */
    public QRCode(String eventId){
        if (eventId == null || eventId.trim().isEmpty())
            throw new IllegalArgumentException("eventId required");
        this.eventId = eventId;
    }

    public String getEventId(){
        return this.eventId;
    }

    public void setEventId(String eventId){
        this.eventId = eventId;
    }

    /**
     * String to embed inside the QR code
     * Easier for QR code to indentify which QR belongs to events
     */

    public String toContent(){
        return PREFIX + eventId;
    }

    /**
     * Checks if our prefix in QRCode
     */
    public static QRCode fromContent(String s){
        if ( s == null || !s.startsWith(PREFIX) ) return null;
        String id = s.substring(PREFIX.length()).trim();
        if (id.isEmpty()) return null;
        return new QRCode(id);
    }
}
