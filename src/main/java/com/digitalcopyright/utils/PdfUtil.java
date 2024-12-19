package com.digitalcopyright.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfUtil {

    public static byte[] generatePdf(String username, String workId, String title, String description,
                                     String userAddress,  String digitalCopyrightId,
                                     String reviewersAddress, String createdAt, String qrContent)
            throws IOException, WriterException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 加载自定义字体
        PdfFont customFont = PdfFontFactory.createFont("src/main/resources/fonts/HYSongYunLangHeiW-1.ttf",
                PdfEncodings.IDENTITY_H);

        // 标题居中显示
        Paragraph titleParagraph = new Paragraph("版权证书")
                .setFont(customFont)
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titleParagraph);

        // 添加用户信息
        document.add(new Paragraph("作品 ID: " + workId).setFont(customFont).setFontSize(12).setMarginBottom(20));

        // 添加作品信息
        document.add(new Paragraph("标题: " + title).setFont(customFont).setFontSize(12).setMarginBottom(10));
        document.add(new Paragraph("描述: " + description).setFont(customFont).setFontSize(12).setMarginBottom(10));
        document.add(new Paragraph("拥有者: " + username).setFont(customFont).setFontSize(12).setMarginBottom(10));
        document.add(new Paragraph("账户: " + userAddress).setFont(customFont).setFontSize(12).setMarginBottom(20));
        document.add(new Paragraph("版权编号: " + digitalCopyrightId).setFont(customFont).setFontSize(12).setMarginBottom(10));
        document.add(new Paragraph("审核地址: " + reviewersAddress).setFont(customFont).setFontSize(12).setMarginBottom(20));
        document.add(new Paragraph("创建时间: " + createdAt).setFont(customFont).setFontSize(12).setMarginBottom(10));

        // 添加二维码标题
        Paragraph qrTitle = new Paragraph("作品二维码:")
                .setFont(customFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(qrTitle);

        // 添加二维码并居中
        Image qrCodeImage = new Image(ImageDataFactory.create(generateQRCode(qrContent)));
        qrCodeImage.setWidth(150);
        qrCodeImage.setHeight(150);

        Div qrContainer = new Div();
        qrContainer.setTextAlignment(TextAlignment.CENTER);
        qrContainer.add(qrCodeImage);

        document.add(qrContainer);

        // 关闭文档
        document.close();
        return outputStream.toByteArray();
    }

    private static byte[] generateQRCode(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        return outputStream.toByteArray();
    }
}
