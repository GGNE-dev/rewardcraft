package org.ggne.rc.global.exception;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, 409);
    }
}
