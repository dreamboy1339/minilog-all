package com.asdf.minilog.batch.quake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** 진앙 좌표. {@code coordinates}는 [경도, 위도, 깊이] 순서다(GeoJSON 규약). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsGeometry(List<Double> coordinates) {}
