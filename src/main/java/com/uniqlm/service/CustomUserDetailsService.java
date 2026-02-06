package com.uniqlm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniqlm.persistence.MstUserDaoService;
import com.uniqlm.persistence.entity.MstUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MstUserDaoService mstUserDaoService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MstUser user = mstUserDaoService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        // Strict Mode: Reject login if email not verified
        if (user.getIsVerified() != null && !user.getIsVerified()) {
            throw new RuntimeException("Email not verified. Please check your inbox.");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    public MstUser register(MstUser user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setIsVerified(false);

            // Generate verification token
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(java.time.LocalDateTime.now().plusHours(24));

            // The exception is usually triggered here during the flush/save
            MstUser savedUser = mstUserDaoService.saveUser(user);

            // Send verification email
            mailService.sendVerificationEmail(savedUser.getEmail(), token);

            return savedUser;

        } catch (DataIntegrityViolationException e) {
            // Log the error for debugging
            System.err.println("Registration failed: Email or username already exists. " + e.getMessage());

            // Throw a 409 Conflict or 400 Bad Request back to the client
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A user with this email already exists."
            );
        } catch (Exception e) {
            // Catch-all for other issues (like Mail server being down)
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."
            );
        }
    }

    public UserDetails processOAuthPostLogin(String email, String name) {
        MstUser user = mstUserDaoService.getByUsername(email);

        if (user == null) {
            log.info("BELUM ADA");
            user = new MstUser();
            user.setUsername(email);
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID()));
            user.setIsVerified(true); // Google OAuth users are auto-verified
            mstUserDaoService.saveUser(user);
        }else {
            log.info("SUDAH ADA");
            try {
                log.info(objectMapper.writeValueAsString(user));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return loadUserByUsername(email);
    }

    public MstUser updateProfile(MstUser userUpdate) {
        MstUser existing = mstUserDaoService.getByUsername(userUpdate.getUsername());
        if (existing == null) throw new RuntimeException("User not found");

        existing.setName(userUpdate.getName());
        existing.setEmail(userUpdate.getEmail());
        existing.setPhone1(userUpdate.getPhone1());
        existing.setPhone2(userUpdate.getPhone2());

        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }

        return mstUserDaoService.saveUser(existing);
    }

    public MstUser getProfile(String username) {
        return mstUserDaoService.getByUsername(username);
    }

    public void createResetToken(String email) {
        MstUser user = mstUserDaoService.getByUsername(email); // Assuming email is username
        if (user == null) throw new RuntimeException("User not found");

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        mstUserDaoService.saveUser(user);

        mailService.sendPasswordResetEmail(user.getEmail(), token);
        System.out.println("Token for " + email + ": " + user.getResetToken());
    }

    public void resetPassword(String token, String newPassword) {
        MstUser user = mstUserDaoService.getByResetToken(token);

        if (user == null || user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        mstUserDaoService.saveUser(user);
    }

    public MstUser verifyEmail(String token) {
        MstUser user = mstUserDaoService.getByVerificationToken(token);

        if (user == null || user.getVerificationTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired verification token");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        
        return mstUserDaoService.saveUser(user);
    }

    public void resendVerification(String email) {
        MstUser user = mstUserDaoService.getByUsername(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new RuntimeException("Email already verified");
        }

        // Generate new verification token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(java.time.LocalDateTime.now().plusHours(24));
        mstUserDaoService.saveUser(user);

        // Send email
        mailService.sendVerificationEmail(user.getEmail(), token);
        System.out.println("Verification email resent to " + email + " with token: " + token);
    }
}