package com.artecomcarinho.security;

import com.artecomcarinho.exception.UnauthorizedException;
import com.artecomcarinho.model.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public void ensureAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new UnauthorizedException("Acesso restrito a administradores");
        }
    }

    public void ensureOrderAccess(Authentication authentication, Order order) {
        if (isAdmin(authentication)) {
            return;
        }

        if (authentication == null || order == null || order.getCustomer() == null) {
            throw new UnauthorizedException("Voce nao tem permissao para acessar este pedido");
        }

        String customerEmail = order.getCustomer().getEmail();
        String authenticatedEmail = authentication.getName();

        if (customerEmail == null || authenticatedEmail == null || !customerEmail.equalsIgnoreCase(authenticatedEmail)) {
            throw new UnauthorizedException("Voce nao tem permissao para acessar este pedido");
        }
    }
}
