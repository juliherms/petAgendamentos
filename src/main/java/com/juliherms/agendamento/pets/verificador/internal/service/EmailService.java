package com.juliherms.agendamento.pets.verificador.internal.service;

import java.time.Instant;
import java.util.Map;

/**
 * Interface para serviços de envio de e-mails.
 * Define os métodos padrão para envio de e-mails de verificação.
 */
public interface EmailService {

    /**
     * Envia um e-mail de verificação.
     * 
     * @param destinatario Endereço de e-mail do destinatário
     * @param token Token de verificação
     * @param expiresAt Data de expiração do token
     * @return true se o envio foi realizado com sucesso
     */
    boolean enviarEmailVerificacao(String destinatario, String token, Instant expiresAt);

    /**
     * Envia um e-mail de texto simples.
     * 
     * @param from Endereço de e-mail do remetente
     * @param to Endereço de e-mail do destinatário
     * @param subject Assunto do e-mail
     * @param body Corpo do e-mail em texto simples
     */
    void enviarEmailTexto(String from, String to, String subject, String body);

    /**
     * Envia um e-mail HTML com anexos opcionais.
     * 
     * @param from Endereço de e-mail do remetente
     * @param to Endereço de e-mail do destinatário
     * @param subject Assunto do e-mail
     * @param html Corpo do e-mail em HTML
     * @param anexos Mapa de anexos (nome do arquivo -> conteúdo em bytes)
     * @throws Exception se houver erro no envio
     */
    void enviarEmailHtml(String from, String to, String subject, String html, Map<String, byte[]> anexos) throws Exception;
}
