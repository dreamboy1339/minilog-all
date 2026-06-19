package com.asdf.minilog.repository;

import com.asdf.minilog.entity.TaskReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 작업 보고서(TaskReport) 엔티티를 관리하는 리포지토리. */
@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, Long> {

  /**
   * 특정 작업에 대한 보고서가 이미 존재하는지 확인한다.
   *
   * @param taskId 작업 ID
   * @return 보고서가 존재하면 true
   */
  boolean existsByTaskId(Long taskId);

  /**
   * 특정 작업에 연결된 보고서를 조회한다.
   *
   * @param taskId 작업 ID
   * @return 존재하면 작업 보고서, 없으면 빈 Optional
   */
  Optional<TaskReport> findByTaskId(Long taskId);
}
