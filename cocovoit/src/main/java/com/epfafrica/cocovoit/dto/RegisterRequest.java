package com.epfafrica.cocovoit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Le nom d'utilisateur(username) est obligatoire")
    String username,

    @NotBlank(message = "Le mot de passe est obligatoire")
    String password,

    @NotBlank(message = "Le nom est obligatoire")
    String name,

    @NotBlank(message ="Le prenom est obligatoire")
    String firstname,

    @NotBlank(message = "l'Email est obligatoire")
    @Email(message = "L'email doit etre valide")
    String email

) {
    
}
