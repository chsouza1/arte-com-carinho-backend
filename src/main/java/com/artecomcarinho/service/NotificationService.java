package com.artecomcarinho.service;

import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Order.OrderStatus;
import com.artecomcarinho.model.User;
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

    @Value("${app.mail.from:atendimento@artecomcarinhobysi.com.br}")
    private String defaultFrom;

    @Value("${app.mail.from-name:Atendimento Arte Com Carinho}")
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
            <div style="font-family: sans-serif; background-color: #fff5f5; padding: 40px 20px; text-align: center;">
                <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; padding: 40px; border: 2px solid #ffe4e6;">
                    <h1 style="color: #e11d48; margin: 0; font-size: 24px; font-weight: 900;">Arte com Carinho</h1>
                    <div style="height: 4px; width: 40px; background: #fb7185; margin: 8px auto; border-radius: 2px;"></div>
                    
                    <h2 style="color: #334155; font-size: 20px; margin-top: 24px;">Ótimas notícias, %s!</h2>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6;">
                        O seu pedido <strong>#%s</strong> já está nas mãos da nossa equipe e entrou em produção! 💖
                    </p>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin-bottom: 30px;">
                        Estamos preparando cada detalhe com muito amor e carinho. Assim que ele estiver pronto e sair para entrega, avisaremos você!
                    </p>
                    
                    <a href="https://artecomcarinhobysi.com.br/account/orders" style="display: inline-block; background: #f43f5e; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 12px; font-weight: bold;">
                        Acompanhar meu pedido
                    </a>
                </div>
            </div>
            """, customer.getName(), order.getOrderNumber());
            }
            case SHIPPED -> {
                subject = "Seu pedido saiu para entrega 📦✨";
                htmlBody = String.format("""
            <div style="font-family: sans-serif; background-color: #fff5f5; padding: 40px 20px; text-align: center;">
                <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; padding: 40px; border: 2px solid #ffe4e6;">
                    <h1 style="color: #e11d48; margin: 0; font-size: 24px; font-weight: 900;">Arte com Carinho</h1>
                    <div style="height: 4px; width: 40px; background: #fb7185; margin: 8px auto; border-radius: 2px;"></div>
                    
                    <h2 style="color: #334155; font-size: 20px; margin-top: 24px;">Prepare o coração! 📦</h2>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6;">
                        O seu pedido <strong>#%s</strong> já saiu do nosso ateliê e está a caminho de você!
                    </p>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin-bottom: 30px;">
                        Em breve ele chegará para deixar seu dia mais especial. ✨
                    </p>
                    
                    <a href="https://artecomcarinhobysi.com.br/account/orders" style="display: inline-block; background: #f43f5e; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 12px; font-weight: bold;">
                        Ver detalhes do envio
                    </a>
                </div>
            </div>
            """, customer.getName(), order.getOrderNumber());
            }
            case DELIVERED -> {
                subject = "Seu pedido foi entregue 💝";
                htmlBody = String.format("""
            <div style="font-family: sans-serif; background-color: #fff5f5; padding: 40px 20px; text-align: center;">
                <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; padding: 40px; border: 2px solid #ffe4e6;">
                    <h1 style="color: #e11d48; margin: 0; font-size: 24px; font-weight: 900;">Arte com Carinho</h1>
                    <div style="height: 4px; width: 40px; background: #fb7185; margin: 8px auto; border-radius: 2px;"></div>
                    
                    <h2 style="color: #334155; font-size: 20px; margin-top: 24px;">Entregue com amor! 💝</h2>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6;">
                        Olá, %s! Consta em nosso sistema que o pedido <strong>#%s</strong> foi entregue.
                    </p>
                    <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin-bottom: 30px;">
                        Esperamos que você ame a sua nova peça! Se puder, adoraríamos receber seu feedback ou ver uma foto marcada no nosso Instagram. 😊
                    </p>
                    
                    <a href="https://wa.me/SEU_NUMERO_WHATSAPP" style="display: inline-block; background: #25d366; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 12px; font-weight: bold;">
                        Enviar feedback no WhatsApp
                    </a>
                </div>
            </div>
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

    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = "https://artecomcarinhobysi.com.br/auth/reset-password?token=" + token;
        String subject = "Recuperação de Senha - Arte com Carinho 💕";

        String htmlBody = String.format("""
        <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #fff5f5; padding: 40px 20px; text-align: center;">
            <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; padding: 40px; border: 2px solid #ffe4e6; shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);">
                
                <div style="margin-bottom: 24px;">
                    <h1 style="color: #e11d48; margin: 0; font-size: 24px; font-weight: 900;">Arte com Carinho</h1>
                    <div style="height: 4px; width: 40px; background: linear-gradient(to right, #f43f5e, #fb7185); margin: 8px auto; border-radius: 2px;"></div>
                </div>

                <h2 style="color: #334155; font-size: 20px; margin-bottom: 16px;">Olá, %s!</h2>
                
                <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin-bottom: 32px;">
                    Recebemos uma solicitação para redefinir a sua senha. Não se preocupe, clique no botão abaixo para escolher uma nova:
                </p>

                <a href="%s" style="display: inline-block; background: linear-gradient(to right, #f43f5e, #ec4899); color: #ffffff; padding: 16px 32px; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 6px rgba(244, 63, 94, 0.3);">
                    Redefinir minha senha
                </a>

                <p style="color: #94a3b8; font-size: 13px; margin-top: 32px; padding-top: 24px; border-top: 1px solid #f1f5f9;">
                    Se você não solicitou esta alteração, pode ignorar este e-mail com segurança.<br>
                    Este link expirará em breve.
                </p>
            </div>
            
            <p style="color: #94a3b8; font-size: 12px; margin-top: 20px;">
                &copy; 2024 Arte com Carinho - Feito com amor por Si
            </p>
        </div>
        """, user.getName(), resetUrl);

        try {
            sendHtmlEmail(user.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de redefinição de senha para {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
