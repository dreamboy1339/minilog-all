package com.asdf.minilog.repository;

import com.asdf.minilog.entity.Task;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findAllByDeviceIdIn(@Param("deviceIds") Collection<Long> deviceIds);

  @Query(
      value = "SELECT t FROM Task t JOIN FETCH t.device",
      countQuery = "SELECT COUNT(t) FROM Task t")
  Page<Task> findAllWithDevice(Pageable pageable);
}
