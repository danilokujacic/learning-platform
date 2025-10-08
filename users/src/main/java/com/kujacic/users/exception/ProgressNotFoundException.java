package com.kujacic.users.exception;

public class ProgressNotFoundException extends RuntimeException {
    public ProgressNotFoundException(String message) {
        super(message);
    }

    public ProgressNotFoundException(Integer courseId, String userId) {
        super(String.format("Progress not found for courseId: %d and userId: %s", courseId, userId));
    }
}