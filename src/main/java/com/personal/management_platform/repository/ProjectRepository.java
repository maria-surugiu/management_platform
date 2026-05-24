package com.personal.management_platform.repository;

import com.personal.management_platform.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByIsActiveTrue();

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m WHERE p.isActive = true AND (p.owner.id = :userId OR m.id = :userId)")
    List<Project> findActiveProjectsForUser(@Param("userId") UUID userId);
}