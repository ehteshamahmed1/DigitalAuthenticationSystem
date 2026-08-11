package com.authentication.service;

import com.authentication.model.User;
import com.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MagicLinkService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // Memory Cache tracking structure: maps unique generated Token -> User Object data
    private final Map<String, TokenData> tokenCache = new ConcurrentHashMap<>();

    // Internal helper class to track individual item validation lifecycles
    private static class TokenData {
        final User user;
        final LocalDateTime expiry;

        TokenData(User user, LocalDateTime expiry) {
            this.user = user;
            this.expiry = expiry;
        }
    }

    public void generateAndSendMagicLink(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.out.println(">>> Magic Link aborted: User not found for email: " + email);
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

        // Store session reference validation flags directly inside memory state cache
        tokenCache.put(token, new TokenData(user, expiryTime));

        String magicLink = "http://localhost:8080/verifyMagicLink?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Your Secure Magic Access Link");
            message.setText("Click the secure link below to sign in automatically. This link expires in 10 minutes:\n\n" + magicLink);

            mailSender.send(message);
            System.out.println(">>> Magic Link dispatched successfully to: " + email);
        } catch (Exception e) {
            System.err.println(">>> Mail delivery failure: " + e.getMessage());
            System.out.println(">>> [FALLBACK] Magic Link URL is: " + magicLink);
        }
    }

    public User validateMagicLink(String token) {
        if (token == null || !tokenCache.containsKey(token)) {
            System.out.println(">>> Magic Link access denied: Token variant missing from map cache.");
            return null;
        }

        TokenData data = tokenCache.get(token);

        // Clean and consume single-use token allocation maps instantly from memory layout
        tokenCache.remove(token);

        if (data.expiry.isBefore(LocalDateTime.now())) {
            System.out.println(">>> Magic Link access denied: Memory cache token has expired.");
            return null;
        }

        System.out.println(">>> Magic Link verified successfully for user: " + data.user.getEmail());
        return data.user;
    }
}
