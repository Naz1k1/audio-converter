package com.naz1k1.controller;

import com.naz1k1.enums.AudioFormat;
import com.naz1k1.service.ConverterService;
import com.naz1k1.validator.FileValidator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/converter")
@Valid
@CrossOrigin(origins = "*", maxAge = 3600)
public class VideoToAudioController {

    private final ConverterService converterService;

    public VideoToAudioController(ConverterService converterService) {
        this.converterService = converterService;
    }

    
 /**
     * 处理视频转音频的请求
     *
     * @param videoFile  要转换的视频文件
     * @param format    目标音频格式 (mp3/aac/wav/flac)
     * @param bitrate   音频比特率 (32000-320000)
     * @param sampleRate 采样率 (8000-192000)
     * @param channels  声道数 (1-8)
     * @return ResponseEntity<byte[]> 包含转换后的音频数据
     * @throws IOException 当文件处理发生错误时抛出
     */
    @PostMapping("/video-audio-convert")
    public ResponseEntity<byte[]> convertVideoToAudio(
            @RequestParam("file") @NotNull(message = "请选择要转换的视频文件") MultipartFile videoFile,
            @RequestParam(value = "format", defaultValue = "mp3") 
                @Pattern(regexp = "^(mp3|aac|wav|flac)$" ,message = "不支持的音频格式") String format,
            @RequestParam(value = "bitrate", defaultValue = "128000") 
                @Min(value = 32000, message = "比特率不能小于32kbps") @Max(value = 320000, message = "比特率不能大于320kps") int bitrate,
            @RequestParam(value = "sampleRate", required = false) 
                @Min(value = 8000, message = "采样率不能小于8kHz") @Max(value = 192000, message = "采样率不能大于192kHz")Integer sampleRate,
            @RequestParam(value = "channels", required = false)  
                @Min(value = 1, message = "声道数不能小于1") @Max(value = 8, message = "声道数不能大于8") Integer channels ) 
                throws IOException {

        log.info("接收到视频转换请求 - 文件名: {}, 格式: {}, 比特率: {}, 采样率: {}, 声道数: {}", 
        videoFile.getOriginalFilename(), format, bitrate, sampleRate, channels);
                
        FileValidator.validateFileSize(videoFile);
        FileValidator.validateFileType(videoFile);
      
            byte[] audioBytes = converterService.convertVideoToAudio(
                videoFile,
                format,
                bitrate,
                sampleRate,
                channels
        );
        AudioFormat audioFormat = AudioFormat.fromExtension(format);
        HttpHeaders headers = buildResponseHeaders(audioFormat, audioBytes.length, 
                getOutputFileName(videoFile.getOriginalFilename(), format));

        log.info("视频转换成功 - 文件名: {}, 输出大小: {} bytes", 
                videoFile.getOriginalFilename(), audioBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }
      
     /**
     * 构建响应头信息
     *
     * @param format        音频格式
     * @param contentLength 内容长度
     * @param fileName      文件名
     * @return HttpHeaders 响应头对象
     */
      private HttpHeaders buildResponseHeaders(AudioFormat format, long contentLength, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(format.getMimeType()));
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(contentLength);
        return headers;
    }

    
    /**
     * 生成输出文件名
     *
     * @param originalFileName 原始文件名
     * @param format          目标格式
     * @return String 生成的输出文件名
     */
    private String getOutputFileName(String originalFileName, String format) {
        String baseName = originalFileName != null ? 
                originalFileName.replaceAll("\\.[^.]*$", "") : "converted";
        return baseName + "." + format;
    }
}
