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
 *   <li><b>organizerId</b> - sender uid</li>
 *   <li><b>eventId</b> - event in context</li>
 *   <li><b>category</b> - semantic bucket (WAITLIST, SELECTED, CHOSEN_SIGNUP, etc.)</li>
 *   <li><b>recipientIds</b> - final opted-in recipients</li>
 *   <li><b>payloadPreview</b> - first 100 chars of message body</li>
 *   <li><b>createdAt</b> - server timestamp for sorting</li>
 * </ul>
 *
 * @author Moffat
 * @maintainer Moffat
 *
 * <p>Parcelable for convenient bundling if needed.
 */


public class NotificationLog implements Parcelable {
    private String organizerId = "";
    private String eventId = "";
    private Category category = Category.WAITLIST;
    private List<String> recipientIds = new ArrayList<>();
    private String payloadPreview = "";
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
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<String> getRecipientIds() { return recipientIds; }
    public void setRecipientIds(List<String> recipientIds) { this.recipientIds = recipientIds; }

    public String getPayloadPreview() { return payloadPreview; }
    public void setPayloadPreview(String payloadPreview) { this.payloadPreview = payloadPreview; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Constructor for creating an instance from a Parcel.
     */
    protected NotificationLog(Parcel in){
        organizerId=in.readString(); eventId=in.readString(); category=Category.valueOf(in.readString());
        recipientIds=in.createStringArrayList(); payloadPreview=in.readString();
        long ts=in.readLong(); createdAt=(ts==-1)?null:new Date(ts);
    }
    /**
     * Flattens this object into a Parcel.
     */
    @Override public void writeToParcel(Parcel dest,int flags){
        dest.writeString(organizerId); dest.writeString(eventId); dest.writeString(category.name());
        dest.writeStringList(recipientIds); dest.writeString(payloadPreview);
        dest.writeLong(createdAt==null?-1:createdAt.getTime());
    }
    /**
     * CREATOR for generating instances of this class from a Parcel.
     */
    @Override public int describeContents(){return 0;}
    public static final Creator<NotificationLog> CREATOR=new Creator<>() {
        public NotificationLog createFromParcel(Parcel in){return new NotificationLog(in);}
        public NotificationLog[] newArray(int size){return new NotificationLog[size];}
    };
}
