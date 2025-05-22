package com.naz1k1.enums;

import org.bytedeco.ffmpeg.global.avcodec;

public enum AudioFormat {
    MP3("mp3", avcodec.AV_CODEC_ID_MP3,"audio/mpeg"),
    ACC("aac", avcodec.AV_CODEC_ID_AAC,"audio/acc"),
    WAV("wav", avcodec.AV_CODEC_ID_PCM_S16LE,"audio/wav"),
    FLAC("flac", avcodec.AV_CODEC_ID_FLAC,"audio/flac"),
    OGG("ogg", avcodec.AV_CODEC_ID_VORBIS,"audio/ogg");

    private final String extension;
    private final int codecId;
    private final String mimeType;

    AudioFormat(String extension, int codecId, String mimeType) {
        this.extension = extension;
        this.codecId = codecId;
        this.mimeType = mimeType;
    }

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
