package com.juliherms.agendamento.pets.verificador.internal.exception;

import com.juliherms.agendamento.pets.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceções específicas do módulo verificador.
 * Cada exceção possui um código de negócio único para identificação.
 */
public final class VerificadorExceptionHandler {

    private VerificadorExceptionHandler() {}

    public static class CanalNaoSuportadoException extends BusinessException {
        public CanalNaoSuportadoException(String mensagem) {
            super(mensagem, HttpStatus.BAD_REQUEST, "VER-001");
        }
    }

    public static class FalhaEnvioEmailException extends BusinessException {
        public FalhaEnvioEmailException(String mensagem) {
            super(mensagem, HttpStatus.INTERNAL_SERVER_ERROR, "VER-002");
        }
    }

    public static class FalhaEnvioSMSException extends BusinessException {
        public FalhaEnvioSMSException(String mensagem) {
            super(mensagem, HttpStatus.INTERNAL_SERVER_ERROR, "VER-003");
        }
    }

    public static class RateLimitExcedidoException extends BusinessException {
        public RateLimitExcedidoException(String mensagem) {
            super(mensagem, HttpStatus.TOO_MANY_REQUESTS, "VER-004");
        }
    }
}
