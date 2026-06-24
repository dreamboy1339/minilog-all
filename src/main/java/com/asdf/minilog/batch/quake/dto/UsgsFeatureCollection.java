package com.asdf.minilog.batch.quake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** USGS GeoJSON 응답의 최상위 객체(FeatureCollection). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsFeatureCollection(List<UsgsFeature> features) {}
