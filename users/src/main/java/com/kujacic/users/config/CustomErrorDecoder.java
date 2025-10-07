package com.kujacic.users.config;

import com.kujacic.users.exception.ResourceNotFoundException;
import com.kujacic.users.exception.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new ResourceNotFoundException("Course not found");
            case 503:
                return new ServiceUnavailableException("Course service is unavailable");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
