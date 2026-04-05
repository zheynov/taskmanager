package com.taskmanager.dto;

import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskResponse {
    Long id;
    String title;
    String description;
    TaskStatus status;
    TaskPriority priority;
    Long authorId;
    String authorUsername;
    Long assigneeId;
    String assigneeUsername;
    Instant createdAt;
    Instant updatedAt;
}
