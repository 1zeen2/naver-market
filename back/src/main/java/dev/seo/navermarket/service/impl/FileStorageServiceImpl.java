package dev.seo.navermarket.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import dev.seo.navermarket.service.FileStorageService;

public class FileStorageServiceImpl implements FileStorageService {
	
	@Value("${file.upload-dir}") // application.yml에 file.upload-dir 설정 필요
	private String uploadDir;
	
	@Override
    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        return fileName; // 저장된 파일명 반환 (URL은 나중에 컨트롤러에서 구성)
    }
	
	@Override
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.equals("default_profile.png")) { // 기본 이미지는 삭제하지 않음
            return;
        }
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + fileName + " - " + e.getMessage());
        }
    }

}
