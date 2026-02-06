package com.uniqlm.controller;

import com.uniqlm.config.JwtUtil;
import com.uniqlm.controller.entity.AuthRequest;
import com.uniqlm.controller.entity.AuthResponse;
import com.uniqlm.persistence.entity.MstUser;
import com.uniqlm.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// Allow all origins at the controller level as a secondary CORS layer
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Tag(name = "Authentication", description = "Authentication and profile endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Login", description = "Authenticate with username and password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "403", description = "Email not verified", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // 1. Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Check verification status before issuing token
        MstUser user = userDetailsService.getProfile(request.getUsername());
        if (user != null && Boolean.FALSE.equals(user.getIsVerified())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email address before logging in.");
        }

        final var userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @Operation(summary = "Register", description = "Create a new user account.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody MstUser user) {
        return ResponseEntity.ok(userDetailsService.register(user));
    }

    @Operation(summary = "Get profile", description = "Fetch authenticated user profile.")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userDetailsService.getProfile(authentication.getName()));
    }

    @Operation(summary = "Update profile", description = "Update authenticated user profile.")
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody MstUser user) {
        return ResponseEntity.ok(userDetailsService.updateProfile(user));
    }

    @Operation(summary = "Verify Email")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        MstUser verifiedUser = userDetailsService.verifyEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(verifiedUser.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @Operation(summary = "Resend Verification Email")
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        userDetailsService.resendVerification(email);
        return ResponseEntity.ok("Verification email sent");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        userDetailsService.createResetToken(email);
        return ResponseEntity.ok("Reset link sent to email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        userDetailsService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}