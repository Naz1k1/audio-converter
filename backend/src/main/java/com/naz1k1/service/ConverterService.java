package com.naz1k1.service;

import com.naz1k1.enums.AudioFormat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ConverterService {

    public byte[] VideoToAudioConverter(
            MultipartFile videoFile,
            String format,
            int bitrate,
            Integer sampleRate,
            Integer channels) throws IOException {

        AudioFormat audioFormat = AudioFormat.fromExtension(format);

        Path tempVideoFile = Files.createTempFile("video-",".tmp");
        Path tempAudioFile = Files.createTempFile("audio-",audioFormat.getExtension());

        try {
            // 保存上传的视频文件到临时位置
            videoFile.transferTo(tempVideoFile);

            convertToAudio(
                    tempVideoFile.toString(),
                    tempAudioFile.toString(),
                    audioFormat.getCodecId(),
                    bitrate,
                    sampleRate,
                    channels
            );

            return Files.readAllBytes(tempAudioFile);
        } finally {
            Files.deleteIfExists(tempVideoFile);
            Files.deleteIfExists(tempAudioFile);
        }
    }

    private void convertToAudio(String inputFile, String outputFile,
                                int audioCodec, int bitrate,
                                Integer sampleRate, Integer channels) throws IOException {

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        FFmpegFrameRecorder recorder = null;

        try {
            grabber.start();

            // 显式设置音频参数
            recorder = new FFmpegFrameRecorder(outputFile, 0);
            recorder.setFormat("mp3"); // 明确指定输出格式
            recorder.setAudioCodec(audioCodec);
            recorder.setAudioBitrate(bitrate);
            recorder.setSampleRate(sampleRate != null ? sampleRate : grabber.getSampleRate());
            recorder.setAudioChannels(channels != null ? channels : grabber.getAudioChannels());

            // 关键：设置音频流参数
            recorder.setAudioOption("crf", "0");
            recorder.setAudioQuality(0);

            recorder.start();

            Frame frame;
            while ((frame = grabber.grab()) != null) {
                if (frame.samples != null) {
                    recorder.record(frame);
                }
            }
        } finally {
            if (recorder != null) {
                recorder.stop();
                recorder.close();
            }
            grabber.stop();
            grabber.close();
        }
    }
}
