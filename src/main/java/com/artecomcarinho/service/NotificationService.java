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
                  <html lang="en"><body>
                    <h2 style="color:#9f1239;margin-top:0;font-size:22px;">
                        Seu pedido está em produção 🎀
                    </h2>
                    
                    <p style="color:#525252;line-height:1.7;font-size:15px;">
                        O pedido <strong>#%s</strong> já entrou em produção.
                    </p>
                    
                    <p style="color:#525252;line-height:1.7;font-size:15px;">
                        Estamos preparando tudo com muito carinho e atenção aos detalhes 💗
                    </p>
                  </body></html>
                """, customer.getName(), order.getOrderNumber());
            }

            case SHIPPED -> {
                subject = "Seu pedido saiu para entrega";
                htmlBody = buildBaseTemplate("""
                    <html lang="en"><body><h2 style="color:#9f1239;margin-top:0;font-size:22px;">
                                Seu pedido foi enviado 📦
                                </h2>
                        
                                <p style="color:#525252;line-height:1.7;font-size:15px;">
                                O pedido <strong>#%s</strong> já saiu para entrega.
                                </p>
                        
                                <p style="color:#525252;line-height:1.7;font-size:15px;">
                                Em breve ele estará com você ✨
                                </p>
                    </body></html>
                """, order.getOrderNumber());
            }

            case DELIVERED -> {
                subject = "Seu pedido foi entregue";
                htmlBody = buildBaseTemplate("""
                    <html lang="en"><body><h2 style="color:#9f1239;margin-top:0;font-size:22px;">
                        Pedido entregue 💝
                        </h2>
                       
                        <p style="color:#525252;line-height:1.7;font-size:15px;">
                        Confirmamos a entrega do pedido
                        <strong>#%s</strong>.
                        </p>
                        
                        <p style="color:#525252;line-height:1.7;font-size:15px;">
                        Esperamos que você ame sua peça tanto quanto nós amamos produzir 💕
                        </p>
                    </body></html>
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
        String resetUrl = "https://artecomcarinhobysi.com.br/auth/reset-password?token=" + token;
        String subject = "Redefinição de senha – Arte com Carinho 💕";

        String htmlBody = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:linear-gradient(180deg,#fff1f2,#ffe4e6);font-family:'Segoe UI',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table width="100%%" style="max-width:600px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 10px 30px rgba(190,18,60,0.08);">
                            <tr>
                                <td align="center" style="padding:40px 30px 20px 30px;">
                                    <h1 style="margin:0;font-size:28px;color:#be123c;font-weight:700;letter-spacing:0.5px;">Arte com Carinho</h1>
                                    <p style="margin:8px 0 0 0;color:#f43f5e;font-size:14px;">Peças feitas com amor 💕</p>
                                </td>
                            </tr>
                            <tr>
                                <td align="center"><div style="width:60px;height:4px;background:#fda4af;border-radius:10px;"></div></td>
                            </tr>
                            <tr>
                                <td style="padding:40px 40px 30px 40px;">
                                    <h2 style="color:#9f1239;margin-top:0;font-size:22px;">Olá %s,</h2>
                                    <p style="color:#525252;line-height:1.7;font-size:15px;">
                                        Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para continuar.
                                    </p>
                                    <div style="text-align:center;margin:35px 0;">
                                        <a href="%s" style="background:linear-gradient(90deg,#fb7185,#e11d48);color:#ffffff;padding:16px 34px;text-decoration:none;border-radius:50px;font-weight:600;font-size:15px;display:inline-block;box-shadow:0 8px 18px rgba(225,29,72,0.25);">
                                            Redefinir senha
                                        </a>
                                    </div>
                                    <p style="color:#737373;font-size:13px;line-height:1.6;">Se você não solicitou esta alteração, pode ignorar este e-mail.</p>
                                </td>
                            </tr>
                            <tr>
                                <td style="background:#fff1f2;padding:25px;text-align:center;font-size:12px;color:#881337;">
                                    Você está recebendo este e-mail porque realizou uma ação em nosso site.<br><br>
                                    <strong>Arte com Carinho</strong><br>
                                    <a href="https://artecomcarinhobysi.com.br" style="color:#be123c;text-decoration:none;">artecomcarinhobysi.com.br</a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """, user.getName(), resetUrl);

        try {
            sendHtmlEmail(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Erro ao enviar reset de senha para {}: {}", user.getEmail(), e.getMessage());
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
        String content = String.format(body, args);

        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin:0;padding:0;background:linear-gradient(180deg,#fff1f2,#ffe4e6);font-family:'Segoe UI',Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table width="100%%" style="max-width:600px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 10px 30px rgba(190,18,60,0.08);">
                            
                            <tr>
                                <td align="center" style="padding:40px 30px 20px 30px;">
                                    <h1 style="margin:0;font-size:28px;color:#be123c;font-weight:700;letter-spacing:0.5px;">
                                        Arte com Carinho
                                    </h1>
                                    <p style="margin:8px 0 0 0;color:#f43f5e;font-size:14px;">
                                        Peças feitas com amor 💕
                                    </p>
                                </td>
                            </tr>

                            <tr>
                                <td align="center">
                                    <div style="width:60px;height:4px;background:#fda4af;border-radius:10px;"></div>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:40px 40px 30px 40px; color:#525252; line-height:1.7; font-size:15px;">
                                    %s
                                </td>
                            </tr>

                            <tr>
                                <td style="background:#fff1f2;padding:25px;text-align:center;font-size:12px;color:#881337;">
                                    Você está recebendo este e-mail porque realizou uma ação em nosso site.<br><br>
                                    <strong>Arte com Carinho</strong><br>
                                    <a href="https://artecomcarinhobysi.com.br" style="color:#be123c;text-decoration:none;font-weight:600;">
                                        artecomcarinhobysi.com.br
                                    </a>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """, content);
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
