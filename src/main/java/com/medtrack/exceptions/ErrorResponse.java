package com.medtrack.exceptions;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors) {
}
