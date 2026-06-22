package com.asdf.minilog.batch.quake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 단일 지진 이벤트(GeoJSON Feature). {@code id}는 USGS 이벤트 id다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsFeature(String id, UsgsProperties properties, UsgsGeometry geometry) {}
