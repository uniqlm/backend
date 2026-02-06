package com.uniqlm.config;

import com.uniqlm.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.oauth2.redirect-path}")
    private String redirectPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Use your service to find or create the user
        UserDetails userDetails = userDetailsService.processOAuthPostLogin(email, name);

        // Generate the JWT
        String token = jwtUtil.generateToken(userDetails);

        // Redirect to Frontend: https://uniqlm.com/home?token=...
        String targetUrl = frontendUrl + redirectPath + "?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
