package com.authentication.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.imageio.ImageIO;

@Service
public class CaptchaService {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CAPTCHA_LENGTH = 6;
    private static final int WIDTH = 220;
    private static final int HEIGHT = 80;

    private final SecureRandom random = new SecureRandom();

    public String generateCaptchaText() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public byte[] generateCaptchaImage(String captchaText) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark modern background
        g2d.setColor(new Color(30, 32, 40));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Noise lines
        for (int i = 0; i < 8; i++) {
            g2d.setColor(randomColor(80, 180));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Noise dots
        for (int i = 0; i < 60; i++) {
            g2d.setColor(randomColor(100, 200));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g2d.fillOval(x, y, 2, 2);
        }

        // Render characters with rotation and noise
        Font baseFont = new Font("Arial", Font.BOLD, 38);
        int charWidth = WIDTH / (captchaText.length() + 1);

        for (int i = 0; i < captchaText.length(); i++) {
            char c = captchaText.charAt(i);

            AffineTransform original = g2d.getTransform();
            double angle = Math.toRadians(random.nextInt(40) - 20);
            int x = charWidth * (i + 1) - 12;
            int y = HEIGHT / 2 + 12 + random.nextInt(10) - 5;

            g2d.rotate(angle, x, y);
            g2d.setFont(baseFont.deriveFont(34f + random.nextInt(8)));
            g2d.setColor(new Color(120 + random.nextInt(120), 180 + random.nextInt(75), 230));
            g2d.drawString(String.valueOf(c), x, y);
            g2d.setTransform(original);
        }

        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate captcha image", e);
        }
    }

    private Color randomColor(int min, int max) {
        int range = max - min;
        int r = min + random.nextInt(range);
        int g = min + random.nextInt(range);
        int b = min + random.nextInt(range);
        return new Color(r, g, b);
    }
}