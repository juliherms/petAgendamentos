package com.juliherms.agendamento.pets.notifications.internal.exception;

import com.juliherms.agendamento.pets.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Exceções específicas do módulo de notificações.
 * Cada exceção possui um código de negócio único para identificação.
 */
public final class NotificationsExceptionHandler {

    private NotificationsExceptionHandler() {}

    public static class CanalNaoSuportadoException extends BusinessException {
        public CanalNaoSuportadoException(String mensagem) {
            super(mensagem, HttpStatus.BAD_REQUEST, "NOT-001");
        }
    }

    public static class FalhaEnvioException extends BusinessException {
        public FalhaEnvioException(String mensagem) {
            super(mensagem, HttpStatus.INTERNAL_SERVER_ERROR, "NOT-002");
        }
    }

    public static class RateLimitExcedidoException extends BusinessException {
        public RateLimitExcedidoException(String mensagem) {
            super(mensagem, HttpStatus.TOO_MANY_REQUESTS, "NOT-003");
        }
    }
}
