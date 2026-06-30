package com.epfafrica.cocovoit.dto;

import com.epfafrica.cocovoit.entity.Utilisateur;

public record UtilisateurResponse(
    Long id,
    String username,
    String name,
    String firstname,
    String email,
    String role
) {
    public static UtilisateurResponse from(Utilisateur u) {
        return new UtilisateurResponse(
        u.getId(), 
        u.getUsername(),
        u.getName(),
        u.getFirstname(),
        u.getEmail(),
        u.getRole().name()
    );
}
}