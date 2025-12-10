package com.artecomcarinho.service;

import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Order.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por disparar notificações quando o pedido muda de status.
 * Aqui já envia e-mail de verdade + faz LOG para debug.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@artecomcarinho.com}")
    private String defaultFrom;

    @Value("${app.mail.from-name:Arte com Carinho}")
    private String defaultFromName;

    public void notifyOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (order == null) {
            return;
        }

        Customer customer = order.getCustomer();
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            log.warn("Pedido {} mudou de status {} -> {}, mas o cliente não possui e-mail cadastrado.",
                    order.getOrderNumber(), oldStatus, newStatus);
            return;
        }

        String subject;
        String htmlBody;

        switch (newStatus) {
            case IN_PRODUCTION -> {
                subject = "Seu pedido entrou em produção 💕";
                htmlBody = String.format("""
                        <p>Olá, %s!</p>
                        <p>Seu pedido <strong>%s</strong> entrou em produção. Estamos preparando tudo com carinho! 💖</p>
                        <p>Assim que ele sair para entrega, você receberá uma nova mensagem.</p>
                        <p>Atenciosamente,<br/>Equipe Arte com Carinho</p>
                        """, customer.getName(), order.getOrderNumber());
            }
            case SHIPPED -> {
                subject = "Seu pedido saiu para entrega 📦";
                htmlBody = String.format("""
                        <p>Olá, %s!</p>
                        <p>Seu pedido <strong>%s</strong> já saiu para entrega! 📦✨</p>
                        <p>Em breve ele deve chegar até você.</p>
                        <p>Atenciosamente,<br/>Equipe Arte com Carinho</p>
                        """, customer.getName(), order.getOrderNumber());
            }
            // Se quiser notificar entrega também, é só descomentar:
             case DELIVERED -> {
                 subject = "Seu pedido foi entregue 💝";
                 htmlBody = String.format("""
                         <p>Olá, %s!</p>
                         <p>Seu pedido <strong>%s</strong> foi entregue.</p>
                         <p>Esperamos que você goste muito da sua peça! Qualquer feedback é muito bem-vindo 😊</p>
                         <p>Atenciosamente,<br/>Equipe Arte com Carinho</p>
                         """, customer.getName(), order.getOrderNumber());
             }
            default -> {
                return;
            }
        }

        try {
            sendHtmlEmail(customer.getEmail(), subject, htmlBody);
            log.info("E-mail de notificação enviado para {} sobre pedido {} ({} -> {}).",
                    customer.getEmail(), order.getOrderNumber(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de notificação para {} sobre pedido {}: {}",
                    customer.getEmail(), order.getOrderNumber(), e.getMessage(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // true = multipart
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        String fromPersonal = String.format("\"%s\" <%s>", defaultFromName, defaultFrom);

        helper.setFrom(fromPersonal);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        mailSender.send(mimeMessage);
    }
}
