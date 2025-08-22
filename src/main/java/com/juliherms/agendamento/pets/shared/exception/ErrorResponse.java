package com.juliherms.agendamento.pets.shared.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resposta padronizada de erro seguindo a especificação Problem Details.
 * Inclui timestamp, status HTTP, código de negócio, mensagem e detalhes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        List<String> details,
        String path,
        String traceId
) {

    public static ErrorResponse of(int status, String error, String message, String path, String traceId) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                null,
                message,
                null,
                path,
                traceId
        );
    }

    public static ErrorResponse of(int status, String error, String code, String message, String path, String traceId) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                code,
                message,
                null,
                path,
                traceId
        );
    }

    public static ErrorResponse of(int status, String error, String code, String message, List<String> details, String path, String traceId) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                code,
                message,
                details,
                path,
                traceId
        );
    }

    public static ErrorResponse of(int status, String error, String code, String message, Map<String, String> fieldErrors, String path, String traceId) {
        List<String> details = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toList();
        
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                code,
                message,
                details,
                path,
                traceId
        );
    }
}
