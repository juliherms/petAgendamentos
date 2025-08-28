package com.juliherms.agendamento.pets.verificador.internal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Implementação do serviço de envio de e-mails usando Mailtrap.
 * Envia e-mails reais através do serviço SMTP do Mailtrap.
 */
@Service
@Qualifier("emailServiceMailTrap")
public class EmailServiceMailTrapImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceMailTrapImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:no-reply@pets.dev}")
    private String fromAddress;

    public EmailServiceMailTrapImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @PostConstruct
    public void testConnection() {
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                ((JavaMailSenderImpl) mailSender).testConnection();
                log.info("[MAILTRAP] Conexão SMTP testada com sucesso");
            }
        } catch (Exception e) {
            log.error("[MAILTRAP] Falha ao testar conexão SMTP: {}", e.getMessage());
            // Em produção, você pode querer falhar a inicialização da aplicação
            // throw new RuntimeException("Falha na conexão SMTP", e);
        }
    }

    @Override
    public boolean enviarEmailVerificacao(String destinatario, String token, Instant expiresAt) {
        try {
            log.info("[MAILTRAP] Enviando e-mail de verificação para: {}", destinatario);
            
            String assunto = "Verificação de Conta - Pets API";
            String html = renderizarTemplateVerificacao(destinatario, token, expiresAt);
            
            enviarEmailHtml(fromAddress, destinatario, assunto, html, null);
            
            log.info("[MAILTRAP] E-mail de verificação enviado com sucesso para: {}", destinatario);
            return true;
            
        } catch (Exception e) {
            log.error("[MAILTRAP] Erro ao enviar e-mail de verificação para {}: {}", destinatario, e.getMessage());
            return false;
        }
    }

    @Override
    public void enviarEmailTexto(String from, String to, String subject, String body) {
        try {
            log.info("[MAILTRAP] Enviando e-mail de texto para: {}", to);
            
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            
            mailSender.send(msg);
            
            log.info("[MAILTRAP] E-mail de texto enviado com sucesso para: {}", to);
            
        } catch (MailAuthenticationException e) {
            log.error("[MAILTRAP] Erro de autenticação SMTP: {}", e.getMessage());
            throw new RuntimeException("Falha de autenticação SMTP", e);
        } catch (MailSendException e) {
            log.error("[MAILTRAP] Erro no envio SMTP: {}", e.getMessage());
            throw new RuntimeException("Falha no envio SMTP", e);
        } catch (Exception e) {
            log.error("[MAILTRAP] Erro inesperado ao enviar e-mail: {}", e.getMessage());
            throw new RuntimeException("Erro inesperado no envio de e-mail", e);
        }
    }

    @Override
    public void enviarEmailHtml(String from, String to, String subject, String html, Map<String, byte[]> anexos) throws Exception {
        try {
            log.info("[MAILTRAP] Enviando e-mail HTML para: {}", to);
            
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            
            if (anexos != null && !anexos.isEmpty()) {
                for (Map.Entry<String, byte[]> entry : anexos.entrySet()) {
                    helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                }
                log.info("[MAILTRAP] Adicionados {} anexo(s) ao e-mail", anexos.size());
            }
            
            mailSender.send(mime);
            
            log.info("[MAILTRAP] E-mail HTML enviado com sucesso para: {}", to);
            
        } catch (MailAuthenticationException e) {
            log.error("[MAILTRAP] Erro de autenticação SMTP: {}", e.getMessage());
            throw new RuntimeException("Falha de autenticação SMTP", e);
        } catch (MailSendException e) {
            log.error("[MAILTRAP] Erro no envio SMTP: {}", e.getMessage());
            throw new RuntimeException("Falha no envio SMTP", e);
        } catch (Exception e) {
            log.error("[MAILTRAP] Erro inesperado ao enviar e-mail HTML: {}", e.getMessage());
            throw new RuntimeException("Erro inesperado no envio de e-mail HTML", e);
        }
    }
    
    private String renderizarTemplateVerificacao(String destinatario, String token, Instant expiresAt) {
        try {
            long horasRestantes = Duration.between(Instant.now(), expiresAt).toHours();
            
            Context context = new Context();
            context.setVariable("destinatario", destinatario);
            context.setVariable("token", token);
            context.setVariable("horasRestantes", horasRestantes);
            context.setVariable("appName", "Pets API");
            
            return templateEngine.process("email/verificacao", context);
            
        } catch (Exception e) {
            log.warn("[MAILTRAP] Falha ao renderizar template, usando HTML padrão: {}", e.getMessage());
            return gerarHtmlPadrao(token, expiresAt);
        }
    }
    
    private String gerarHtmlPadrao(String token, Instant expiresAt) {
        long horasRestantes = Duration.between(Instant.now(), expiresAt).toHours();
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Verificação de Conta</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Verificação de Conta - Pets API</h2>
                    <p>Olá!</p>
                    <p>Você solicitou a verificação da sua conta na Pets API.</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin: 0; color: #495057;">Seu código de verificação:</h3>
                        <p style="font-size: 24px; font-weight: bold; color: #007bff; margin: 10px 0;">%s</p>
                    </div>
                    <p><strong>Este código expira em %d horas.</strong></p>
                    <p>Se você não solicitou esta verificação, ignore este e-mail.</p>
                    <hr style="border: none; border-top: 1px solid #dee2e6; margin: 30px 0;">
                    <p style="color: #6c757d; font-size: 14px;">
                        Atenciosamente,<br>
                        Equipe Pets API
                    </p>
                </div>
            </body>
            </html>
            """, token, horasRestantes);
    }
}
