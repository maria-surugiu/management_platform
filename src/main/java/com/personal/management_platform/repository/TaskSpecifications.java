package com.personal.management_platform.repository;

import com.personal.management_platform.model.Task;
import com.personal.management_platform.model.TaskPriority;
import com.personal.management_platform.model.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecifications {
    public static Specification<Task> filterTasks(UUID projectId, String status, String priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("project").get("id"), projectId));


            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(status)));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), TaskPriority.valueOf(priority)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
