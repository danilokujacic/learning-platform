package com.kujacic.courses.exception;

public class CertificateAlreadyExistsException extends RuntimeException{
    public CertificateAlreadyExistsException() {
        super("Certificate already exist for this course");
    }
}
