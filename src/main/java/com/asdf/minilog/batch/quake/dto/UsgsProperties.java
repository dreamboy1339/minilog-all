package com.asdf.minilog.batch.quake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 지진 이벤트 속성. {@code time}은 발생 시각(epoch milliseconds), {@code magType}은 규모 산출 방식이다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsProperties(Double mag, String place, Long time, String url, String magType) {}
