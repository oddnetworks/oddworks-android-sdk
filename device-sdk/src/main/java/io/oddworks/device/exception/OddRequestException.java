package io.oddworks.device.exception;

public class OddRequestException extends RuntimeException {
    public OddRequestException() {
    }

    public OddRequestException(String detailMessage) {
        super(detailMessage);
    }
}
