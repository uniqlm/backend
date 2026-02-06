package com.uniqlm.controller.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Credentials for authentication")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Schema(description = "Username of the account", example = "jane.doe")
    private String username;
    @Schema(description = "Password of the account", example = "P@ssw0rd123")
    private String password;
}
