package com.asdf.minilog.repository;

import com.asdf.minilog.entity.TaskReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, Long> {

  boolean existsByTaskId(Long taskId);

  Optional<TaskReport> findByTaskId(Long taskId);
}
