package com.example.lotterypatentpending.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

/**
 * User-facing notification stored under users/{userId}/notifications/{id}.
 * Supports categories used in Entrant/Organizer stories (WIN/LOSE, WAITLIST, etc.).
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class Notification implements Parcelable {


    public enum Category { CHOSEN_SIGNUP, WAITLIST, SELECTED, CANCELLED, WIN, LOSE, ORGANIZER_MESSAGE }

    @DocumentId private String id;
    private String userId = "";
    private String eventId = "";
    // the logged-in user who organized the event
    private String senderId = "";
    private String title = "";
    private String body = "";
    private String status= "";
    private Category category = Category.WAITLIST;
    private List<RecipientRef>recipients;
    private boolean read = false;


    @ServerTimestamp @Nullable private Date createdAt;

    /** Required by Firestore. */
    public Notification() {}

    public Notification(@NonNull String userId, @NonNull String eventId, @NonNull String organizerId,
                        @NonNull String title, @NonNull String body, @NonNull Category category) {
        this.userId = userId; this.eventId = eventId; this.senderId = organizerId;
        this.title = title; this.body = body; this.category = category;
    }

    // getters/setters for Firestore
    public String getId() { return id; } public void setId(String id){ this.id=id; }
    public String getUserId(){ return userId; } public void setUserId(String v){ this.userId=v; }
    public String getEventId(){ return eventId; } public void setEventId(String v){ this.eventId=v; }
    public String getSenderId(){ return senderId; } public void setSenderId(String v){ this.senderId =v; }
    public String getTitle(){ return title; } public void setTitle(String v){ this.title=v; }
    public String getBody(){ return body; } public void setBody(String v){ this.body=v; }
    public Category getCategory(){ return category; } public void setCategory(Category c){ this.category=c; }
    public boolean isRead(){ return read; } public void setRead(boolean r){ this.read=r; }
    public String getStatus(){ return status; } public void setStatus(@NonNull String status) { this.status = status;}

    public List<RecipientRef> getRecipients() { return recipients;} public void setRecipients(List<RecipientRef> recipients) { this.recipients = recipients;}
    public Date getCreatedAt(){ return createdAt; } public void setCreatedAt(Date d){ this.createdAt=d; }

    // Parcelable
    protected Notification(Parcel in){
        id=in.readString(); userId=in.readString(); eventId=in.readString(); senderId =in.readString();
        title=in.readString(); body=in.readString(); category=Category.valueOf(in.readString());
        read=in.readByte()!=0; long ts=in.readLong(); createdAt=(ts==-1)?null:new Date(ts);
    }
    @Override public void writeToParcel(Parcel dest,int flags){
        dest.writeString(id); dest.writeString(userId); dest.writeString(eventId); dest.writeString(senderId);
        dest.writeString(title); dest.writeString(body); dest.writeString(category.name());
        dest.writeString(recipients.toString());
        dest.writeByte((byte)(read?1:0)); dest.writeLong(createdAt==null?-1:createdAt.getTime());
    }
    @Override public int describeContents(){ return 0; }
    public static final Creator<Notification> CREATOR=new Creator<>() {
        public Notification createFromParcel(Parcel in){ return new Notification(in); }
        public Notification[] newArray(int size){ return new Notification[size]; }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification n = (Notification) o;

        // If you treat two notifications as equal only when their Firestore doc IDs match:
        if (id == null || n.id == null) return false;
        return id.equals(n.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : 0;
    }

}
