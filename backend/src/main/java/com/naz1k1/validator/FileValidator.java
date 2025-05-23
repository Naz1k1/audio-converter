package com.naz1k1.validator;

import com.naz1k1.exception.InvalidInputException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

    /**
     * 验证文件大小
     *
     * @param file 要验证的文件
     * @throws InvalidInputException 当文件大小超过限制时抛出
     */
    public static void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidInputException("文件大小超过限制（最大500MB）");
        }
    }
    
    /**
     * 验证文件类型
     *
     * @param file 要验证的文件
     * @throws InvalidInputException 当文件类型不支持时抛出
     */
    public static void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new InvalidInputException("只支持视频文件格式");
        }
    }
}