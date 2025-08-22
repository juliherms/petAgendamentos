package com.juliherms.agendamento.pets.users.internal.web;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Controlador REST para gerenciar usuários.
 * Fornece endpoints para criar usuários e verificar usuários usando um token de verificação.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para cadastro e verificação de usuários")
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    /** Endpoint para criar um novo usuário.
     *
     * @param req Requisição contendo os dados do usuário a ser criado.
     * @return Resposta HTTP com o status e o corpo apropriados.
     */
    @PostMapping
    @Operation(summary = "Cria um novo usuário", description = "Cria usuário e envia token de verificação via evento")
    public ResponseEntity<?> criar(@Valid @RequestBody UserApi.CreateUserRequest req) {
        try {
            var resp = service.criar(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (UserService.ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("email já cadastrado"));
        }
    }

    /** Endpoint para verificar um usuário usando um token de verificação.
     *
     * @param id ID do usuário a ser verificado.
     * @param req Requisição contendo o token de verificação e o canal.
     * @return Resposta HTTP com o status e o corpo apropriados.
     */
    @PostMapping("/{id}/verificar")
    @Operation(summary = "Verifica contato do usuário", description = "Valida token e ativa a conta")
    public ResponseEntity<?> verify(@PathVariable("id") Long id, @Valid @RequestBody UserApi.VerifyRequest req) {
        try {
            var resp = service.verificar(id, req);
            return ResponseEntity.ok(resp);
        } catch (UserService.NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (UserService.ValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}


