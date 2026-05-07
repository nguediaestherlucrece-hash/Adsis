package com.ISI.Appel_classe.Gestion_des_appels_etudiant.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeEtudiantService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Génère un QR code en base64 à partir d'un contenu
     * @param content Le contenu à encoder dans le QR code
     * @return Le QR code en format base64
     */
    public String generateQRCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_WIDTH,
                    QR_CODE_HEIGHT,
                    hints
            );

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    /**
     * Génère un code PIN aléatoire de 6 chiffres
     * @return Un code PIN de 6 chiffres
     */
    public String generatePINCode() {
        int pin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(pin);
    }

    /**
     * Génère un token QR unique pour un étudiant et une séance
     * Format: ETUDIANT:{etudiantId}:{seanceId}:{timestamp}:{hash}
     * @param etudiantId ID de l'étudiant
     * @param seanceId ID de la séance
     * @return Le token QR
     */
    public String generateQRToken(Long etudiantId, Long seanceId) {
        long timestamp = System.currentTimeMillis();
        long hash = (etudiantId ^ seanceId ^ timestamp) & 0x7FFFFFFF;
        return String.format("ETUDIANT:%d:%d:%d:%d", etudiantId, seanceId, timestamp, hash);
    }
}
