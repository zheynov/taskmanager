package com.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.UserPrincipal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void create_persistsAndReturnsResponse() {

        User author = User.builder()
                .id(1L)
                .username("anna_zaytseva")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(50L);
            t.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            t.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
            return t;
        });

        TaskRequest request = new TaskRequest();
        request.setTitle("New");
        request.setDescription("Desc");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.MEDIUM);
        request.setAssigneeId(null);

        UserPrincipal principal = new UserPrincipal(1L, "anna_zaytseva", "x", Role.USER);

        TaskResponse response = taskService.create(request, principal);

        assertEquals(50L, response.getId());
        assertEquals("New", response.getTitle());
        assertEquals(TaskStatus.TODO, response.getStatus());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertEquals(author, captor.getValue().getAuthor());
    }

    @Test
    void update_throwsWhenNeitherAuthorNorAdmin() {

        User author = User.builder()
                .id(1L)
                .username("kirill_voronov")
                .build();

        Task task = Task.builder()
                .id(100L)
                .author(author)
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskRequest request = new TaskRequest();
        request.setTitle("X");
        request.setDescription("Y");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);
        request.setAssigneeId(null);

        UserPrincipal other = new UserPrincipal(2L, "pavel_sergeev", "x", Role.USER);

        assertThrows(AccessDeniedException.class, () -> taskService.update(100L, request, other));
    }

    @Test
    void update_allowedForAuthor() {

        User author = User.builder()
                .id(1L)
                .username("kirill_voronov")
                .build();

        Task task = Task.builder()
                .id(100L)
                .title("Old")
                .description("d")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .author(author)
                .assignee(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskRequest request = new TaskRequest();
        request.setTitle("New title");
        request.setDescription("New desc");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setAssigneeId(null);

        UserPrincipal authorPrincipal = new UserPrincipal(1L, "kirill_voronov", "x", Role.USER);
        TaskResponse response = taskService.update(100L, request, authorPrincipal);

        assertEquals("New title", response.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void delete_throwsWhenNeitherAuthorNorAdmin() {

        User author = User.builder()
                .id(1L)
                .username("kirill_voronov")
                .build();

        Task task = Task.builder()
                .id(100L)
                .author(author)
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        UserPrincipal other = new UserPrincipal(2L, "pavel_sergeev", "x", Role.USER);

        assertThrows(AccessDeniedException.class, () -> taskService.delete(100L, other));
    }

    @Test
    void delete_allowedForAuthor() {

        User author = User.builder()
                .id(1L)
                .username("kirill_voronov")
                .build();

        Task task = Task.builder()
                .id(100L)
                .author(author)
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        UserPrincipal authorPrincipal = new UserPrincipal(1L, "kirill_voronov", "x", Role.USER);

        assertDoesNotThrow(() -> taskService.delete(100L, authorPrincipal));
        verify(taskRepository).delete(task);
    }
}
