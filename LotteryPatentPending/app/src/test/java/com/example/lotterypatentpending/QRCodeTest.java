package com.example.lotterypatentpending;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.example.lotterypatentpending.models.QRCode;


public class QRCodeTest {
    private String event = "ABC";
    private String eventPayload = "EVT:ABC";

    @Test
    public void testQRConstructor(){
        QRCode qrcode = new QRCode(event);
        assertSame(qrcode.getEventId(), event);
    }

    @Test
    public void testQRToPayload(){
        QRCode qrcode = new QRCode(event);
        assertTrue(qrcode.toContent().equals(eventPayload) );
    }

    @Test
    public void testQRFromPayload(){
        QRCode qrcode = new QRCode(event);
        String payload = qrcode.toContent();
        assertTrue(payload.equals(eventPayload));
        assertFalse(payload.equals(event));
    }

    @Test
    public void testConstructorException(){
        assertThrows(IllegalArgumentException.class, () -> new QRCode(null));
        assertThrows(IllegalArgumentException.class, () -> new QRCode(""));
        assertThrows(IllegalArgumentException.class, () -> new QRCode("   "));
    }

}
