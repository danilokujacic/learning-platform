package com.kujacic.courses.exception;

public class VideoProcessingError extends RuntimeException{

    public VideoProcessingError() {
        super("Video failed to process");
    }
}
