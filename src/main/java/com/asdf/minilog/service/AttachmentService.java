package com.asdf.minilog.service;

import com.asdf.minilog.dto.AttachmentResponseDto;
import com.asdf.minilog.dto.FileDownloadDto;
import com.asdf.minilog.entity.main.Attachment;
import com.asdf.minilog.exception.AttachmentNotFoundException;
import com.asdf.minilog.repository.main.AttachmentRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일(Attachment) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>업로드된 파일을 DB(attachments 테이블)와 서버 폴더 양쪽에 저장하고, 두 저장소 각각에서 파일을 읽어 다운로드용으로 제공한다. main_db에 쓰므로 기본
 * 트랜잭션 매니저를 사용한다.
 */
@Service
@Transactional
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;
  private final FileStorageService fileStorageService;

  @Autowired
  public AttachmentService(
      AttachmentRepository attachmentRepository, FileStorageService fileStorageService) {
    this.attachmentRepository = attachmentRepository;
    this.fileStorageService = fileStorageService;
  }

  /**
   * 업로드된 파일을 DB(attachments 테이블)와 서버 폴더 양쪽에 저장한다.
   *
   * @param file 업로드된 멀티파트 파일
   * @return 저장된 첨부파일 메타데이터
   * @throws IllegalArgumentException 파일이 비어 있는 경우
   */
  public AttachmentResponseDto upload(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Uploaded file is empty");
    }

    String originalFileName =
        StringUtils.cleanPath(
            file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());

    byte[] data;
    try {
      data = file.getBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read uploaded file: " + originalFileName, e);
    }

    // 1) 서버의 특정 폴더(attachment)에 저장
    String storedFileName = fileStorageService.store(data, originalFileName);

    // 2) DB(attachments 테이블)에 메타데이터 + 파일 바이트 저장
    Attachment attachment =
        Attachment.builder()
            .originalFileName(originalFileName)
            .storedFileName(storedFileName)
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .data(data)
            .build();

    Attachment saved = attachmentRepository.save(attachment);
    return EntityDtoMapper.toDto(saved);
  }

  /**
   * DB(attachments 테이블)에 저장된 파일 바이트를 다운로드용으로 읽어온다.
   *
   * @param attachmentId 첨부파일 ID
   * @return 다운로드 캐리어(파일 바이트 + 메타데이터)
   * @throws AttachmentNotFoundException 첨부파일을 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public FileDownloadDto downloadFromDatabase(Long attachmentId) {
    Attachment attachment = getAttachment(attachmentId);
    Resource resource = new ByteArrayResource(attachment.getData());
    return toDownloadDto(attachment, resource);
  }

  /**
   * 서버 폴더(attachment)에 저장된 파일을 다운로드용으로 읽어온다.
   *
   * @param attachmentId 첨부파일 ID
   * @return 다운로드 캐리어(파일 바이트 + 메타데이터)
   * @throws AttachmentNotFoundException 첨부파일을 찾을 수 없거나 디스크에 파일이 없는 경우
   */
  @Transactional(readOnly = true)
  public FileDownloadDto downloadFromDisk(Long attachmentId) {
    Attachment attachment = getAttachment(attachmentId);
    Resource resource = fileStorageService.loadAsResource(attachment.getStoredFileName());
    return toDownloadDto(attachment, resource);
  }

  private Attachment getAttachment(Long attachmentId) {
    return attachmentRepository
        .findById(attachmentId)
        .orElseThrow(
            () ->
                new AttachmentNotFoundException(
                    String.format("Attachment with id %d not found", attachmentId)));
  }

  private FileDownloadDto toDownloadDto(Attachment attachment, Resource resource) {
    return new FileDownloadDto(
        resource,
        attachment.getOriginalFileName(),
        attachment.getContentType(),
        attachment.getFileSize());
  }
}
