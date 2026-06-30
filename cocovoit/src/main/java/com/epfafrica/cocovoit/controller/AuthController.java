package com.epfafrica.cocovoit.controller;

import com.epfafrica.cocovoit.dto.*;
import com.epfafrica.cocovoit.security.SecurityUtils;
import com.epfafrica.cocovoit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Creer un compte ETUDIANT")
    @PostMapping("/register")
    public ResponseEntity<UtilisateurResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @Operation(summary = "Se connecter et obtenir un jeton JWT")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @Operation(summary = "Profil de l'utilisateur courant")
    @GetMapping("/moi")
    public ResponseEntity<UtilisateurResponse> moi() {
        return ResponseEntity.ok(UtilisateurResponse.from(SecurityUtils.getCurrentUser()));
    }
}
