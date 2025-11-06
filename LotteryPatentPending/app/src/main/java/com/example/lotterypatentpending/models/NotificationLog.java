package com.example.lotterypatentpending.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.lotterypatentpending.models.Notification.Category;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.*;

public class NotificationLog implements Parcelable {
    private String organizerId = "";
    private String eventId = "";
    private Category category = Category.WAITLIST;
    private List<String> recipientIds = new ArrayList<>();
    private String payloadPreview = "";
    @ServerTimestamp private Date createdAt;

    public NotificationLog() {}
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

    protected NotificationLog(Parcel in){
        organizerId=in.readString(); eventId=in.readString(); category=Category.valueOf(in.readString());
        recipientIds=in.createStringArrayList(); payloadPreview=in.readString();
        long ts=in.readLong(); createdAt=(ts==-1)?null:new Date(ts);
    }
    @Override public void writeToParcel(Parcel dest,int flags){
        dest.writeString(organizerId); dest.writeString(eventId); dest.writeString(category.name());
        dest.writeStringList(recipientIds); dest.writeString(payloadPreview);
        dest.writeLong(createdAt==null?-1:createdAt.getTime());
    }
    @Override public int describeContents(){return 0;}
    public static final Creator<NotificationLog> CREATOR=new Creator<>() {
        public NotificationLog createFromParcel(Parcel in){return new NotificationLog(in);}
        public NotificationLog[] newArray(int size){return new NotificationLog[size];}
    };
}
