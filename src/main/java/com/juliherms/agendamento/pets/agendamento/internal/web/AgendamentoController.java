package com.juliherms.agendamento.pets.agendamento.internal.web;

import com.juliherms.agendamento.pets.agendamento.api.AgendamentoApi;
import com.juliherms.agendamento.pets.agendamento.internal.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gerenciar agendamentos.
 * Permite criar agendamentos e consultar histórico.
 */
@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Agendamentos", description = "Endpoints para criação e consulta de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Cria um novo agendamento.
     * 
     * @param request Dados do agendamento a ser criado
     * @return Response com o agendamento criado
     */
    @PostMapping
    @Operation(summary = "Cria um novo agendamento", 
               description = "Cria agendamento validando disponibilidade, horário comercial e regras de negócio")
    public ResponseEntity<AgendamentoApi.AgendamentoResponse> criarAgendamento(
            @Valid @RequestBody AgendamentoApi.CreateAgendamentoRequest request) {
        
        var response = agendamentoService.criarAgendamento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca agendamentos por usuário (tutor do pet).
     * 
     * @param usuarioId ID do usuário
     * @return Lista de agendamentos do usuário
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Busca agendamentos por usuário", 
               description = "Retorna todos os agendamentos de pets pertencentes ao usuário")
    public ResponseEntity<List<AgendamentoApi.AgendamentoResponse>> buscarPorUsuario(
            @Parameter(description = "ID do usuário") 
            @PathVariable Long usuarioId) {
        
        var agendamentos = agendamentoService.buscarAgendamentosPorUsuario(usuarioId);
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Busca agendamentos por pet.
     * 
     * @param petId ID do pet
     * @return Lista de agendamentos do pet
     */
    @GetMapping("/pet/{petId}")
    @Operation(summary = "Busca agendamentos por pet", 
               description = "Retorna todos os agendamentos de um pet específico")
    public ResponseEntity<List<AgendamentoApi.AgendamentoResponse>> buscarPorPet(
            @Parameter(description = "ID do pet") 
            @PathVariable Long petId) {
        
        var agendamentos = agendamentoService.buscarAgendamentosPorPet(petId);
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Endpoint alternativo para criar agendamento via usuário.
     * 
     * @param usuarioId ID do usuário que está fazendo o agendamento
     * @param request Dados do agendamento
     * @return Response com o agendamento criado
     */
    @PostMapping("/usuario/{usuarioId}")
    @Operation(summary = "Cria agendamento via usuário", 
               description = "Cria agendamento associado a um usuário específico")
    public ResponseEntity<AgendamentoApi.AgendamentoResponse> criarAgendamentoViaUsuario(
            @Parameter(description = "ID do usuário") 
            @PathVariable Long usuarioId,
            @Valid @RequestBody AgendamentoApi.CreateAgendamentoRequest request) {
        
        // Aqui você pode adicionar validação adicional se necessário
        // Por exemplo, verificar se o pet pertence ao usuário
        
        var response = agendamentoService.criarAgendamento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
