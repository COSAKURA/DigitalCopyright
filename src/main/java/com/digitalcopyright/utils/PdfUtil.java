package com.digitalcopyright.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Sakura
 */
public class PdfUtil {

    public static byte[] generatePdfWithTemplate(String registrationId, String copyrightNumber, String workTitle,
                                                String workCategory, String ownerAddress,
                                                 String reviewAddress, String createdAt, String qrContent)
            throws IOException, WriterException {

        // 输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);

        // 设置页面为横向 (Landscape)
            // A4 横向
        PageSize pageSize = PageSize.A4.rotate();
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(pageSize);

        Document document = new Document(pdf);

        // 加载证书模板图片
            // 替换为您的模板路径
        String imagePath = "src/main/resources/templates/log.png";
        ImageData templateImageData = ImageDataFactory.create(imagePath);
        Image templateImage = new Image(templateImageData);

        // 设置背景图片全屏显示
            // 图片固定在页面左下角
        templateImage.setFixedPosition(17, 0);
        // 缩放图片填满整个页面
        templateImage.scaleToFit(pageSize.getWidth(), pageSize.getHeight());

        // 添加模板图片作为背景
        document.add(templateImage);

        // 加载字体
        PdfFont customFont = PdfFontFactory.createFont("src/main/resources/fonts/HYSongYunLangHeiW-1.ttf",
                com.itextpdf.io.font.PdfEncodings.IDENTITY_H);

        // 在指定位置添加动态数据（根据模板调整位置）
        document.add(new Paragraph(copyrightNumber)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(230, 399, 400));
        document.add(new Paragraph( workCategory)  // 作品编号
                .setFont(customFont)
                .setFontSize(18)
                .setFixedPosition(504, 398, 400));
        document.add(new Paragraph( workTitle)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(270, 350, 400));
        document.add(new Paragraph( createdAt)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(510, 350, 400));
        document.add(new Paragraph(registrationId)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(230, 315, 400));
        document.add(new Paragraph( registrationId)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(510, 315, 400));
        document.add(new Paragraph(registrationId)
                .setFont(customFont)
                .setFontSize(12)
                .setFixedPosition(280, 215, 400));
        document.add(new Paragraph( ownerAddress)
                .setFont(customFont)
                .setFontSize(8.5F)
                .setFixedPosition(465, 219, 400));
        document.add(new Paragraph( reviewAddress)
                .setFont(customFont)
                .setFontSize(17)
                .setFixedPosition(220, 112, 400));

        // 添加二维码
        Image qrCodeImage = new Image(ImageDataFactory.create(generateQRCode(qrContent)));
        // 设置二维码位置（横向页面的右下角）
        qrCodeImage.setFixedPosition(600, 80);
        // 设置二维码大小
        qrCodeImage.scaleToFit(100, 100);
        document.add(qrCodeImage);

        // 关闭文档
        document.close();
        return outputStream.toByteArray();
    }

    // 生成二维码图片
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
