package com.freightflow.modules.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlatformLoginRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 255) String password
) {
}

