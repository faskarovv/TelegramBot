package org.example.exception;

public class UploadFileException extends RuntimeException {

    public UploadFileException(String message, Throwable cause) {
        super(message);
    }

    public UploadFileException(String message) {
        super(message);
    }

    public UploadFileException(Throwable cause){
        super(cause);
    }
}
