package lk.oracene.hardware_management_api.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class BarcodeUtil {

    public static String generateBase64Barcode(String text) {
        try {
            BitMatrix bitMatrix = new Code128Writer().encode(text, BarcodeFormat.CODE_128, 400, 100, null);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate barcode for: " + text, e);
        }
    }
}
