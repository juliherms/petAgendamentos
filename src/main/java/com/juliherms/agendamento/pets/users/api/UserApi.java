package com.juliherms.agendamento.pets.users.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public interface UserApi {

    record CreateUserRequest(
            @NotBlank String nome,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^\\+\\d{6,15}$") String telefone,
            @NotBlank String endereco,
            @NotBlank @Size(min = 8, max = 100) String senha,
            @NotNull Perfil perfil,
            @NotNull CanalVerificacao preferenciaVerificacao
    ) {}

    record UserResponse(
            Long id,
            String nome,
            String email,
            String telefone,
            String endereco,
            Perfil perfil,
            Status status,
            boolean verified_email,
            boolean verified_phone,
            Instant createdAt
    ) {}

    record VerifyRequest(@NotBlank String token, @NotNull CanalVerificacao canal) {}

    enum Perfil { ADMIN, CLIENTE, PROVEDOR }
    enum Status { pendente_verificacao, ativo, inativo }
    enum CanalVerificacao { EMAIL, SMS }
}


