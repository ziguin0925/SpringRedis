package org.example.waitingapi.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {

    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001","Already registered in queue"),

    QUEUE_ALREADY_REGISTERED_USER2(HttpStatus.CONFLICT, "UQ-0001","Already registered in %s");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ApplicationException build() {
        return new ApplicationException(httpStatus, code, message);
    }


    public ApplicationException build(Object... args) {
        return new ApplicationException(httpStatus, code, message.formatted(args));
    }

}
