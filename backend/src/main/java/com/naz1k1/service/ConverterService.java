package com.naz1k1.service;

import com.naz1k1.enums.AudioFormat;
import com.naz1k1.exception.ConversionException;
import com.naz1k1.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Service
public class ConverterService {
        /**
         * 将视频文件转换为音频
         *
         * @param videoFile  要转换的视频文件
         * @param format    目标音频格式
         * @param bitrate   音频比特率
         * @param sampleRate 采样率
         * @param channels  声道数
         * @return byte[] 转换后的音频数据
         * @throws ConversionException 当转换过程发生错误时抛出
         */
        public byte[] convertVideoToAudio(
                MultipartFile videoFile,
                String format,
                int bitrate,
                Integer sampleRate,
                Integer channels) throws ConversionException {

            validateInputFile(videoFile);
            AudioFormat audioFormat = AudioFormat.fromExtension(format);

            Path tempVideoFile = null;
            Path tempAudioFile = null;

            try {
            String videoExtension = getFileExtension(videoFile.getOriginalFilename());
            tempVideoFile = Files.createTempFile("video-", "." + videoExtension);
            tempAudioFile = Files.createTempFile("audio-", "." + audioFormat.getExtension());
            
            log.debug("创建临时文件 - 视频: {}, 音频: {}", tempVideoFile, tempAudioFile);
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

            } catch (IOException e) {
                log.error("音频转换失败", e);
                throw new ConversionException("音频转换失败: " + e.getMessage(), e);
            } finally {
                cleanupTempFiles(tempVideoFile, tempAudioFile);
            }
        }

    
        /**
         * 执行音频转换过程
         *
         * @param inputFile  输入文件路径
         * @param outputFile 输出文件路径
         * @param audioCodec 音频编解码器ID
         * @param bitrate   比特率
         * @param sampleRate 采样率
         * @param channels  声道数
         * @throws IOException 当IO操作失败时抛出
         */
        private void convertToAudio(
                String inputFile,
                String outputFile,
                int audioCodec,
                int bitrate,
                Integer sampleRate,
                Integer channels) throws IOException {

            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;

            try {
                // 初始化视频帧抓取器
                grabber = initializeGrabber(inputFile);
                grabber.start();

                // 初始化音频记录器
                recorder = initializeRecorder(
                        outputFile,
                        audioCodec,
                        bitrate,
                        sampleRate != null ? sampleRate : grabber.getSampleRate(),
                        channels != null ? channels : grabber.getAudioChannels()
                );
                recorder.start();

                // 执行转换
                processFrames(grabber, recorder);
                
            } catch (IOException e) {
                log.error("转换过程中发生错误", e);
                throw new ConversionException("转换失败: " + e.getMessage(), e);
            } finally {
                closeResources(grabber, recorder);
            }
        }
        

        /**
         * 验证输入文件
         *
         * @param file 要验证的文件
         * @throws InvalidInputException 当文件验证失败时抛出
         */
        private void validateInputFile(MultipartFile file) {
            if (file == null || file.isEmpty()) {
                throw new InvalidInputException("输入文件不能为空");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new InvalidInputException("只支持视频文件格式");
            }

            long maxSize = 500 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new InvalidInputException("文件大小超过限制（最大500MB）");
            }
        }

        /**
         * 清理临时文件
         *
         * @param files 要清理的文件路径数组
         */
        private void cleanupTempFiles(Path... files) {
            for (Path file : files) {
                if (file != null) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        log.warn("清理临时文件失败: {}", file, e);
                    }
                }
            }
        }

        /**
         * 初始化FFmpeg帧抓取器
         *
         * @param inputFile 输入文件路径
         * @return FFmpegFrameGrabber 初始化后的帧抓取器
         */
        private FFmpegFrameGrabber initializeGrabber(String inputFile) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
            // 设置额外的grabber选项，提高性能
            grabber.setOption("threads", "auto");        // 自动选择线程数
            grabber.setOption("analyzeduration", "10M"); // 分析时长限制
            return grabber;
        }

        /**
         * 初始化FFmpeg帧记录器
         *
         * @param outputFile 输出文件路径
         * @param audioCodec 音频编解码器ID
         * @param bitrate   比特率
         * @param sampleRate 采样率
         * @param channels  声道数
         * @return FFmpegFrameRecorder 初始化后的帧记录器
         */
        private FFmpegFrameRecorder initializeRecorder(
                String outputFile,
                int audioCodec,
                int bitrate,
                int sampleRate,
                int channels) {
                
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0);
            
            // 基本设置
            recorder.setAudioCodec(audioCodec);
            recorder.setAudioBitrate(bitrate);
            recorder.setSampleRate(sampleRate);
            recorder.setAudioChannels(channels);

            // 音频质量设置
            recorder.setAudioQuality(0); // 最高质量
            recorder.setAudioOption("crf", "0");

            // 其他优化选项
            recorder.setOption("threads", "auto");
            recorder.setOption("preset", "medium"); // 平衡编码速度和质量

            return recorder;
        }

        /**
         * 处理音频帧转换
         *
         * @param grabber  帧抓取器
         * @param recorder 帧记录器
         * @throws IOException 当处理帧时发生IO错误
         */
        private void processFrames(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) 
                throws IOException {
            Frame frame;
            long totalFrames = grabber.getLengthInAudioFrames();
            long processedFrames = 0;
            
            while ((frame = grabber.grab()) != null) {
                if (frame.samples != null) {
                    recorder.record(frame);
                    processedFrames++;
                    
                    // 记录进度
                    if (totalFrames > 0 && processedFrames % 100 == 0) {
                        double progress = (double) processedFrames / totalFrames * 100;
                        log.debug("转换进度: {:.2f}%", progress);
                    }
                }
            }
        }

        /**
         * 关闭资源
         *
         * @param grabber  要关闭的帧抓取器
         * @param recorder 要关闭的帧记录器
         */
        private void closeResources(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.close();
                } catch (IOException e) {
                    log.warn("关闭recorder时发生错误", e);
                }
            }
            
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.close();
                } catch (IOException e) {
                    log.warn("关闭grabber时发生错误", e);
                }
            }
        }

        /**
         * 获取文件扩展名
         *
         * @param filename 文件名
         * @return String 文件扩展名
         */
        private String getFileExtension(String filename) {
        if (filename == null) {
            return "tmp";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > -1 ? filename.substring(lastDotIndex + 1) : "tmp";
    }

}
