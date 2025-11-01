package com.kujacic.courses.exception;

public class CourseLevelNotFoundException extends RuntimeException {
    public CourseLevelNotFoundException(String message) {
        super(message);
    }
}