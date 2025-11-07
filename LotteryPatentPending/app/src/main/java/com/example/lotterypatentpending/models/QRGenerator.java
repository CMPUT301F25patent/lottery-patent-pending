package com.example.lotterypatentpending.models;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Helper class for creating QR codes in the app.
 *
 * This class turns a string payload into a QR code, first as a BitMatrix
 * (ZXing format) and then as an Android Bitmap. It also has a convenience
 * method to drop a QR code straight into an ImageView.
 *
 * Known limitations:
 * - Always renders black on white, no customization of colors.
 * - Runs synchronously; using very large sizes on the main thread could
 *   cause jank.
 *
 * @author Erik
 */
public class QRGenerator {

    /**
     * Encodes the given string into a ZXing BitMatrix for a QR code.
     *
     * @param content the payload to encode (for example, QRCode.toContent())
     * @param size    width and height of the matrix in pixels
     * @return a square BitMatrix representing the QR code
     * @throws WriterException if the content cannot be encoded as a QR code
     */
    public static BitMatrix encodeToMatrix(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        return writer.encode(content, BarcodeFormat.QR_CODE, size, size);
    }

    /**
     * Generates a Bitmap QR code for the given content.
     *
     * @param content the payload to encode in the QR code
     * @param size    width and height of the bitmap in pixels
     * @return a black-and-white bitmap containing the QR code
     * @throws WriterException if the content cannot be encoded as a QR code
     */
    public static Bitmap generate(String content, int size) throws WriterException {
        BitMatrix matrix = encodeToMatrix(content, size);
        return toBitmap(matrix);
    }

    /**
     * Converts a ZXing BitMatrix into a black-and-white Bitmap.
     *
     * @param matrix the QR code matrix to convert
     * @return a bitmap with the same dimensions, where true bits are black
     *         and false bits are white
     */
    public static Bitmap toBitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }

    /**
     * Builds a QR code for the given event ID and sets it on the target ImageView.
     * Internally this creates a QRCode, turns it into a payload string, generates
     * the bitmap, and assigns it to the view.
     *
     * @param target  ImageView where the QR code should be shown
     * @param eventId ID of the event to encode
     * @param sizePx  width and height of the QR code bitmap in pixels
     */
    public static void setQRToView(ImageView target, String eventId, int sizePx) {
        String payload = new QRCode(eventId).toContent();

        try {
            Bitmap bmp = generate(payload, sizePx);
            target.setImageBitmap(bmp);
        } catch (WriterException e) {
            // In this app we treat this as unexpected; rethrow as unchecked.
            throw new RuntimeException(e);
        }
    }
}
