package com.asdf.minilog.repository.quake;

import com.asdf.minilog.entity.quake.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 지진(Earthquake) 엔티티를 관리하는 리포지토리. 자연키(USGS 이벤트 id)를 사용한다. */
@Repository
public interface EarthquakeRepository extends JpaRepository<Earthquake, String> {}
