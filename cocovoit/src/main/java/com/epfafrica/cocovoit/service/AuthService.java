package com.epfafrica.cocovoit.service;

import com.epfafrica.cocovoit.dto.*;
import com.epfafrica.cocovoit.entity.Role;
import com.epfafrica.cocovoit.entity.Utilisateur;
import com.epfafrica.cocovoit.exception.ConflitMetierException;
import com.epfafrica.cocovoit.repository.UtilisateurRepository;
import com.epfafrica.cocovoit.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UtilisateurResponse register(RegisterRequest req) {
        if (utilisateurRepository.existsByUsername(req.username())) {
            throw new ConflitMetierException("Ce username est deja utilise");
        }
        Utilisateur u = new Utilisateur(
                req.username(),
                passwordEncoder.encode(req.password()),
                req.name(),
                req.firstname(),
                req.email(),
                Role.ETUDIANT
        );
        return UtilisateurResponse.from(utilisateurRepository.save(u));
    }

    public JwtResponse login(LoginRequest req) {
        // lance BadCredentialsException -> 401 si echec
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        Utilisateur u = utilisateurRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generateToken(u.getUsername(), u.getRole().name());
        return new JwtResponse(token, u.getUsername(), u.getRole().name());
    }
}
