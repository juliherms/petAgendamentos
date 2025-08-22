package com.juliherms.agendamento.pets.services.internal.exception;

import com.juliherms.agendamento.pets.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceções específicas do módulo de serviços.
 * Cada exceção possui um código de negócio único para identificação.
 */
public final class ServicesExceptionHandler {

    private ServicesExceptionHandler() {}

    public static class ServicoNaoEncontradoException extends BusinessException {
        public ServicoNaoEncontradoException(String mensagem) {
            super(mensagem, HttpStatus.NOT_FOUND, "SRV-001");
        }
    }

    public static class PrecosInvalidosException extends BusinessException {
        public PrecosInvalidosException(String mensagem) {
            super(mensagem, HttpStatus.UNPROCESSABLE_ENTITY, "SRV-002");
        }
    }

    public static class DadosServicoInvalidosException extends BusinessException {
        public DadosServicoInvalidosException(String mensagem) {
            super(mensagem, HttpStatus.BAD_REQUEST, "SRV-003");
        }
    }

    public static class ServicoInativoException extends BusinessException {
        public ServicoInativoException(String mensagem) {
            super(mensagem, HttpStatus.FORBIDDEN, "SRV-004");
        }
    }
}
