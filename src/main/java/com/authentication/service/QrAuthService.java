package com.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QrAuthService {

    @Autowired
    private EmailService emailService;

    // Defines the lifecycle stages of a QR login attempt
    public enum QrStatus {
        WAITING_FOR_SCAN,           // 1. displayed on screen
        WAITING_FOR_EMAIL_APPROVAL, // 2. scanned by phone, email sent
        APPROVED,                   // 3. email link clicked
        EXPIRED                     // 4. timeout
    }

    // Inner class to hold session data in memory
    private static class QrSessionData {
        String token;
        String email;
        QrStatus status;
        LocalDateTime expiryTime;

        public QrSessionData(String token, String email) {
            this.token = token;
            this.email = email;
            this.status = QrStatus.WAITING_FOR_SCAN;
            this.expiryTime = LocalDateTime.now().plusMinutes(2); // Token valid for 2 minutes
        }
    }

    // Maps: Desktop Session ID -> Session Data
    private final Map<String, QrSessionData> qrSessions = new ConcurrentHashMap<>();

    // 1. INITIALIZE: Called when Desktop loads the QR page
    public String initQrSession(String sessionId, String email) {
        String token = UUID.randomUUID().toString();
        qrSessions.put(sessionId, new QrSessionData(token, email));
        return token;
    }

    // 2. MOBILE SCAN: Called when phone hits /scan-qr
    public boolean handleMobileScan(String token, String baseUrl) {
        String sessionId = findSessionIdByToken(token);
        
        if (sessionId == null) return false;
        
        QrSessionData data = qrSessions.get(sessionId);
        
        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            data.status = QrStatus.EXPIRED;
            return false;
        }

        // Only proceed if we are in the initial state
        if (data.status == QrStatus.WAITING_FOR_SCAN) {
            data.status = QrStatus.WAITING_FOR_EMAIL_APPROVAL;
            
            // Generate the approval link pointing back to the server
            String approvalLink = baseUrl + "/approve-qr-login?token=" + token;
            
            // Trigger the email
            emailService.sendQrApprovalEmail(data.email, approvalLink);
            return true;
        }
        
        // Return true if already scanned to avoid error messages on double-scan
        return data.status == QrStatus.WAITING_FOR_EMAIL_APPROVAL;
    }

    // 3. EMAIL APPROVAL: Called when user clicks the link in their inbox
    public boolean approveLogin(String token) {
        String sessionId = findSessionIdByToken(token);
        if (sessionId == null) return false;

        QrSessionData data = qrSessions.get(sessionId);

        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            data.status = QrStatus.EXPIRED;
            return false;
        }

        // Only approve if we were waiting for it
        if (data.status == QrStatus.WAITING_FOR_EMAIL_APPROVAL) {
            data.status = QrStatus.APPROVED;
            return true;
        }
        return false;
    }

    // 4. STATUS CHECK: Polled by the Desktop Browser
    public QrStatus checkStatus(String sessionId) {
        if (!qrSessions.containsKey(sessionId)) return QrStatus.EXPIRED;
        
        QrSessionData data = qrSessions.get(sessionId);
        
        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            qrSessions.remove(sessionId);
            return QrStatus.EXPIRED;
        }
        
        if (data.status == QrStatus.APPROVED) {
            qrSessions.remove(sessionId); // Cleanup after success
            return QrStatus.APPROVED;
        }
        
        return data.status;
    }

    // Helper: Reverse lookup to find Session ID using the Token
    private String findSessionIdByToken(String token) {
        return qrSessions.entrySet().stream()
                .filter(entry -> entry.getValue().token.equals(token))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
