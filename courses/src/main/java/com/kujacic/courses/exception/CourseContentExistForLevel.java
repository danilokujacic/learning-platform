package com.kujacic.courses.exception;

public class CourseContentExistForLevel extends RuntimeException{

    public CourseContentExistForLevel() {
        super("Course content for given level already exists");
    }
}
