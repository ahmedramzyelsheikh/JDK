package com.jdk.rest.error;

/** Exceptions the service layer throws; the web layer maps them to HTTP status codes. */
public final class ApiExceptions {

    private ApiExceptions() {
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String resource, long id) {
            super(resource + " " + id + " does not exist");
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    public static class PreconditionFailedException extends RuntimeException {
        public PreconditionFailedException(String message) {
            super(message);
        }
    }
}
