package com.example.lotterypatentpending.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.Timestamp;
import java.util.*;

public class Notification {
    //Entrant is the primary recipient of messages

    //Organizer is the Sender of updates(event logistics and participant status)

    //LotterySystem generates automatic notifications following lottery draws to
    //inform users of their selection status

    /*FirebaseManager ensures notifications are
     * a)stored
     * b)timestamped
     * c)synchronized across devices through real-time database updates
     */
    @DocumentId
    //id of the notification
    private String id;
    //type of notification
    private String type;
    //Title
    private String title;
    //Body/content of the notification
    private String body;
    private String senderId;
    //List containing recipients of the notification
    private List<RecipientRef> recipients;

    /*Status of the notification
     * Either Pending, Sent or Read
     * We can include An Inbox view for entrants afterwards
     */
    private String status;

    //TimeStamp which is updated by Firebase
    private Timestamp createdAt;
    private Timestamp deliverAt;

    //Additional: keeping track of users who have read their notifications
    private List<String> readBy;

    //Constructor
    public Notification(){}  //For Firebase
    public Notification(String type, String title, String body, String senderId, List<RecipientRef> recipients){
        this.type = type;
        this.title = title;
        this.body = body;
        this.senderId = senderId;
        this.status = "PENDING";
        this.createdAt = Timestamp.now();
        this.readBy = new ArrayList<>();
    }
    //Getters
    public String getId(){
        return id;
    }
    public String getType(){
        return type;
    }
    public String getTitle(){
        return title;
    }
    public String getBody(){
        return body;
    }
    public String getSenderId(){
        return senderId;
    }
    public List<RecipientRef> getRecipients(){
        return recipients;
    }
    public String getStatus(){
        return status;
    }
    public Timestamp getCreatedAt(){
        return createdAt;
    }
    public Timestamp getDeliveredAt(){
        return deliverAt;
    }
    public List<String> getReadBy() { return readBy; }

    //Setters
    public void setId(String id){
        this.id = id;
    }
    public void setType(String type){
        this.type = type;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setBody(String body){
        this.body = body;
    }
    public void setSenderId(String senderId){
        this.senderId = senderId;
    }
    public void setRecipients(List<RecipientRef> recipients){
        this.recipients = recipients;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setCreatedAt(Timestamp createdAt){
        this.createdAt = createdAt;
    }
    public void setDeliverAt(Timestamp deliverAt){
        this.deliverAt = deliverAt;
    }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }


}
