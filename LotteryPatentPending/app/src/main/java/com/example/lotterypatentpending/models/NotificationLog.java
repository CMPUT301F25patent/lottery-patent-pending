package com.example.lotterypatentpending.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.lotterypatentpending.models.Notification.Category;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.*;

/**
 * Audit record for admin: one row per organizer send.
 *
 * <p>Stored at: admin/notificationsLog/records/{autoId}. Captures the
 * intent of a fan-out operation (who sent what to which list), without
 * duplicating the entire Notification documents.
 *
 * <ul>
 * <li><b>organizerId</b> - sender uid</li>
 * <li><b>eventId</b> - event in context</li>
 * <li><b>category</b> - semantic bucket (WAITLIST, SELECTED, CHOSEN_SIGNUP, etc.)</li>
 * <li><b>recipientIds</b> - final opted-in recipients</li>
 * <li><b>payloadPreview</b> - first 100 chars of message body</li>
 * <li><b>createdAt</b> - server timestamp for sorting</li>
 * </ul>
 *
 * @author Moffat
 * @maintainer Moffat
 *
 * <p>Parcelable for convenient bundling if needed.
 */
public class NotificationLog implements Parcelable {
    /** The ID of the user (organizer) who initiated the notification send. */
    private String organizerId = "";
    /** The ID of the event related to the notification. */
    private String eventId = "";
    /** The category defining the type or purpose of the notification. */
    private Category category = Category.WAITLIST;
    /** A list of user IDs who received the notification. */
    private List<String> recipientIds = new ArrayList<>();
    /** A truncated preview of the notification's message body. */
    private String payloadPreview = "";
    /** The server-side timestamp indicating when this log was created. */
    @ServerTimestamp private Date createdAt;

    /** No-arg constructor for Firestore deserialization. */
    public NotificationLog() {}

    /**
     * Constructs a new NotificationLog.
     * @param org Organizer's user ID.
     * @param evt Event ID.
     * @param cat Notification category.
     * @param rec List of recipient user IDs.
     * @param preview A short preview of the notification content.
     */
    public NotificationLog(String org,String evt,Category cat,List<String> rec,String preview){
        this.organizerId=org; this.eventId=evt; this.category=cat; this.recipientIds=rec; this.payloadPreview=preview;
    }

    // getters/setters
    /** Gets the organizer's user ID. */
    public String getOrganizerId() { return organizerId; }
    /** Sets the organizer's user ID. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** Gets the related event ID. */
    public String getEventId() { return eventId; }
    /** Sets the related event ID. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** Gets the notification category. */
    public Category getCategory() { return category; }
    /** Sets the notification category. */
    public void setCategory(Category category) { this.category = category; }

    /** Gets the list of recipient user IDs. */
    public List<String> getRecipientIds() { return recipientIds; }
    /** Sets the list of recipient user IDs. */
    public void setRecipientIds(List<String> recipientIds) { this.recipientIds = recipientIds; }

    /** Gets the payload preview string. */
    public String getPayloadPreview() { return payloadPreview; }
    /** Sets the payload preview string. */
    public void setPayloadPreview(String payloadPreview) { this.payloadPreview = payloadPreview; }

    /** Gets the creation timestamp. */
    public Date getCreatedAt() { return createdAt; }
    /** Sets the creation timestamp. */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Constructor for creating an instance from a Parcel.
     * @param in The Parcel containing the object data.
     */
    protected NotificationLog(Parcel in){
        organizerId=in.readString(); eventId=in.readString(); category=Category.valueOf(in.readString());
        recipientIds=in.createStringArrayList(); payloadPreview=in.readString();
        long ts=in.readLong(); createdAt=(ts==-1)?null:new Date(ts);
    }

    /**
     * Flattens this object into a Parcel.
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     */
    @Override public void writeToParcel(Parcel dest,int flags){
        dest.writeString(organizerId); dest.writeString(eventId); dest.writeString(category.name());
        dest.writeStringList(recipientIds); dest.writeString(payloadPreview);
        dest.writeLong(createdAt==null?-1:createdAt.getTime());
    }

    /**
     * Returns a bitmask indicating the set of special object types marshaled by this Parcelable object.
     * @return 0
     */
    @Override public int describeContents(){return 0;}

    /**
     * Creator field used by the Parcelable mechanism to create new instances of the NotificationLog class.
     */
    public static final Creator<NotificationLog> CREATOR=new Creator<>() {
        /** Creates a new instance of the Parcelable class, instantiating it from the given Parcel. */
        public NotificationLog createFromParcel(Parcel in){return new NotificationLog(in);}
        /** Creates a new array of the Parcelable class. */
        public NotificationLog[] newArray(int size){return new NotificationLog[size];}
    };
}