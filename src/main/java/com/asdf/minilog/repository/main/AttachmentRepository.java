package com.asdf.minilog.repository.main;

import com.asdf.minilog.entity.main.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 첨부파일(Attachment) 엔티티를 관리하는 리포지토리. */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {}
