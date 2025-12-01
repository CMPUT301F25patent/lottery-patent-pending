package com.example.lotterypatentpending;

import com.example.lotterypatentpending.models.QRGenerator;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.junit.Test;
import static org.junit.Assert.*;

public class QRGeneratorTest {

    @Test
    public void testEncodeToMatrix_hasCorrectSize() throws WriterException {
        int size = 256;
        BitMatrix matrix = QRGenerator.encodeToMatrix("EVT:ABC", size);

        assertNotNull("BitMatrix should not be null", matrix);
        assertEquals("Width should match requested size", size, matrix.getWidth());
        assertEquals("Height should match requested size", size, matrix.getHeight());
    }

    @Test
    public void testEncodeToMatrix_changesWithContent() throws WriterException {
        int size = 64;
        BitMatrix m1 = QRGenerator.encodeToMatrix("EVT:ABC", size);
        BitMatrix m2 = QRGenerator.encodeToMatrix("EVT:XYZ", size);

        assertEquals("Width of first matrix", size, m1.getWidth());
        assertEquals("Height of first matrix", size, m1.getHeight());
        assertEquals("Width of second matrix", size, m2.getWidth());
        assertEquals("Height of second matrix", size, m2.getHeight());

        boolean anyDifferent = false;
        for (int x = 0; x < size && !anyDifferent; x++) {
            for (int y = 0; y < size && !anyDifferent; y++) {
                if (m1.get(x, y) != m2.get(x, y)) {
                    anyDifferent = true;
                }
            }
        }

        assertTrue("BitMatrix for different contents should not be identical", anyDifferent);
    }

    @Test
    public void testEncodeToMatrix_sameContent_isDeterministic() throws WriterException {
        int size = 64;
        BitMatrix m1 = QRGenerator.encodeToMatrix("EVT:SAME", size);
        BitMatrix m2 = QRGenerator.encodeToMatrix("EVT:SAME", size);

        boolean allSame = true;
        for (int x = 0; x < size && allSame; x++) {
            for (int y = 0; y < size && allSame; y++) {
                if (m1.get(x, y) != m2.get(x, y)) {
                    allSame = false;
                }
            }
        }

        assertTrue("BitMatrix for same content should be identical", allSame);
    }
}
