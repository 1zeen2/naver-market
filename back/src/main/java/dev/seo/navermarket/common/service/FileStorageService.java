package dev.seo.navermarket.common.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * @file FileStorageService.java
 * @brief 파일 저장 및 관리를 위한 서비스 인터페이스입니다.
 * 다양한 파일 저장 방식 (로컬, 클라우드 스토리지 등)을 추상화합니다.
 */
public interface FileStorageService {

    /**
     * @brief MultipartFile을 저장하고 저장된 파일의 URL 또는 경로를 반환합니다.
     * @param file 저장할 MultipartFile 객체
     * @return 저장된 파일의 접근 가능한 URL 또는 경로
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    String storeFile(MultipartFile file) throws IOException;

    /**
     * @brief 지정된 파일 URL 또는 경로에 해당하는 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 URL 또는 경로
     * @return 파일 삭제 성공 여부 (true: 성공, false: 파일이 존재하지 않거나 삭제 실패)
     */
    boolean deleteFile(String fileUrl);
}
