package dev.learning.virtualthreads.workload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface WorkItemRepository extends JpaRepository<WorkItem, UUID> {
}

