package com.authentication.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.authentication.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;

            HttpSession session = request.getSession();
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userMobile", user.getMobile());
            session.setAttribute("currentUser", user);

            String targetUrl = user.getRedirectUrl();
            System.out.println(">>> LOGIN SUCCESS: Redirecting " + user.getEmail() + " to " + targetUrl);

            response.sendRedirect(request.getContextPath() + targetUrl);
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}