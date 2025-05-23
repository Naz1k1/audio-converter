package com.naz1k1.enums;

import org.bytedeco.ffmpeg.global.avcodec;

public enum AudioFormat {
    MP3("mp3", avcodec.AV_CODEC_ID_MP3,"audio/mpeg"),
    ACC("aac", avcodec.AV_CODEC_ID_AAC,"audio/acc"),
    WAV("wav", avcodec.AV_CODEC_ID_PCM_S16LE,"audio/wav"),
    FLAC("flac", avcodec.AV_CODEC_ID_FLAC,"audio/flac");

    private final String extension;
    private final int codecId;
    private final String mimeType;


    /**
     * 音频格式枚举构造函数
     *
     * @param extension 文件扩展名
     * @param codecId  编解码器ID
     * @param mimeType MIME类型
     */
    AudioFormat(String extension, int codecId, String mimeType) {
        this.extension = extension;
        this.codecId = codecId;
        this.mimeType = mimeType;
    }

    /**
     * 根据扩展名获取音频格式
     *
     * @param extension 文件扩展名
     * @return AudioFormat 对应的音频格式枚举值
     * @throws IllegalArgumentException 当扩展名不支持时抛出
     */
    public static AudioFormat fromExtension(String extension) {
        for (AudioFormat format : AudioFormat.values()) {
            if (format.extension.equalsIgnoreCase(extension)) {
                return format;
            }
        }
        throw new IllegalArgumentException("不支持的音频格式: " + extension);
    }

    public String getExtension() {
        return extension;
    }

    public int getCodecId() {
        return codecId;
    }

    public String getMimeType() {
        return mimeType;
    }
}
