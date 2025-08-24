package com.juliherms.agendamento.pets.users.internal.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.domain.VerificationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VerificationTokenRepositoryTest {

    @Autowired
    private VerificationTokenRepository repository;

    @Test
    void shouldFindLatestValidToken() {
        VerificationToken token = new VerificationToken();
        token.setIdUsuario(1L);
        token.setCanal(UserApi.CanalVerificacao.EMAIL);
        token.setTokenHash("hash");
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setUtilizado(false);
        repository.save(token);

        var found = repository.findTopByIdUsuarioAndCanalAndUtilizadoIsFalseAndExpiresAtAfterOrderByExpiresAtDesc(
                1L, UserApi.CanalVerificacao.EMAIL, Instant.now());

        assertThat(found).isPresent();
        assertThat(found.get().getIdUsuario()).isEqualTo(1L);
    }
}
