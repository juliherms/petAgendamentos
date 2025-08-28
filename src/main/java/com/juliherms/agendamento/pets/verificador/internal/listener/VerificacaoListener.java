package com.juliherms.agendamento.pets.verificador.internal.listener;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.api.UserCreatedEvent;
import com.juliherms.agendamento.pets.verificador.internal.exception.VerificadorExceptionHandler;
import com.juliherms.agendamento.pets.verificador.internal.service.EmailService;
import com.juliherms.agendamento.pets.verificador.internal.service.SMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que processa eventos de criação de usuário e dispara
 * o envio de verificação via e-mail ou SMS conforme preferência do usuário.
 */
@Component
public class VerificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(VerificacaoListener.class);

    private final EmailService emailService;
    private final SMSService smsService;

    public VerificacaoListener(@Qualifier("emailServiceMailTrap") EmailService emailService, SMSService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Processa o evento de criação de usuário e envia a verificação
     * pelo canal preferido (e-mail ou SMS).
     * 
     * @param event Evento de criação de usuário
     */
    @EventListener
    @Async
    public void onUserCreated(UserCreatedEvent event) {
        try {
            log.info("[VERIFICADOR] Processando verificação para usuário {} via canal {}", 
                    event.userId(), event.canalPreferido());

            switch (event.canalPreferido()) {
                case EMAIL -> processarVerificacaoEmail(event);
                case SMS -> processarVerificacaoSMS(event);
                default -> throw new VerificadorExceptionHandler.CanalNaoSuportadoException(
                    "Canal de verificação não suportado: " + event.canalPreferido()
                );
            }

            log.info("[VERIFICADOR] Verificação processada com sucesso para usuário {}", event.userId());

        } catch (Exception e) {
            log.error("[VERIFICADOR] Erro ao processar verificação para usuário {}: {}", 
                    event.userId(), e.getMessage(), e);
            
            // Em um cenário real, você poderia:
            // 1. Registrar a falha para retry posterior
            // 2. Publicar um evento de falha
            // 3. Notificar sistemas de monitoramento
            throw new RuntimeException("Falha no processamento de verificação", e);
        }
    }

    private void processarVerificacaoEmail(UserCreatedEvent event) {
        log.info("[VERIFICADOR] Enviando verificação por e-mail para: {}", event.email());
        
        boolean sucesso = emailService.enviarEmailVerificacao(
            event.email(),
            event.tokenVerificacao(),
            event.expiresAt()
        );
        
        if (sucesso) {
            log.info("[VERIFICADOR] E-mail de verificação enviado com sucesso para: {}", event.email());
        }
    }

    private void processarVerificacaoSMS(UserCreatedEvent event) {
        log.info("[VERIFICADOR] Enviando verificação por SMS para: {}", event.telefone());
        
        boolean sucesso = smsService.enviarSMSVerificacao(
            event.telefone(),
            event.tokenVerificacao(),
            event.expiresAt()
        );
        
        if (sucesso) {
            log.info("[VERIFICADOR] SMS de verificação enviado com sucesso para: {}", event.telefone());
        }
    }
}
