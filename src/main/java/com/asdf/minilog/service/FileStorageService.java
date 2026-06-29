package com.asdf.minilog.service;

import com.asdf.minilog.exception.AttachmentNotFoundException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

/**
 * 서버의 특정 폴더(attachment)에 파일을 저장하고 읽어오는 서비스.
 *
 * <p>저장 경로는 {@code app.attachment.dir} 설정값으로 지정하며(기본값 {@code attachment}), 도커 환경에서는 해당 경로에 볼륨을
 * 마운트하여 사용한다. 파일명 충돌을 막기 위해 디스크에는 UUID 기반 이름으로 저장하고, 원본 파일명은 DB에서 관리한다.
 */
@Service
public class FileStorageService {

  /** 파일을 저장할 서버 폴더의 절대 경로. */
  private final Path storageDir;

  public FileStorageService(@Value("${app.attachment.dir:attachment}") String dir) {
    this.storageDir = Paths.get(dir).toAbsolutePath().normalize();
  }

  /** 애플리케이션 기동 시 저장 디렉터리가 없으면 생성한다. */
  @PostConstruct
  void initStorageDir() {
    try {
      Files.createDirectories(storageDir);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Could not create attachment storage directory: " + storageDir, e);
    }
  }

  /**
   * 파일 바이트를 서버 폴더에 저장하고, 저장에 사용한 파일명(UUID 기반)을 반환한다.
   *
   * @param data 저장할 파일 바이트
   * @param originalFileName 원본 파일명(확장자 추출용)
   * @return 디스크에 저장된 파일명
   */
  public String store(byte[] data, String originalFileName) {
    String storedFileName = UUID.randomUUID() + extractExtension(originalFileName);
    Path target = storageDir.resolve(storedFileName).normalize();
    // 경로 탈출(path traversal) 방지
    if (!target.startsWith(storageDir)) {
      throw new IllegalArgumentException("Invalid storage path: " + storedFileName);
    }
    try {
      Files.write(target, data);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not store file: " + originalFileName, e);
    }
    return storedFileName;
  }

  /**
   * 서버 폴더에 저장된 파일을 다운로드용 {@link Resource}로 읽어온다.
   *
   * @param storedFileName 디스크에 저장된 파일명
   * @return 파일 리소스
   * @throws AttachmentNotFoundException 디스크에서 파일을 찾을 수 없는 경우
   */
  public Resource loadAsResource(String storedFileName) {
    Path file = storageDir.resolve(storedFileName).normalize();
    if (!file.startsWith(storageDir)) {
      throw new IllegalArgumentException("Invalid storage path: " + storedFileName);
    }
    try {
      Resource resource = new UrlResource(file.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new AttachmentNotFoundException("File not found on disk: " + storedFileName);
      }
      return resource;
    } catch (MalformedURLException e) {
      throw new AttachmentNotFoundException("File not found on disk: " + storedFileName);
    }
  }

  /** 원본 파일명에서 확장자(점 포함)를 추출한다. 없으면 빈 문자열. */
  private String extractExtension(String fileName) {
    if (fileName == null) {
      return "";
    }
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
  }
}
