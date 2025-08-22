package com.juliherms.agendamento.pets.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global centralizado para captura e tratamento de exceções.
 * Delega tratamento específico para handlers de módulos quando aplicável.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        String path = request.getRequestURI();
        
        ErrorResponse error = ErrorResponse.of(
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getCodigoNegocio(),
                ex.getMessage(),
                path,
                traceId
        );

        log.warn("Exceção de negócio capturada: status={}, code={}, path={}, traceId={}, excecao={}",
                ex.getHttpStatus().value(), ex.getCodigoNegocio(), path, traceId, ex.getClass().getSimpleName());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        String path = request.getRequestURI();
        
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "VAL-001",
                "Dados de entrada inválidos",
                fieldErrors,
                path,
                traceId
        );

        log.warn("Exceção de validação capturada: status=400, path={}, traceId={}, campos={}",
                path, traceId, fieldErrors.keySet());

        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        String path = request.getRequestURI();
        
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "VAL-002",
                "Violação de restrições de validação",
                fieldErrors,
                path,
                traceId
        );

        log.warn("Violação de restrição capturada: status=400, path={}, traceId={}, campos={}",
                path, traceId, fieldErrors.keySet());

        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId(request);
        String path = request.getRequestURI();
        
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "SYS-001",
                "Erro interno do servidor",
                path,
                traceId
        );

        log.error("Exceção não tratada capturada: status=500, path={}, traceId={}, excecao={}",
                path, traceId, ex.getClass().getSimpleName(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(error);
    }

    private String getTraceId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId != null && !requestId.trim().isEmpty()) {
            return requestId;
        }
        
        // Gera um trace ID único se não fornecido
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getSessionId() != null ? 
                    attributes.getSessionId() : 
                    "trace-" + System.currentTimeMillis();
        }
        
        return "trace-" + System.currentTimeMillis();
    }
}
