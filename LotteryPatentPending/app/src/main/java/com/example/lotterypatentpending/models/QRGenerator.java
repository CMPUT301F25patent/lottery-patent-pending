package com.example.lotterypatentpending.models;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRGenerator {

    /**
     *
     * @param content type of event
     * @param size width/height in pixels(e.g. 512)
     * @return returns bitmap black and white containing thr QR code
     * @throws WriterException content cannot be encoded
     */
    public static Bitmap generate(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        return toBitmap(matrix);
    }

    /**
     * Convert the ZXing BitMatrix to a bitmap (black & white)
     * @param matrix QR code matrix
     * @return bitmap with same dimension
     */
    public static Bitmap toBitmap(BitMatrix matrix) {
        int w = matrix.getWidth(), h = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int x = 0; x<w; x++){
            for (int y = 0; y<h; y++){
                bmp.setPixel(x,y, matrix.get(x,y) ? 0xFF000000: 0xFFFFFFFF);
            }
        }
        return bmp;
    }
}
