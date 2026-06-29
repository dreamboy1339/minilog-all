package com.asdf.minilog.controller;

import com.asdf.minilog.dto.AttachmentResponseDto;
import com.asdf.minilog.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 REST 컨트롤러.
 *
 * <p>{@code POST /api/v2/files/upload}로 멀티파트 파일을 받아, DB(attachments 테이블)와 서버의 특정 폴더(attachment) 양쪽에
 * 저장한다. 응답으로는 다운로드에 사용할 첨부파일 ID 등 메타데이터를 반환한다.
 */
@RestController
@RequestMapping("/api/v2/files")
public class UploadFileController {

  private final AttachmentService attachmentService;

  @Autowired
  public UploadFileController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  /**
   * 파일을 업로드하여 DB와 서버 폴더 양쪽에 저장한다. (POST /api/v2/files/upload)
   *
   * @param file 업로드할 멀티파트 파일
   * @return 저장된 첨부파일 메타데이터
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload a file (stored in both the database and the server folder)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
    @ApiResponse(responseCode = "400", description = "Empty or invalid file")
  })
  public ResponseEntity<AttachmentResponseDto> upload(@RequestParam("file") MultipartFile file) {
    AttachmentResponseDto response = attachmentService.upload(file);
    return ResponseEntity.ok(response);
  }
}
