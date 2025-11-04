package com.example.lotterypatentpending.models;

import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.RecipientRef;

import java.util.ArrayList;
import java.util.List;

public final class NotificationFactory {

    private NotificationFactory() {}

    public static Notification lotteryWin(String eventId, String eventTitle,
                                          String organizerId, List<String> recipientIds) {
        Notification n = base(organizerId, recipientIds);
        n.setType("LOTTERY_WIN");
        n.setTitle("You're in! " + eventTitle);
        n.setBody("Congrats — you were selected for “" + eventTitle + "”. "
                + "Open the app to confirm your spot.");
        n.setId(eventId); // if your model has this; if not, remove
        return n;
    }

    public static Notification lotteryLose(String eventId, String eventTitle,
                                           String organizerId, List<String> recipientIds) {
        Notification n = base(organizerId, recipientIds);
        n.setType("LOTTERY_RESULT");
        n.setTitle("Update for " + eventTitle);
        n.setBody("Thanks for entering. You weren’t selected this time. "
                + "You remain on the waiting list if spots open.");
        n.setId(eventId); // if supported
        return n;
    }

    public static Notification custom(String organizerId, String title,
                                      String body, List<String> recipientIds) {
        Notification n = base(organizerId, recipientIds);
        n.setType("ORGANIZER_MESSAGE");
        n.setTitle(title);
        n.setBody(body);
        return n;
    }

    private static Notification base(String organizerId, List<String> recipientIds) {
        Notification n = new Notification();
        n.setSenderId(organizerId);
        n.setStatus("SENT");
        n.setRecipients(toRecipients(recipientIds));
        return n;
    }

    private static List<RecipientRef> toRecipients(List<String> ids) {
        List<RecipientRef> out = new ArrayList<>();
        if (ids == null) return out;
        for (String id : ids) {
            out.add(new RecipientRef(id));
        }
        return out;
    }
}


