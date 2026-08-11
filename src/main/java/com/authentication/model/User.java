package com.authentication.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String mobile;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType;

    // --- Added Magic Link Tracking Fields ---
    @Transient
    private String magicLinkToken;

    @Transient
    private LocalDateTime magicLinkExpiry;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public void setPassword(String password) { this.password = password; }

    public AuthenticationType getAuthenticationType() { return authenticationType; }
    public void setAuthenticationType(AuthenticationType authenticationType) { this.authenticationType = authenticationType; }

    // --- Added Magic Link Getters & Setters ---
    public String getMagicLinkToken() { return magicLinkToken; }
    public void setMagicLinkToken(String magicLinkToken) { this.magicLinkToken = magicLinkToken; }

    public LocalDateTime getMagicLinkExpiry() { return magicLinkExpiry; }
    public void setMagicLinkExpiry(LocalDateTime magicLinkExpiry) { this.magicLinkExpiry = magicLinkExpiry; }

    public String getRedirectUrl() {
        if (authenticationType == null) return "/dashboard";
        switch (authenticationType) {
            case EMAIL_OTP: return "/emailotp";
            case MOBILE_OTP: return "/mobileotp";
            case PUZZLE: return "/puzzle";
            case MAGIC_LINK: return "/magiclink";
            case QR_CODE: return "/qrauthentication";
            case CAPTCHA: return "/captcha";
            default: return "/dashboard";
        }
    }

    // --- UserDetails Methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
