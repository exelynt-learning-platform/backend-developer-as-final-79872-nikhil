package com.example.booking.exception;

public class ResourceAlreadyBookedException extends RuntimeException {

    public ResourceAlreadyBookedException(String message) {
        super(message);
    }
}