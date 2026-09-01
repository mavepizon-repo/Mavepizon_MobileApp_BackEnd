package com.example.MpApp.dto.callLogs;

public enum CallStatus {
    COMPLETED, BUSY, NO_ANSWER, FAILED, UNKNOWN;

    public static boolean isValid(String value) {
        for (CallStatus status : values()) {
            if (status.name().equals(value)) return true;
        }
        return false;
    }
}