package com.naz1k1.controller;

import com.naz1k1.enums.AudioFormat;
import com.naz1k1.service.ConverterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
public class VideoToAudioController {

    private final ConverterService converterService;

    public VideoToAudioController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/video-audio-convert")
    public ResponseEntity<byte[]> convertVideoToAudio(
            @RequestParam("file") MultipartFile videoFile,
            @RequestParam(value = "format", defaultValue = "mp3") String format,
            @RequestParam(value = "bitrate", defaultValue = "128000") int bitrate,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate,
            @RequestParam(value = "channels", required = false) Integer channels) throws IOException {

        AudioFormat audioFormat = AudioFormat.fromExtension(format);

        byte[] audioBytes = converterService.VideoToAudioConverter(
                videoFile,
                format,
                bitrate,
                sampleRate,
                channels
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(audioFormat.getMimeType()));
        headers.setContentDispositionFormData("attachment", "converted." + audioFormat.getExtension());
        headers.setContentLength(audioBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }
}
