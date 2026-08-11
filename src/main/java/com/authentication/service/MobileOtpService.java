package com.authentication.service;

import com.authentication.model.User;
import com.authentication.repository.UserRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MobileOtpService {

    @Value("${twilio.phone.number:${twilio.phone_number:+10000000000}}")
    private String twilioPhoneNumber;

    private final UserRepository userRepository;

    public MobileOtpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public String generateOtp(String mobileNumber) {
        String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        otpCache.put(mobileNumber, otp);
        return otp;
    }

    public void sendSms(String mobileNumber, String otp) {
        String formattedNumber = mobileNumber.trim();
        if (!formattedNumber.startsWith("+")) {
            formattedNumber = "+91" + formattedNumber;
        }

        String messageBody = "Your secure verification code is: " + otp;

        try {
            Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();
            System.out.println(">>> Mobile OTP sent via Twilio to: " + formattedNumber);
        } catch (Exception e) {
            System.err.println(">>> Twilio Delivery Error: " + e.getMessage());
            System.out.println(">>> Fallback Console OTP for " + formattedNumber + " is: " + otp);
        }
    }

    public void generateAndSendOtp(String mobileNumber) {
        String otp = generateOtp(mobileNumber);
        sendSms(mobileNumber, otp);
    }

    public User validateOtp(String mobileNumber, String userOtp) {
        if (!otpCache.containsKey(mobileNumber) || !otpCache.get(mobileNumber).equals(userOtp.trim())) {
            return null;
        }
        otpCache.remove(mobileNumber);

        return userRepository.findByMobile(mobileNumber).orElse(null);
    }
}