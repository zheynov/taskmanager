package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> {
            return status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) {
                return cb.conjunction();
            }
            // Subquery чтобы избежать микса JOIN с FETCH JOIN (в withUsersFetched) для фильтрации
            assert query != null;
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Task> subRoot = sub.from(Task.class);
            sub.select(cb.literal(1));
            sub.where(
                    cb.equal(subRoot.get("id"), root.get("id")),
                    cb.equal(subRoot.join("assignee", JoinType.INNER).get("id"), assigneeId));

            return cb.exists(sub);
        };
    }

    public static Specification<Task> withUsersFetched() {
        return (root, query, cb) -> {
            assert query != null;
            // чтобы избежать N+1
            root.fetch("author", JoinType.INNER);
            root.fetch("assignee", JoinType.LEFT);
            query.distinct(true);
            return cb.conjunction();
        };
    }

    public static Specification<Task> hasAuthorId(Long authorId) {
        return (root, query, cb) -> {
            if (authorId == null) {
                return cb.conjunction();
            }
            // Используем тот же трюк с Subquery, что и в hasAssigneeId, чтобы не было дубликатов
            assert query != null;
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Task> subRoot = sub.from(Task.class);
            sub.select(cb.literal(1));
            sub.where(
                    cb.equal(subRoot.get("id"), root.get("id")),
                    cb.equal(subRoot.join("author", JoinType.INNER).get("id"), authorId));
            return cb.exists(sub);
        };
    }
}
