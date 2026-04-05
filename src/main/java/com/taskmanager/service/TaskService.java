package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TaskSpecifications;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse create(TaskRequest request, UserPrincipal principal) {
        User author = userRepository
                .findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        User assignee = findAssignee(request.getAssigneeId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .author(author)
                .assignee(assignee)
                .build();

        Task saved = taskRepository.save(task);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request, UserPrincipal principal) {

        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));

        assertCanModify(principal, task); // to check if user has access to action

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setAssignee(findAssignee(request.getAssigneeId()));

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id, UserPrincipal principal) {
        Task task = taskRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));

        assertCanModify(principal, task); // to check if user has access to action

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(TaskStatus status, Long assigneeId, Long authorId) {

        Specification<Task> spec = Specification.where(TaskSpecifications.withUsersFetched())
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasAssigneeId(assigneeId))
                .and(TaskSpecifications.hasAuthorId(authorId));

        return taskRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User findAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository
                .findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("Assignee not found: " + assigneeId));
    }

    private void assertCanModify(UserPrincipal principal, Task task) {
        boolean admin = principal.getRole() == Role.ADMIN;
        boolean author = task.getAuthor().getId().equals(principal.getId());

        if (!admin && !author) {
            throw new AccessDeniedException("Only task author or admin can modify this task");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .authorId(task.getAuthor().getId())
                .authorUsername(task.getAuthor().getUsername())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeUsername(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
