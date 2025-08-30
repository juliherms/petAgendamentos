package com.juliherms.agendamento.pets.verificador.internal.web;

import com.juliherms.agendamento.pets.verificador.internal.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para testes de envio de e-mails.
 * Disponível apenas em ambiente de desenvolvimento.
 */
@RestController
@RequestMapping("/emails")
@Tag(name = "E-mails", description = "Endpoints para teste de envio de e-mails")
public class EmailController {

    private final EmailService emailServiceSimulado;
    private final EmailService emailServiceMailTrap;

    @Value("${app.mail.from:no-reply@pets.dev}")
    private String fromAddress;

    @Value("${app.email.service.active:emailServiceSimulado}")
    private String activeService;

    public EmailController(
            @Qualifier("emailServiceSimulado") EmailService emailServiceSimulado,
            @Qualifier("emailServiceMailTrap") EmailService emailServiceMailTrap) {
        this.emailServiceSimulado = emailServiceSimulado;
        this.emailServiceMailTrap = emailServiceMailTrap;
    }

    /**
     * Envia um e-mail de teste usando o serviço ativo.
     * 
     * @param to Endereço de e-mail do destinatário
     * @return Resposta de sucesso
     */
    @PostMapping("/test")
    @Operation(summary = "Envia e-mail de teste", description = "Envia um e-mail de teste usando o serviço ativo")
    public ResponseEntity<?> enviarTeste(
            @Parameter(description = "Endereço de e-mail do destinatário") 
            @RequestParam String to) {
        
        String mensagem = "Olá! Este é um e-mail de teste da Pets API.";
        
        if ("emailServiceMailTrap".equals(activeService)) {
            emailServiceMailTrap.enviarEmailTexto(fromAddress, to, "Teste Mailtrap - Pets API", mensagem);
        } else {
            emailServiceSimulado.enviarEmailTexto(fromAddress, to, "Teste Simulado - Pets API", mensagem);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "E-mail de teste enviado com sucesso",
            "to", to,
            "service", activeService
        ));
    }

    /**
     * Envia um e-mail HTML de teste.
     * 
     * @param to Endereço de e-mail do destinatário
     * @return Resposta de sucesso
     */
    @PostMapping("/test/html")
    @Operation(summary = "Envia e-mail HTML de teste", description = "Envia um e-mail HTML de teste usando o serviço ativo")
    public ResponseEntity<?> enviarTesteHtml(
            @Parameter(description = "Endereço de e-mail do destinatário") 
            @RequestParam String to) {
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Teste HTML</title>
            </head>
            <body>
                <h1>Teste de E-mail HTML</h1>
                <p>Este é um <strong>e-mail de teste</strong> da Pets API.</p>
                <p>Data e hora: <em>%s</em></p>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now());
        
        try {
            if ("emailServiceMailTrap".equals(activeService)) {
                emailServiceMailTrap.enviarEmailHtml(fromAddress, to, "Teste HTML Mailtrap - Pets API", html, null);
            } else {
                emailServiceSimulado.enviarEmailHtml(fromAddress, to, "Teste HTML Simulado - Pets API", html, null);
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "E-mail HTML de teste enviado com sucesso",
                "to", to,
                "service", activeService
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Falha ao enviar e-mail HTML",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Retorna informações sobre o serviço de e-mail ativo.
     * 
     * @return Informações do serviço
     */
    @GetMapping("/info")
    @Operation(summary = "Informações do serviço", description = "Retorna informações sobre o serviço de e-mail ativo")
    public ResponseEntity<?> getInfo() {
        return ResponseEntity.ok(Map.of(
            "activeService", activeService,
            "fromAddress", fromAddress,
            "availableServices", new String[]{"emailServiceSimulado", "emailServiceMailTrap"}
        ));
    }
}


