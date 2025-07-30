package dev.seo.navermarket.common.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.seo.navermarket.common.service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @file FileStorageServiceImpl.java
 * @brief FileStorageService 인터페이스의 구현체로, 로컬 파일 시스템에 파일을 저장하고 관리합니다.
 * application.yml에 설정된 'file.upload-dir' 경로에 파일을 저장합니다.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class); // ✨ 로거 인스턴스

    // application.yml에서 파일 업로드 디렉토리 경로를 주입받습니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation; // 파일 저장 디렉토리 Path 객체

    /**
     * @brief 빈 초기화 시 파일 저장 디렉토리를 생성합니다.
     * 디렉토리가 없으면 생성하고, 접근 권한을 확인합니다.
     */
    @PostConstruct // 빈 생성 후 초기화 메서드
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation); // 디렉토리 생성 시도
            log.info("파일 저장 디렉토리 초기화 완료: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            log.error("파일 저장 디렉토리를 생성할 수 없습니다: {}", uploadDir, ex);
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * @brief MultipartFile을 로컬 파일 시스템에 저장합니다.
     * 파일명은 UUID를 사용하여 고유하게 생성됩니다.
     * @param file 저장할 MultipartFile 객체
     * @return 저장된 파일의 웹 접근 가능한 상대 경로 (예: /uploads/uuid.jpg)
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    @Override
    public String storeFile(MultipartFile file) throws IOException {
        // 파일명에서 확장자를 추출합니다.
        String fileName = file.getOriginalFilename();
        String fileExtension = "";
        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf("."));
        }

        // 고유한 파일명 생성 (UUID + 확장자)
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(newFileName); // fileStorageLocation 사용

        try {
            // 파일 저장
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 성공: {} -> {}", file.getOriginalFilename(), newFileName);
            // 저장된 파일의 웹 접근 가능한 상대 경로를 반환합니다.
            // 실제 서비스에서는 이 /uploads/ 경로는 웹 서버 (예: Nginx, Apache) 또는 Spring Boot의 정적 리소스 설정에 의해
            // 실제 파일 시스템 경로와 매핑되어야 합니다.
            return "/uploads/" + newFileName; // 웹 접근 가능한 URL 형식으로 반환
        } catch (IOException ex) {
            log.error("파일 저장 실패: {}", newFileName, ex);
            throw new IOException("파일 " + newFileName + "을 저장할 수 없습니다. 다시 시도해주세요!", ex);
        }
    }

    /**
     * @brief 지정된 파일 URL 또는 경로에 해당하는 파일을 로컬 파일 시스템에서 삭제합니다.
     * @param fileUrl 삭제할 파일의 URL 또는 경로 (예: /uploads/uuid.jpg)
     * @return 파일 삭제 성공 여부 (true: 성공, false: 파일이 존재하지 않거나 삭제 실패)
     */
    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty() || fileUrl.endsWith("default_profile.png")) { // 기본 이미지는 삭제하지 않음
            log.debug("삭제 대상 파일이 없거나 기본 이미지입니다: {}", fileUrl);
            return false; // 삭제할 필요 없거나 기본 이미지이므로 false 반환
        }
        
        // URL에서 실제 파일명 추출 (예: /uploads/uuid.jpg -> uuid.jpg)
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize(); // fileStorageLocation 사용

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("파일 삭제 성공: {}", fileName);
            } else {
                log.warn("삭제할 파일을 찾을 수 없거나 이미 삭제되었습니다: {}", fileName);
            }
            return deleted;
        } catch (IOException ex) {
            log.error("파일 삭제 실패: {}", fileName, ex); // System.err.println 대신 로거 사용
            return false;
        }
    }
}
