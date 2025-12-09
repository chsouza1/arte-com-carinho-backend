package com.artecomcarinho.service;

import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import static com.artecomcarinho.model.Order.OrderStatus.IN_PRODUCTION;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender; //email
    //private final WhatsAppClient whatsAppClient;
    // private final TelegramClient telegramClient;

    @Value("${notifications.email.from:no-reply@artecomcarinho.com}")
    private String fromEmail;

    public void notifyOrderStatusChange(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        Customer customer = order.getCustomer();
        if (customer == null) return;

        String message = buildMessage(order, newStatus);

        // E-mail
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            sendEmail(customer.getEmail(), "Atualização do pedido #" + order.getId(), message);
        }

        // WhatsApp (futuro)
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            // sendWhatsApp(customer.getPhone(), message);
        }

        // Telegram (futuro)
        // if (customer.getTelegramChatId() != null) {
        //     sendTelegram(customer.getTelegramChatId(), message);
        // }
    }

    private String buildMessage(Order order, Order.OrderStatus newStatus) {
        return switch (newStatus) {
            case IN_PRODUCTION -> String.format(
                    "Oi! Seu pedido #%d entrou em PRODUÇÃO 💖\n\n" +
                            "Assim que estiver pronto para envio, te avisamos novamente.",
                    order.getId()
            );
            case SHIPPED -> String.format(
                    "Boa notícia! Seu pedido #%d já foi ENVIADO 📦\n\n" +
                            "Em breve ele deve chegar até você.",
                    order.getId()
            );
            default -> String.format("Status do pedido #%d atualizado para %s.", order.getId(), newStatus);
        };
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
