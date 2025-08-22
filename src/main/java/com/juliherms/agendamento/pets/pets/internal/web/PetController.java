package com.juliherms.agendamento.pets.pets.internal.web;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import com.juliherms.agendamento.pets.pets.internal.domain.Pet;
import com.juliherms.agendamento.pets.pets.internal.repo.PetRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/** * Controlador REST para gerenciar pets associados a usuários.
 * Fornece um endpoint para criar um novo pet.
 */
@RestController
@RequestMapping("/users/{idUsuario}/pets")
@Tag(name = "Pets", description = "Endpoints para cadastro de pets do cliente")
class PetController {

    private final PetRepository pets;
    private final UserRepository users;

    PetController(PetRepository pets, UserRepository users) {
        this.pets = pets;
        this.users = users;
    }

    record CreatePetRequest(@NotBlank String nome, @NotNull Integer idade, @NotBlank String raca, @NotNull Double peso) {}

    /**
     * Endpoint para criar um novo pet associado a um usuário.
     *
     * @param idUsuario ID do usuário ao qual o pet será associado.
     * @param req Requisição contendo os dados do pet a ser criado.
     * @return Resposta HTTP com o status e o corpo apropriados.
     */
    @PostMapping
    @Operation(summary = "Cadastra um novo pet", description = "Somente para usuários com perfil CLIENTE e conta ativa")
    public ResponseEntity<?> create(@PathVariable Long idUsuario, @Valid @RequestBody CreatePetRequest req) {

        // pega o usuário pelo ID
        var user = users.findById(idUsuario).orElse(null);

       // verifica se o usuário existe e se está ativo e é do perfil CLIENTE
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("usuario não encontrado"));
        if (user.getStatus() != UserApi.Status.ativo) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("conta não ativa"));
        if (user.getPerfil() != UserApi.Perfil.CLIENTE) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ação não permitida para o perfil"));

        // cria o pet e salva no repositório
        Pet pet = new Pet();
        pet.setUsuarioId(idUsuario);
        pet.setNome(req.nome());
        pet.setIdade(req.idade());
        pet.setRaca(req.raca());
        pet.setPeso(req.peso());
        var saved = pets.save(pet);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    record ErrorResponse(String message) {}
}


