package com.kujacic.courses.exception;

public class CourseContentNotFoundException extends RuntimeException {

    public CourseContentNotFoundException() {
        super("Course content not found");
    }

}
