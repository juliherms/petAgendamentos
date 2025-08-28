package com.juliherms.agendamento.pets.users.internal.service;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.api.UserApi.CanalVerificacao;
import com.juliherms.agendamento.pets.users.api.UserApi.Perfil;
import com.juliherms.agendamento.pets.users.api.UserApi.Status;
import com.juliherms.agendamento.pets.users.internal.domain.User;
import com.juliherms.agendamento.pets.users.internal.domain.VerificationToken;
import com.juliherms.agendamento.pets.users.api.UserCreatedEvent;
import com.juliherms.agendamento.pets.users.internal.exception.UsersExceptionHandler;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import com.juliherms.agendamento.pets.users.internal.repo.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pela criação e verificação de usuários.
 * Contém métodos para criar um novo usuário e verificar um usuário existente com um token.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final ApplicationEventPublisher events;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepository userRepository, VerificationTokenRepository tokenRepository, ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.events = events;
    }

    /**
     * Gera um token de validação contendo números e letras maiúsculas com até 6 caracteres.
     * @return String contendo o token gerado.
     */
    private String gerarTokenValidacao() {
        StringBuilder token = new StringBuilder();
        String caracteres = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // Gera um token com comprimento entre 4 e 6 caracteres
        int comprimento = secureRandom.nextInt(3) + 4; // 4, 5 ou 6 caracteres

        for (int i = 0; i < comprimento; i++) {
            int indice = secureRandom.nextInt(caracteres.length());
            token.append(caracteres.charAt(indice));
        }

        return token.toString();
    }

    /**
     * Cria um novo usuário com os dados fornecidos.
     * @param req Dados do usuário a ser criado.
     * @return Detalhes do usuário criado.
     * @throws UsersExceptionHandler.EmailJaCadastradoException se o email já estiver cadastrado.
     */
    @Transactional
    public UserApi.UserResponse criar(UserApi.CreateUserRequest req) {

        // Verifica se o email já está cadastrado
        String normalizedEmail = req.email().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(u -> {
            throw new UsersExceptionHandler.EmailJaCadastradoException("email já cadastrado");
        });

        // Cria o usuário com os dados fornecidos
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

        // Gera um token de verificação para o usuário
        String rawToken = gerarTokenValidacao();
        VerificationToken token = new VerificationToken();
        token.setIdUsuario(user.getId());
        token.setCanal(req.preferenciaVerificacao());
        token.setTokenHash(rawToken);
        Instant expiresAt = Instant.now().plus(Duration.ofHours(24));
        token.setExpiresAt(expiresAt);
        token.setUtilizado(false);

        tokenRepository.save(token);

        // Publica um evento de criação de usuário para iniciar o processo de verificação
        events.publishEvent(new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getTelefone(),
                req.preferenciaVerificacao(),
                rawToken,
                expiresAt
        ));

        // Retorna os detalhes do usuário criado
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
     * Verifica o usuário com o token fornecido.
     * @param idUsuario ID do usuário a ser verificado.
     * @param req Dados de verificação contendo o token e o canal.
     * @return Detalhes do usuário após a verificação.
     * @throws UsersExceptionHandler.UsuarioNaoEncontradoException se o usuário não for encontrado.
     * @throws UsersExceptionHandler.TokenInvalidoException se o token for inválido ou expirado.
     */
    @Transactional
    public UserApi.UserResponse verificar(Long idUsuario, UserApi.VerifyRequest req) {

        // Verifica se o usuário existe
        User user = userRepository.findById(idUsuario).orElseThrow(() -> new UsersExceptionHandler.UsuarioNaoEncontradoException("usuario não encontrado"));

        // Verifica se o canal de verificação é válido
        Optional<VerificationToken> opt =
                tokenRepository.findTopByIdUsuarioAndCanalAndUtilizadoIsFalseAndExpiresAtAfterOrderByExpiresAtDesc(
                        user.getId(), req.canal(), Instant.now()
                );

        // Se não encontrar um token válido, lança uma exceção
        VerificationToken token = opt.orElseThrow(() -> new UsersExceptionHandler.TokenInvalidoException("token inválido ou expirado"));

        // Verifica se o token fornecido corresponde ao token armazenado
        if (!BCrypt.checkpw(req.token(), token.getTokenHash())) {
            throw new UsersExceptionHandler.TokenInvalidoException("token inválido ou expirado");
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
}