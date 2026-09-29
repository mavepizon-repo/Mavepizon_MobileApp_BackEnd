package com.example.MpApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Expected, user-correctable failure in the OTP flow (bad code, expired code,
 * cooldown hit). These are business outcomes rather than server faults, so they
 * surface as 400/429 instead of falling through to the 500 catch-all.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OtpException extends RuntimeException {

    private final String code;

    public OtpException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
