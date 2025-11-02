package com.example.lotterypatentpending.models;


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

    /**
     * String to embed inside the QR code
     */

    public String toPayload(){
        return PREFIX + eventId;
    }

    /**
     * Checks if our prefix in QRCode
     */
    public static QRCode fromPayload(String s){
        if ( s == null || !s.startsWith(PREFIX) ) return null;
        String id = s.substring(PREFIX.length()).trim();
        if (id.isEmpty()) return null;
        return new QRCode(id);
    }
}
