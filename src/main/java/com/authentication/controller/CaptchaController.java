package com.authentication.controller;

import com.authentication.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public String showCaptchaPage() {
        return "captcha";
    }

    @GetMapping(value = "/captcha-image", produces = MediaType.IMAGE_PNG_VALUE)
    public void getCaptchaImage(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String captchaText = captchaService.generateCaptchaText();
        session.setAttribute("CAPTCHA_TEXT", captchaText);

        byte[] image = captchaService.generateCaptchaImage(captchaText);
        response.getOutputStream().write(image);
        response.getOutputStream().flush();
    }

    @PostMapping("/verifyCaptcha")
    public String verifyCaptcha(@RequestParam("captcha") String submittedCaptcha,
                                HttpServletRequest request,
                                Model model) {

        HttpSession session = request.getSession(false);
        String expectedCaptcha = (session != null) ? (String) session.getAttribute("CAPTCHA_TEXT") : null;

        if (expectedCaptcha != null && expectedCaptcha.equalsIgnoreCase(submittedCaptcha.trim())) {
            if (session != null) {
                session.removeAttribute("CAPTCHA_TEXT");
            }
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "❌ Invalid or expired CAPTCHA code!");
            return "captcha";
        }
    }
}