package com.juliherms.agendamento.pets.users.internal.service;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.api.UserApi.CanalVerificacao;
import com.juliherms.agendamento.pets.users.api.UserApi.Perfil;
import com.juliherms.agendamento.pets.users.api.UserApi.Status;
import com.juliherms.agendamento.pets.users.internal.domain.User;
import com.juliherms.agendamento.pets.users.internal.domain.VerificationToken;
import com.juliherms.agendamento.pets.users.api.UserCreatedEvent;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import com.juliherms.agendamento.pets.users.internal.repo.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço para gerenciamento de usuários, incluindo criação e verificação.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final ApplicationEventPublisher events;

    public UserService(UserRepository userRepository, VerificationTokenRepository tokenRepository, ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.events = events;
    }

    /**
     * Cria um novo usuário, gera um token de verificação e publica um evento para envio do token.
     * @param req Dados para criação do usuário.
     * @return Detalhes do usuário criado.
     * @throws ConflictException se o email já estiver cadastrado.
     */
    @Transactional
    public UserApi.UserResponse criar(UserApi.CreateUserRequest req) {
        String normalizedEmail = req.email().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(u -> {
            throw new ConflictException("email já cadastrado");
        });

        User user = new User();
        user.setNome(req.nome());
        user.setEmail(normalizedEmail);
        user.setTelefone(req.telefone());
        user.setEndereco(req.endereco());
        user.setSenhaHash(BCrypt.hashpw(req.senha(), BCrypt.gensalt()));
        user.setPerfil(req.perfil());
        user.setStatus(Status.pendente_verificacao);
        user.setEmailVerificado(false);
        user.setTelefoneVerificado(false);
        user = userRepository.save(user);

        String rawToken = UUID.randomUUID().toString().replace("-", "");
        VerificationToken token = new VerificationToken();
        token.setIdUsuario(user.getId());
        token.setCanal(req.preferenciaVerificacao());
        token.setTokenHash(BCrypt.hashpw(rawToken, BCrypt.gensalt()));
        Instant expiresAt = Instant.now().plus(Duration.ofHours(24));
        token.setExpiresAt(expiresAt);
        token.setUtilizado(false);

        tokenRepository.save(token);

        events.publishEvent(new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getTelefone(),
                req.preferenciaVerificacao(),
                rawToken,
                expiresAt
        ));

        return new UserApi.UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getTelefone(),
                user.getEndereco(),
                user.getPerfil(),
                user.getStatus(),
                user.isEmailVerificado(),
                user.isTelefoneVerificado(),
                user.getCreatedAt()
        );
    }

    /**
     * Verifica o usuário usando o token fornecido.
     * @param idUsuario ID do usuário a ser verificado.
     * @param req Dados da verificação, incluindo o canal e o token.
     * @return Detalhes do usuário após a verificação.
     * @throws NotFoundException se o usuário não for encontrado.
     * @throws ValidationException se o token for inválido ou expirado.
     */
    @Transactional
    public UserApi.UserResponse verificar(Long idUsuario, UserApi.VerifyRequest req) {

        User user = userRepository.findById(idUsuario).orElseThrow(() -> new NotFoundException("usuario não encontrado"));

        Optional<VerificationToken> opt =
                tokenRepository.findTopByidUsuarioAndCanalAndUtilizadoIsFalseAndExpiresAtAfterOrderByExpiresAtDesc(
                user.getId(), req.canal(), Instant.now()
        );

        VerificationToken token = opt.orElseThrow(() -> new ValidationException("token inválido ou expirado"));

        if (!BCrypt.checkpw(req.token(), token.getTokenHash())) {
            throw new ValidationException("token inválido ou expirado");
        }
        token.setUtilizado(true);
        // salva o token como utilizado
        tokenRepository.save(token);

        user.setStatus(Status.ativo);
        if (req.canal() == CanalVerificacao.EMAIL) user.setEmailVerificado(true); else user.setTelefoneVerificado(true);
        // atualiza o status do usuário e marca o canal como verificado
        userRepository.save(user);

        return new UserApi.UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getTelefone(),
                user.getEndereco(),
                user.getPerfil(),
                user.getStatus(),
                user.isEmailVerificado(),
                user.isTelefoneVerificado(),
                user.getCreatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException { public NotFoundException(String m){super(m);} }
    public static class ConflictException extends RuntimeException { public ConflictException(String m){super(m);} }
    public static class ValidationException extends RuntimeException { public ValidationException(String m){super(m);} }
}


