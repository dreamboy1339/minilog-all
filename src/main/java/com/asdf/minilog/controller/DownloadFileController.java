package com.asdf.minilog.controller;

import com.asdf.minilog.dto.FileDownloadDto;
import com.asdf.minilog.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파일 다운로드 REST 컨트롤러.
 *
 * <p>{@code /api/v2/files/download} 경로에서 같은 첨부파일을 두 저장소 각각에서 내려받는 API를 제공한다.
 *
 * <ul>
 *   <li>{@code GET /db/{attachmentId}} — DB(attachments 테이블)에 저장된 파일 바이트를 다운로드
 *   <li>{@code GET /disk/{attachmentId}} — 서버의 특정 폴더(attachment)에 저장된 파일을 다운로드
 * </ul>
 *
 * 두 API 모두 첨부파일 ID로 메타데이터(원본 파일명·타입·크기)를 조회하며, 파일 바이트의 출처만 다르다.
 */
@RestController
@RequestMapping("/api/v2/files/download")
public class DownloadFileController {

  private final AttachmentService attachmentService;

  @Autowired
  public DownloadFileController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  /**
   * DB에 저장된 파일을 다운로드한다. (GET /api/v2/files/download/db/{attachmentId})
   *
   * @param attachmentId 첨부파일 ID
   * @return 파일 바이트(첨부 다운로드 응답)
   */
  @GetMapping("/db/{attachmentId}")
  @Operation(summary = "Download a file stored in the database")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Attachment not found")
  })
  public ResponseEntity<Resource> downloadFromDatabase(@PathVariable Long attachmentId) {
    FileDownloadDto download = attachmentService.downloadFromDatabase(attachmentId);
    return toDownloadResponse(download);
  }

  /**
   * 서버 폴더에 저장된 파일을 다운로드한다. (GET /api/v2/files/download/disk/{attachmentId})
   *
   * @param attachmentId 첨부파일 ID
   * @return 파일 바이트(첨부 다운로드 응답)
   */
  @GetMapping("/disk/{attachmentId}")
  @Operation(summary = "Download a file stored in the server folder")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Attachment not found")
  })
  public ResponseEntity<Resource> downloadFromDisk(@PathVariable Long attachmentId) {
    FileDownloadDto download = attachmentService.downloadFromDisk(attachmentId);
    return toDownloadResponse(download);
  }

  /** 다운로드 캐리어를 첨부파일 다운로드용 {@link ResponseEntity}로 변환한다. */
  private ResponseEntity<Resource> toDownloadResponse(FileDownloadDto download) {
    MediaType mediaType =
        download.contentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM
            : MediaType.parseMediaType(download.contentType());
    ContentDisposition contentDisposition =
        ContentDisposition.attachment()
            .filename(download.fileName(), StandardCharsets.UTF_8)
            .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .contentType(mediaType)
        .contentLength(download.size())
        .body(download.resource());
  }
}
