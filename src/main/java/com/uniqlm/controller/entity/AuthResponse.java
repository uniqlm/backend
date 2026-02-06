package com.uniqlm.controller.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "JWT response after successful authentication")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    @Schema(description = "Bearer token to access protected endpoints", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
