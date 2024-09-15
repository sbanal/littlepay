package com.github.sbanal.littlepay;

public class InvalidTripEventException extends RuntimeException {
    public InvalidTripEventException(String message, Throwable e) {
        super(message, e);
    }

    public InvalidTripEventException(String message) {
        super(message);
    }
}
