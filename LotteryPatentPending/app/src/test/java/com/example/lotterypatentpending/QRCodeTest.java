package com.example.lotterypatentpending;


import org.junit.Test;
import static org.junit.Assert.*;
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
    public void testFromPayload(){
        QRCode qrcode1 = new QRCode(event);
        QRCode qrcode2 = QRCode.fromContent(eventPayload);

        assertEquals(qrcode2.getEventId(), qrcode1.getEventId());
    }

    @Test
    public void testConstructorException(){
        assertThrows(IllegalArgumentException.class, () -> new QRCode(null));
        assertThrows(IllegalArgumentException.class, () -> new QRCode(""));
        assertThrows(IllegalArgumentException.class, () -> new QRCode("   "));
    }

}
