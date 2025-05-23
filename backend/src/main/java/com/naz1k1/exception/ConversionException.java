package com.naz1k1.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversionException extends RuntimeException {
    
    public ConversionException(String message) {
        super(message);
        log.error(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
        log.error(message, cause);
    }
}
