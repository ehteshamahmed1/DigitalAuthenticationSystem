package com.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Inject the sender email from application.properties to prevent spam blocking
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Sends a standard 6-digit OTP for Email Authentication.
     */
    public void sendOTP(String receiver, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(receiver);
            message.setSubject("OTP Verification - Digital Authentication System");
            message.setText("Your verification OTP code is: " + otp);

            mailSender.send(message);
            System.out.println(">>> OTP Email Sent Successfully to " + receiver);
        } catch (Exception e) {
            System.err.println(">>> Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a security alert link for QR Code 2-Step Verification.
     */
    public void sendQrApprovalEmail(String receiverEmail, String approvalLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(receiverEmail);
            message.setSubject("Security Check: Approve QR Login");

            String emailBody = """
                A new login attempt was initiated via QR Code Scan.
                
                If this was you, please click the link below to approve the login:
                %s
                
                If you did not scan a QR code just now, please ignore this email. Your account remains secure.
                """.formatted(approvalLink);

            message.setText(emailBody);

            mailSender.send(message);
            System.out.println(">>> QR Approval Email sent to: " + receiverEmail);
        } catch (Exception e) {
            System.err.println(">>> Failed to send QR Approval Email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
