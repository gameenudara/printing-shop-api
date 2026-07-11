package lk.oracene.hardware_management_api.service;

import lk.oracene.hardware_management_api.dto.response.SalesResponse;
import lk.oracene.hardware_management_api.model.PrinterSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ReceiptBuilder {

    private static final byte[] ESC_INIT = {0x1B, 0x40};
    private static final byte[] LF = {0x0A};
    private static final byte[] CUT = {0x1D, 0x56, 0x42, 0x00};
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};

    private static final int LINE_CHARS = 48;
    private static final String DASH_LINE = "------------------------------------------------";
    private static final String SHORT_DASH = "       --------------------------------       ";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

    public byte[] buildSalesReceipt(SalesResponse sale, PrinterSettings settings) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String charset = settings.getCharset() != null ? settings.getCharset() : "CP437";
            int paperWidth = settings.getPaperWidthDots() != null ? settings.getPaperWidthDots() : 576;

            out.write(ESC_INIT);

            writeHeaderImage(out, paperWidth);

            writeLine(out, DASH_LINE, charset);

            writeLine(out, twoColumns("Invoice:", sale.getInvoiceNumber()), charset);
            if (sale.getSaleDate() != null) {
                writeLine(out, twoColumns("Date/Time:", sale.getSaleDate().format(DATE_FMT)), charset);
            }
            if (sale.getCashier() != null) {
                writeLine(out, twoColumns("Cashier:", sale.getCashier()), charset);
            }
            if (sale.getCustomerName() != null) {
                writeLine(out, twoColumns("Customer:", sale.getCustomerName()), charset);
            }

            writeLine(out, DASH_LINE, charset);
            writeLine(out, String.format("  %-6s %10s %10s %14s", "QTY", "PRICE", "DISC", "AMOUNT"), charset);
            writeLine(out, DASH_LINE, charset);

            BigDecimal totalSaved = BigDecimal.ZERO;
            int itemCount = 0;

            if (sale.getItems() != null) {
                for (int i = 0; i < sale.getItems().size(); i++) {
                    var item = sale.getItems().get(i);
                    itemCount++;

                    writeLine(out, (i + 1) + ". " + item.getProductName(), charset);

                    BigDecimal grossTotal = item.getUnitPrice()
                            .multiply(item.getQuantity())
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal itemDisc = grossTotal.subtract(item.getLineTotal()).max(BigDecimal.ZERO);
                    totalSaved = totalSaved.add(itemDisc);

                    String qty = formatQty(item.getQuantity());
                    String price = fmtComma(item.getUnitPrice());
                    String disc = fmtComma(itemDisc);
                    String amount = fmtComma(item.getLineTotal());

                    writeLine(out, String.format("  %-6s %10s %10s %14s", qty, price, disc, amount), charset);
                }
            }

            writeLine(out, DASH_LINE, charset);

            writeLine(out, twoColumns("Subtotal:", "Rs. " + fmtComma(sale.getSubTotal())), charset);
            if (sale.getDiscountAmount() != null && sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalSaved = totalSaved.add(sale.getDiscountAmount());
                writeLine(out, twoColumns("Discount:", "-Rs. " + fmtComma(sale.getDiscountAmount())), charset);
            }

            writeLine(out, DASH_LINE, charset);

            out.write(BOLD_ON);
            writeLine(out, twoColumns("NET TOTAL:", "Rs. " + fmtComma(sale.getTotalAmount())), charset);
            out.write(BOLD_OFF);

            writeLine(out, DASH_LINE, charset);

            if (sale.getPayments() != null && !sale.getPayments().isEmpty()) {
                String method = sale.getPayments().getFirst().getMethod().name();
                if (sale.getReceivedAmount() != null
                        && sale.getReceivedAmount().compareTo(sale.getPaidAmount()) > 0) {
                    writeLine(out, twoColumns("Received:", "Rs. " + fmtComma(sale.getReceivedAmount())), charset);
                }
                writeLine(out, twoColumns("Method:", method), charset);
            }
            if (sale.getRemainingAmount() != null && sale.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                writeLine(out, twoColumns("Balance:", "Rs. " + fmtComma(sale.getRemainingAmount())), charset);
            }
            if (sale.getChangeAmount() != null && sale.getChangeAmount().compareTo(BigDecimal.ZERO) > 0) {
                writeLine(out, twoColumns("Change:", "Rs. " + fmtComma(sale.getChangeAmount())), charset);
            }

            writeLine(out, DASH_LINE, charset);

            if (totalSaved.compareTo(BigDecimal.ZERO) > 0) {
                writeLine(out, SHORT_DASH, charset);
                out.write(ALIGN_CENTER);
                writeLine(out, "You Saved: Rs. " + fmtComma(totalSaved), charset);
                out.write(ALIGN_LEFT);
                writeLine(out, SHORT_DASH, charset);
            }

            writeLine(out, DASH_LINE, charset);
            writeLine(out, twoColumns("Total Items:", String.valueOf(itemCount)), charset);
            writeLine(out, DASH_LINE, charset);

            out.write(ALIGN_CENTER);
            writeLine(out, sale.getInvoiceNumber(), charset);
            out.write(LF);
            writeLine(out, "Thank you for your purchase!", charset);
            out.write(ALIGN_LEFT);
            writeLine(out, DASH_LINE, charset);
            out.write(ALIGN_CENTER);
            writeLine(out, "Software by Oracene (077 634 2431)", charset);
            out.write(ALIGN_LEFT);

            for (int i = 0; i < 2; i++) {
                out.write(LF);
            }

            if (Boolean.TRUE.equals(settings.getAutoCut())) {
                out.write(CUT);
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build receipt", e);
        }
    }

    private void writeHeaderImage(ByteArrayOutputStream out, int paperWidth) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/static/images/siyapatah-printers.png")) {
            if (is == null) {
                log.warn("Header image not found at /static/images/siyapatah-printers.png");
                return;
            }
            BufferedImage img = ImageIO.read(is);
            if (img == null) return;

            out.write(ALIGN_CENTER);
            out.write(imageToRaster(img, paperWidth));
            out.write(ALIGN_LEFT);
        }
    }

    private byte[] imageToRaster(BufferedImage original, int maxWidthDots) throws IOException {
        int targetWidth = Math.min(original.getWidth(), maxWidthDots);
        float ratio = (float) targetWidth / original.getWidth();
        int targetHeight = (int) (original.getHeight() * ratio);

        BufferedImage mono = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = mono.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        int widthBytes = (targetWidth + 7) / 8;

        ByteArrayOutputStream raster = new ByteArrayOutputStream();
        raster.write(0x1D);
        raster.write(0x76);
        raster.write(0x30);
        raster.write(0x00);
        raster.write(widthBytes & 0xFF);
        raster.write((widthBytes >> 8) & 0xFF);
        raster.write(targetHeight & 0xFF);
        raster.write((targetHeight >> 8) & 0xFF);

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < widthBytes; x++) {
                int b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int px = x * 8 + bit;
                    if (px < targetWidth) {
                        int gray = mono.getRGB(px, y) & 0xFF;
                        if (gray < 128) {
                            b |= (0x80 >> bit);
                        }
                    }
                }
                raster.write(b);
            }
        }

        return raster.toByteArray();
    }

    private void writeLine(ByteArrayOutputStream out, String text, String charset) throws IOException {
        out.write((text + "\n").getBytes(charset));
    }

    private String twoColumns(String left, String right) {
        int padding = LINE_CHARS - left.length() - right.length();
        if (padding < 1) {
            left = left.substring(0, Math.max(1, LINE_CHARS - right.length() - 1));
            padding = 1;
        }
        return left + " ".repeat(padding) + right;
    }

    private String fmtComma(BigDecimal amount) {
        if (amount == null) return "0.00";
        return new DecimalFormat("#,##0.00").format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatQty(BigDecimal qty) {
        if (qty == null) return "0";
        if (qty.stripTrailingZeros().scale() <= 0) {
            return qty.toBigInteger().toString();
        }
        return qty.toPlainString();
    }
}
