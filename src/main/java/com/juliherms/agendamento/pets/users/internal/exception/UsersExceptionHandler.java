package com.juliherms.agendamento.pets.users.internal.exception;

import com.juliherms.agendamento.pets.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Classe de tratamento de exceções específicas relacionadas aos usuários.
 * Cada exceção estende a classe BusinessException com um código e status HTTP apropriados.
 */
public final class UsersExceptionHandler {

    private UsersExceptionHandler() {}

    public static class UsuarioNaoEncontradoException extends BusinessException {
        public UsuarioNaoEncontradoException(String mensagem) {
            super(mensagem, HttpStatus.NOT_FOUND, "USR-001");
        }
    }

    public static class EmailJaCadastradoException extends BusinessException {
        public EmailJaCadastradoException(String mensagem) {
            super(mensagem, HttpStatus.CONFLICT, "USR-002");
        }
    }

    public static class TokenInvalidoException extends BusinessException {
        public TokenInvalidoException(String mensagem) {
            super(mensagem, HttpStatus.UNPROCESSABLE_ENTITY, "USR-003");
        }
    }

    public static class TokenExpiradoException extends BusinessException {
        public TokenExpiradoException(String mensagem) {
            super(mensagem, HttpStatus.UNPROCESSABLE_ENTITY, "USR-004");
        }
    }

    public static class PerfilNaoPermitidoException extends BusinessException {
        public PerfilNaoPermitidoException(String mensagem) {
            super(mensagem, HttpStatus.FORBIDDEN, "USR-005");
        }
    }

    public static class ContaNaoAtivaException extends BusinessException {
        public ContaNaoAtivaException(String mensagem) {
            super(mensagem, HttpStatus.FORBIDDEN, "USR-006");
        }
    }
}
