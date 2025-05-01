package com.hospital.queue.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class QRCodeService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public String generateQRCodeId() {
        return UUID.randomUUID().toString();
    }

    public String generateQRCodeImage(String qrCodeId, int width, int height) throws WriterException, IOException {
        String qrContent = frontendUrl + "/join-queue/" + qrCodeId;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        
        return Base64.getEncoder().encodeToString(pngData);
    }
}
