package com.authentication.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OTPService {

    @Autowired
    private EmailService emailService;

    // In-memory store for generated OTPs (Email -> OTP)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        otpStorage.put(email, otp);
        emailService.sendOTP(email, otp);
        return otp;
    }

    public boolean verifyOTP(String email, String inputOtp) {
        if (email == null || inputOtp == null) {
            return false;
        }
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(inputOtp.trim())) {
            otpStorage.remove(email); // Invalidate after successful verification
            return true;
        }
        return false;
    }
}