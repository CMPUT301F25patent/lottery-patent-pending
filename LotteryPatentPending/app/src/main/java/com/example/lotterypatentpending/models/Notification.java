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


    /**
     * Defines the purpose or context of the notification.
     */
    public enum Category {
        /** Notification indicating the user has been selected after sign-up. */
        CHOSEN_SIGNUP,
        /** Notification regarding the user being placed on the waitlist. */
        WAITLIST,
        /** Notification confirming the user has been officially selected as an entrant. */
        SELECTED,
        /** Notification informing the user that the event has been cancelled. */
        CANCELLED,
        /** Notification indicating the user has won a lottery or selection. */
        WIN,
        /** Notification indicating the user has lost a lottery or selection. */
        LOSE,
        /** A general message sent by the event organizer. */
        ORGANIZER_MESSAGE
    }

    /** The Firestore document ID for this notification. */
    @DocumentId private String id;
    /** The ID of the user who is the recipient of this notification. */
    private String userId = "";
    /** The ID of the event related to this notification. */
    private String eventId = "";
    /** The ID of the user (typically the organizer) who sent the notification. */
    private String senderId = "";
    /** The short, displayed title of the notification. */
    private String title = "";
    /** The main content or body of the notification. */
    private String body = "";
    /** An optional status string. */
    private String status= "";
    /** The category defining the type of notification. Defaults to {@link Category#WAITLIST}. */
    private Category category = Category.WAITLIST;
    /** A list of recipients (typically used for mass organizer messages). */
    private List<RecipientRef>recipients;
    /** Flag indicating whether the user has read the notification. */
    private boolean read = false;


    /** The timestamp indicating when the notification was created, set by the server. */
    @ServerTimestamp @Nullable private Date createdAt;

    /** Required by Firestore. */
    public Notification() {}

    /**
     * Constructor for creating a new notification before it is saved to Firestore.
     * @param userId The ID of the user receiving the notification.
     * @param eventId The ID of the related event.
     * @param organizerId The ID of the event organizer (sender).
     * @param title The title of the notification.
     * @param body The main content of the notification.
     * @param category The category of the notification.
     */
    public Notification(@NonNull String userId, @NonNull String eventId, @NonNull String organizerId,
                        @NonNull String title, @NonNull String body, @NonNull Category category) {
        this.userId = userId; this.eventId = eventId; this.senderId = organizerId;
        this.title = title; this.body = body; this.category = category;
    }

    // getters/setters for Firestore
    /** Gets the Firestore document ID. */
    public String getId() { return id; }
    /** Sets the Firestore document ID. */
    public void setId(String id){ this.id=id; }

    /** Gets the recipient user's ID. */
    public String getUserId(){ return userId; }
    /** Sets the recipient user's ID. */
    public void setUserId(String v){ this.userId=v; }

    /** Gets the related event's ID. */
    public String getEventId(){ return eventId; }
    /** Sets the related event's ID. */
    public void setEventId(String v){ this.eventId=v; }

    /** Gets the sender's user ID. */
    public String getSenderId(){ return senderId; }
    /** Sets the sender's user ID. */
    public void setSenderId(String v){ this.senderId =v; }

    /** Gets the notification title. */
    public String getTitle(){ return title; }
    /** Sets the notification title. */
    public void setTitle(String v){ this.title=v; }

    /** Gets the notification body content. */
    public String getBody(){ return body; }
    /** Sets the notification body content. */
    public void setBody(String v){ this.body=v; }

    /** Gets the notification category. */
    public Category getCategory(){ return category; }
    /** Sets the notification category. */
    public void setCategory(Category c){ this.category=c; }

    /** Checks if the notification has been read. */
    public boolean isRead(){ return read; }
    /** Sets the read status of the notification. */
    public void setRead(boolean r){ this.read=r; }

    /** Gets the optional status string. */
    public String getStatus(){ return status; }
    /** Sets the optional status string. */
    public void setStatus(@NonNull String status) { this.status = status;}

    /** Gets the list of additional recipients (if applicable). */
    public List<RecipientRef> getRecipients() { return recipients;}
    /** Sets the list of additional recipients. */
    public void setRecipients(List<RecipientRef> recipients) { this.recipients = recipients;}

    /** Gets the server-side creation timestamp. */
    public Date getCreatedAt(){ return createdAt; }
    /** Sets the server-side creation timestamp. */
    public void setCreatedAt(Date d){ this.createdAt=d; }

    // Parcelable
    /**
     * Constructor used for parcelable implementation to reconstruct the object from a Parcel.
     * @param in The Parcel containing the notification data.
     */
    protected Notification(Parcel in){
        id=in.readString(); userId=in.readString(); eventId=in.readString(); senderId =in.readString();
        title=in.readString(); body=in.readString();
        // Handles case where category might not exist in Parcel
        String categoryName = in.readString();
        if (categoryName != null) {
            category = Category.valueOf(categoryName);
        }

        read=in.readByte()!=0; long ts=in.readLong(); createdAt=(ts==-1)?null:new Date(ts);
    }

    /**
     * Flattens this object into a Parcel.
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override public void writeToParcel(Parcel dest,int flags){
        dest.writeString(id); dest.writeString(userId); dest.writeString(eventId); dest.writeString(senderId);
        dest.writeString(title); dest.writeString(body); dest.writeString(category.name());
        // Note: Assumes recipients.toString() is sufficient for serialization, but often requires more complex logic.
        dest.writeString(recipients != null ? recipients.toString() : null);
        dest.writeByte((byte)(read?1:0)); dest.writeLong(createdAt==null?-1:createdAt.getTime());
    }

    /**
     * Returns a bitmask indicating the set of special object types marshaled by this Parcelable object.
     * @return 0
     */
    @Override public int describeContents(){ return 0; }

    /**
     * Creator field used by the Parcelable mechanism to create new instances of the Notification class.
     */
    public static final Creator<Notification> CREATOR=new Creator<>() {
        /** Creates a new instance of the Parcelable class, instantiating it from the given Parcel. */
        public Notification createFromParcel(Parcel in){ return new Notification(in); }
        /** Creates a new array of the Parcelable class. */
        public Notification[] newArray(int size){ return new Notification[size]; }
    };

    /**
     * Compares this Notification object to another object for equality.
     * Equality is determined by matching the Firestore document IDs.
     * @param o The object to compare against.
     * @return true if the objects are equal (have the same non-null ID), false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification n = (Notification) o;

        // If you treat two notifications as equal only when their Firestore doc IDs match:
        if (id == null || n.id == null) return false;
        return id.equals(n.id);
    }

    /**
     * Returns a hash code value for the object, based on the Firestore document ID.
     * @return The hash code of the ID, or 0 if the ID is null.
     */
    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : 0;
    }

}