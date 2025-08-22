package com.juliherms.agendamento.pets.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção base para erros de negócio da aplicação.
 * Permite configurar código HTTP, código de negócio e metadados adicionais.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String codigoNegocio;
    private final Object metadata;

    public BusinessException(String mensagem) {
        this(mensagem, HttpStatus.UNPROCESSABLE_ENTITY, null, null);
    }

    public BusinessException(String mensagem, HttpStatus httpStatus) {
        this(mensagem, httpStatus, null, null);
    }

    public BusinessException(String mensagem, HttpStatus httpStatus, String codigoNegocio) {
        this(mensagem, httpStatus, codigoNegocio, null);
    }

    public BusinessException(String mensagem, HttpStatus httpStatus, String codigoNegocio, Object metadata) {
        super(mensagem);
        this.httpStatus = httpStatus;
        this.codigoNegocio = codigoNegocio;
        this.metadata = metadata;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCodigoNegocio() {
        return codigoNegocio;
    }

    public Object getMetadata() {
        return metadata;
    }
}
