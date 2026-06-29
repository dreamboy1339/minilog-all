package com.asdf.minilog.dto;

import org.springframework.core.io.Resource;

/**
 * 파일 다운로드 시 서비스 계층이 컨트롤러로 전달하는 내부 캐리어.
 *
 * <p>실제 바이트({@link Resource})와 함께 응답 헤더 구성에 필요한 원본 파일명, MIME 타입, 크기를 묶어 전달한다. 바이트의 출처(DB / 서버 폴더)와
 * 무관하게 동일한 형태로 컨트롤러에서 응답을 만들 수 있게 한다.
 *
 * @param resource 다운로드할 파일 바이트
 * @param fileName 응답에 사용할 원본 파일명
 * @param contentType MIME 타입(없으면 null)
 * @param size 파일 크기(바이트)
 */
public record FileDownloadDto(Resource resource, String fileName, String contentType, long size) {}
