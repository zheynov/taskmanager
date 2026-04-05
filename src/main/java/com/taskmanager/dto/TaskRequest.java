package com.taskmanager.dto;

import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank
    @Schema(example = "Сверка документов")
    private String title;

    @Schema(example = "До конца недели")
    private String description;

    @NotNull
    @Schema(example = "TODO")
    private TaskStatus status;

    @NotNull
    @Schema(example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "User id of assignee; omit or null for unassigned", example = "2")
    private Long assigneeId;
}
