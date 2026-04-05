package com.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Schema(example = "oleg_smirnov")
    private String username;

    @NotBlank
    @Schema(example = "secret12")
    private String password;
}
