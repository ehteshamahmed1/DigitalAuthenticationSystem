package com.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.authentication.dto.RegisterRequest;
import com.authentication.model.AuthenticationType;
import com.authentication.model.User;
import com.authentication.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {

        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            return "login";
        }

        if (service.emailExists(request.getEmail())) {
            model.addAttribute("error", "Email already exists.");
            return "login";
        }

        if (service.mobileExists(request.getMobile())) {
            model.addAttribute("error", "Mobile number already exists.");
            return "login";
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        user.setMobile(request.getMobile() != null ? request.getMobile().trim() : null);

        // ENCRYPT PASSWORD
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        AuthenticationType authType = request.getAuthenticationType();
        if (authType == null) {
            authType = AuthenticationType.EMAIL_OTP;
        }
        user.setAuthenticationType(authType);

        service.register(user);

        System.out.println(">>> User registered: " + user.getEmail() + " with AuthType: " + authType);

        return "redirect:/login?registered=true";
    }
}