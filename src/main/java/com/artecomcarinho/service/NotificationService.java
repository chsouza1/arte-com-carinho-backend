package com.artecomcarinho.service;

import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Order.OrderStatus;
import com.artecomcarinho.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    /* =======================
       NOTIFICAÇÃO DE PEDIDO
       ======================= */

    public void notifyOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (order == null || order.getCustomer() == null) {
            return;
        }

        Customer customer = order.getCustomer();
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            return;
        }

        String subject;
        String htmlBody;

        switch (newStatus) {

            case IN_PRODUCTION -> {
                subject = "Seu pedido entrou em produção";
                htmlBody = buildBaseTemplate("""
                    <h2>Ótimas notícias, %s!</h2>
                    <p>O pedido <strong>#%s</strong> entrou em produção.</p>
                    <p>Estamos preparando tudo com muito carinho 💕</p>
                    <p style="text-align:center;">
                        <a href="https://artecomcarinhobysi.com.br/account/orders"
                           style="background:#e11d48;color:#fff;padding:12px 20px;
                                  text-decoration:none;border-radius:6px;font-weight:600;">
                           Acompanhar pedido
                        </a>
                    </p>
                """, customer.getName(), order.getOrderNumber());
            }

            case SHIPPED -> {
                subject = "Seu pedido saiu para entrega";
                htmlBody = buildBaseTemplate("""
                    <h2>Prepare o coração 📦</h2>
                    <p>O pedido <strong>#%s</strong> já foi enviado.</p>
                    <p>Em breve ele chegará até você.</p>
                    <p style="text-align:center;">
                        <a href="https://artecomcarinhobysi.com.br/account/orders"
                           style="background:#e11d48;color:#fff;padding:12px 20px;
                                  text-decoration:none;border-radius:6px;font-weight:600;">
                           Ver envio
                        </a>
                    </p>
                """, order.getOrderNumber());
            }

            case DELIVERED -> {
                subject = "Seu pedido foi entregue";
                htmlBody = buildBaseTemplate("""
                    <h2>Pedido entregue 💝</h2>
                    <p>Olá, %s!</p>
                    <p>Confirmamos a entrega do pedido <strong>#%s</strong>.</p>
                    <p>Esperamos que você ame sua peça!</p>
                """, customer.getName(), order.getOrderNumber());
            }

            default -> {
                return;
            }
        }

        try {
            sendHtmlEmail(customer.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail do pedido {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }

    /* =======================
       RESET DE SENHA
       ======================= */

    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl =
                "https://artecomcarinhobysi.com.br/auth/reset-password?token=" + token;

        String subject = "Redefinição de senha – Arte com Carinho";

        String htmlBody = buildBaseTemplate("""
            <h2>Olá, %s</h2>
            <p>Recebemos uma solicitação para redefinir sua senha.</p>
            <p style="text-align:center;">
                <a href="%s"
                   style="background:#e11d48;color:#fff;padding:12px 20px;
                          text-decoration:none;border-radius:6px;font-weight:600;">
                   Redefinir senha
                </a>
            </p>
            <p style="font-size:13px;color:#6b7280;">
                Se você não solicitou esta ação, ignore este e-mail.
            </p>
        """, user.getName(), resetUrl);

        try {
            sendHtmlEmail(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Erro ao enviar reset de senha: {}", e.getMessage());
        }
    }

    /* =======================
       ENVIO DE E-MAIL
       ======================= */

    private void sendHtmlEmail(String to, String subject, String htmlBody)
            throws MessagingException, java.io.UnsupportedEncodingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(mimeMessage, true, "UTF-8");

        InternetAddress from =
                new InternetAddress("contato@artecomcarinhobysi.com.br", "Arte com Carinho");

        helper.setFrom(from);
        helper.setReplyTo(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(stripHtml(htmlBody), htmlBody);

        mailSender.send(mimeMessage);
    }

    /* =======================
       TEMPLATE BASE
       ======================= */

    private String buildBaseTemplate(String body, Object... args) {
        return String.format("""
        <div style="font-family:Arial,sans-serif;background:#f9fafb;padding:24px;">
          <div style="max-width:520px;margin:0 auto;background:#ffffff;
                      padding:32px;border-radius:8px;">
            <h1>Arte com Carinho</h1>

            %s

            <hr style="margin:32px 0;border:none;border-top:1px solid #e5e7eb;">

            <p style="font-size:12px;color:#6b7280;line-height:1.5;">
              Você está recebendo este e-mail porque realizou uma ação em nosso site.<br>
              Arte com Carinho<br>
              <a href="https://artecomcarinhobysi.com.br" style="color:#6b7280;">
                https://artecomcarinhobysi.com.br
              </a>
            </p>
          </div>
        </div>
        """, String.format(body, args));
    }

    /* =======================
       TEXTO PURO
       ======================= */

    private String stripHtml(String html) {
        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("<[^>]*>", "")
                .replaceAll("&nbsp;", " ")
                .trim();
    }
}
