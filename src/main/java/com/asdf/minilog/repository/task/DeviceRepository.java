package com.asdf.minilog.repository.task;

import com.asdf.minilog.entity.task.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 장비(Device) 엔티티를 관리하는 리포지토리. */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {}
