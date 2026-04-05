package com.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Schema(example = "oleg_smirnov")
    private String username;

    @NotBlank
    @Email
    @Schema(example = "oleg@primer.local")
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(example = "secret12")
    private String password;
}
