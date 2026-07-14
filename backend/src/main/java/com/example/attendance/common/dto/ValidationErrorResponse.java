package com.example.attendance.common.dto;

import java.util.List;

public record ValidationErrorResponse(String message, List<FieldError> errors) {
    public record FieldError(String field, String message) {
    }
}
