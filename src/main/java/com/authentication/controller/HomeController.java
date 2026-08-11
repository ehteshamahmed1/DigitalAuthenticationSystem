package com.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.authentication.model.User;
import com.authentication.repository.UserRepository;
import com.authentication.service.MobileOtpService;
import com.authentication.service.OTPService;
import com.authentication.service.MagicLinkService;
import com.authentication.service.ImagePuzzleService;
import com.authentication.service.QrAuthService; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired private OTPService otpService;
    @Autowired private MobileOtpService mobileOtpService;
    @Autowired private MagicLinkService magicLinkService;
    @Autowired private ImagePuzzleService imagePuzzleService;
    @Autowired private QrAuthService qrAuthService;
    @Autowired private UserRepository userRepository;

    // --- GLOBAL NGROK DOMAIN (Used for Email Links) ---
    private final String PUBLIC_DOMAIN = " https://chase-coat-cover.ngrok-free.dev";

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        String nameToDisplay = "User";

        if (email == null) {
            User user = (User) session.getAttribute("currentUser");
            if (user != null) {
                email = user.getEmail();
                nameToDisplay = user.getFullName();
            }
        }

        if ((email == null || email.trim().isEmpty()) && authentication != null) {
            String loggedInIdentifier = authentication.getName(); 
            User user = userRepository.findByEmailOrMobile(loggedInIdentifier, loggedInIdentifier).orElse(null);
            if (user != null) {
                email = user.getEmail();
                nameToDisplay = user.getFullName();
                session.setAttribute("userEmail", email);
                session.setAttribute("userMobile", user.getMobile());
                session.setAttribute("currentUser", user);
            } else {
                email = loggedInIdentifier;
                nameToDisplay = loggedInIdentifier;
            }
        } else {
            User user = (User) session.getAttribute("currentUser");
            if (user == null && email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }
            if (user != null) {
                nameToDisplay = user.getFullName();
            } else if (email != null) {
                nameToDisplay = email;
            }
        }

        model.addAttribute("email", email);
        model.addAttribute("fullName", nameToDisplay);
        return "dashboard";
    }

    // --- OTHER AUTH METHODS ---
    @GetMapping("/emailotp")
    public String emailOtp(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        otpService.generateOTP(email);
        model.addAttribute("email", email);
        return "emailotp";
    }

    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, HttpSession session, Model model) {
        boolean verified = otpService.verifyOTP(email, otp);
        if (verified) {
            session.setAttribute("userEmail", email);
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                session.setAttribute("userMobile", user.getMobile());
                session.setAttribute("currentUser", user);
            }
            return "redirect:/dashboard";
        }
        model.addAttribute("email", email);
        model.addAttribute("error", "Invalid or Expired Email OTP!");
        return "emailotp";
    }

    @GetMapping("/resendOtp")
    public String resendOtp(@RequestParam String email, Model model) {
        otpService.generateOTP(email);
        model.addAttribute("email", email);
        model.addAttribute("success", "New Email OTP Sent Successfully.");
        return "emailotp";
    }

    @GetMapping("/mobileotp")
    public String mobileOtp(HttpSession session, Model model) {
        String mobile = (String) session.getAttribute("userMobile");
        if (mobile == null) {
            User user = (User) session.getAttribute("currentUser");
            if (user != null) mobile = user.getMobile();
        }
        if (mobile == null) return "redirect:/login";
        mobileOtpService.generateAndSendOtp(mobile);
        model.addAttribute("mobile", mobile);
        return "mobileotp";
    }

    @PostMapping("/verifyMobileOtp")
    public String verifyMobileOtp(@RequestParam String mobile, @RequestParam String otp, HttpSession session, Model model) {
        User user = mobileOtpService.validateOtp(mobile, otp);
        if (user != null) {
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userMobile", user.getMobile());
            session.setAttribute("currentUser", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("mobile", mobile);
        model.addAttribute("error", "The Mobile OTP provided is invalid or has expired.");
        return "mobileotp";
    }
    
    @GetMapping("/resendMobileOtp")
    public String resendMobileOtp(@RequestParam String mobile, Model model) {
        mobileOtpService.generateAndSendOtp(mobile);
        model.addAttribute("mobile", mobile);
        model.addAttribute("success", "New Mobile OTP Sent Successfully.");
        return "mobileotp";
    }

    @GetMapping("/magiclink")
    public String magicLink(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        magicLinkService.generateAndSendMagicLink(email);
        model.addAttribute("email", email);
        return "magiclink";
    }

    @GetMapping("/verifyMagicLink")
    public String verifyMagicLink(@RequestParam String token, HttpSession session, Model model) {
        User user = magicLinkService.validateMagicLink(token);
        if (user != null) {
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userMobile", user.getMobile());
            session.setAttribute("currentUser", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "The Magic Link is invalid, used, or has expired.");
        return "login";
    }

    @GetMapping("/puzzle")
    public String puzzle(HttpSession session, Model model) {
        if (session.getAttribute("userEmail") == null) return "redirect:/login";
        List<Integer> scrambledSequence = imagePuzzleService.generateScrambledGrid(session.getId());
        model.addAttribute("sequence", scrambledSequence);
        return "puzzle";
    }

    @PostMapping("/verifyPuzzle")
    public String verifyPuzzle(@RequestParam("puzzleSolution") String puzzleSolution, HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        boolean solved = imagePuzzleService.verifyPuzzleSolution(session.getId(), puzzleSolution);
        if (solved) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                session.setAttribute("userMobile", user.getMobile());
                session.setAttribute("currentUser", user);
            }
            return "redirect:/dashboard";
        }
        List<Integer> renewedSequence = imagePuzzleService.generateScrambledGrid(session.getId());
        model.addAttribute("sequence", renewedSequence);
        model.addAttribute("error", "❌ Incorrect arrangement! Please piece together the image correctly.");
        return "puzzle";
    }

    // ==========================================
    // 5. QR CODE AUTHENTICATION (NGROK INTEGRATED)
    // ==========================================

    @GetMapping("/qrauthentication")
    public String qrAuthentication(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        // Initialize session
        String qrToken = qrAuthService.initQrSession(session.getId(), email);
        
        model.addAttribute("qrToken", qrToken);
        return "qrauthentication";
    }

    // POLLING: Desktop browser hits this every 2 seconds
    @GetMapping("/qr-check-status")
    @ResponseBody
    public Map<String, String> checkQrStatus(HttpSession session) {
        QrAuthService.QrStatus status = qrAuthService.checkStatus(session.getId());
        
        if (status == QrAuthService.QrStatus.APPROVED) {
            // Login the user in the session
            String email = (String) session.getAttribute("userEmail");
            if (email != null) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    session.setAttribute("userMobile", user.getMobile());
                    session.setAttribute("currentUser", user);
                }
            }
        }
        return Map.of("status", status.name());
    }

    // MOBILE SCAN: Phone hits this. Triggers Email.
    @GetMapping("/scan-qr")
    public String scanQrUrl(@RequestParam("token") String token, Model model) {
        // FIX: We explicitly use the NGROK domain for the email link
        // This ensures the link sent to your inbox is clickable from anywhere
        String baseUrl = PUBLIC_DOMAIN; 

        boolean scanSuccess = qrAuthService.handleMobileScan(token, baseUrl);
        
        if (scanSuccess) {
            model.addAttribute("message", "✅ Scan Successful! We sent a verification link to your email. Please approve it to finish logging in.");
        } else {
            model.addAttribute("error", "❌ Scan Failed. Session expired or invalid.");
        }
        return "login"; 
    }

    // EMAIL APPROVAL: User clicks this in their inbox
    @GetMapping("/approve-qr-login")
    public String approveQrLogin(@RequestParam("token") String token, Model model) {
        boolean approved = qrAuthService.approveLogin(token);
        
        if (approved) {
            model.addAttribute("message", "✅ Login Approved! Check your desktop screen.");
        } else {
            model.addAttribute("error", "❌ Approval Failed. Link expired or invalid.");
        }
        return "login";
    }
}
