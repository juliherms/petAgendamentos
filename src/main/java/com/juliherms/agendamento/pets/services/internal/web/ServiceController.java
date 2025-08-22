package com.juliherms.agendamento.pets.services.internal.web;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import com.juliherms.agendamento.pets.services.internal.domain.OfferedService;
import com.juliherms.agendamento.pets.services.internal.repo.OfferedServiceRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/** * Controlador REST para gerenciar serviços oferecidos pelos usuários.
 * Permite criar novos serviços, vinculados a um usuário específico.
 */
@RestController
@RequestMapping("/users/{idUsuario}/servicos")
@Tag(name = "Serviços", description = "Endpoints para cadastro de serviços do provedor")
class ServiceController {

    private final OfferedServiceRepository services;
    private final UserRepository users;

    ServiceController(OfferedServiceRepository services, UserRepository users) {
        this.services = services;
        this.users = users;
    }

    record Prices(@NotNull Double p, @NotNull Double m, @NotNull Double g) {}
    record CreateServiceRequest(@NotBlank String titulo, @NotBlank String descricao, @NotNull Prices precosPorPorte, boolean ativo) {}

    /**
     * Cria um novo serviço oferecido por um usuário.
     *
     * @param idUsuario ID do usuário que está oferecendo o serviço.
     * @param req Dados do serviço a ser criado.
     * @return ResponseEntity com o serviço criado ou erro apropriado.
     */
    @PostMapping
    @Operation(summary = "Cadastra um novo serviço", description = "Somente para usuários com perfil PROVEDOR e conta ativa; valida preços > 0")
    public ResponseEntity<?> create(@PathVariable Long idUsuario, @Valid @RequestBody CreateServiceRequest req) {

        // Verifica se o usuário existe e está ativo
        var user = users.findById(idUsuario).orElse(null);

        // Verifica se o usuário é um provedor
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("usuario não encontrado"));
        if (user.getStatus() != UserApi.Status.ativo) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("conta não ativa"));
        if (user.getPerfil() != UserApi.Perfil.PROVEDOR) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ação não permitida para o perfil"));

        // Valida os preços dos serviços
        if (req.precosPorPorte.p() <= 0 || req.precosPorPorte.m() <= 0 || req.precosPorPorte.g() <= 0) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse("preços devem ser > 0"));
        }

        // Cria e salva o serviço oferecido
        OfferedService svc = new OfferedService();
        svc.setUsuarioId(idUsuario);
        svc.setTitulo(req.titulo());
        svc.setDescricao(req.descricao());
        svc.setPrecoP(req.precosPorPorte.p());
        svc.setPrecoM(req.precosPorPorte.m());
        svc.setPrecoG(req.precosPorPorte.g());
        svc.setAtivo(req.ativo());

        var saved = services.save(svc);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    record ErrorResponse(String message) {}
}


