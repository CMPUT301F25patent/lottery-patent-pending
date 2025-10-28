package com.example.lotterypatentpending.models;


/**
 * Class to represent a QR code
 * @maintainer Erik
 * @author Erik
 */
public class QRCodeModel {

    //Helpful for identifying if QRCode is ours
    private static final String PREFIX = "EVT:";
    private String eventId; //app data


    /**
     * @param eventId Event ID associated with QRCode
     */
    public QRCodeModel(String eventId){
        this.eventId = eventId;
    }

    public String getEventId(){
        return this.eventId;
    }

    /**
     * String to embed inside the QR code
     */

    public String toPayLoad(){
        return PREFIX + eventId;
    }

    /**
     * Checks if our prefix in QRCode
     */
    public static QRCodeModel fromPayload(String s){
        if ( s == null || !s.startsWith(PREFIX) ) return null;
        String id = s.substring(PREFIX.length()).trim();
        if (id.isEmpty()) return null;
        return new QRCodeModel(id);
    }



}
