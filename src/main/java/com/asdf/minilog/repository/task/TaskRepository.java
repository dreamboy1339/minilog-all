package com.asdf.minilog.repository.task;

import com.asdf.minilog.entity.task.Task;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 작업(Task) 엔티티를 관리하는 리포지토리. */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  /**
   * 주어진 장비 ID 목록에 속한 모든 작업을 조회한다.
   *
   * @param deviceIds 장비 ID 컬렉션
   * @return 해당 장비들의 작업 목록
   */
  List<Task> findAllByDeviceIdIn(@Param("deviceIds") Collection<Long> deviceIds);

  /**
   * 작업을 장비와 함께 페이징하여 조회한다. JOIN FETCH로 연관 장비를 한 번에 가져와 N+1 문제를 방지하며, countQuery를 별도로 지정한다.
   *
   * @param pageable 페이징 정보
   * @return 장비가 함께 로딩된 작업 페이지
   */
  @Query(
      value = "SELECT t FROM Task t JOIN FETCH t.device",
      countQuery = "SELECT COUNT(t) FROM Task t")
  Page<Task> findAllWithDevice(Pageable pageable);
}
