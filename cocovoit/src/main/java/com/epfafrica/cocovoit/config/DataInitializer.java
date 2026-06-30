package com.epfafrica.cocovoit.config;

import com.epfafrica.cocovoit.entity.*;
import com.epfafrica.cocovoit.repository.TrajetRepository;
import com.epfafrica.cocovoit.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           TrajetRepository trajetRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }

        Utilisateur admin = new Utilisateur("admin",
                passwordEncoder.encode("admin123"),
                "Diallo", "Admin", "admin@epfafrica.com", Role.ADMIN);

        Utilisateur awa = new Utilisateur("awa",
                passwordEncoder.encode("awa123"),
                "Sow", "Awa", "awa@epfafrica.com", Role.ETUDIANT);

        Utilisateur cheikh = new Utilisateur("cheikh",
                passwordEncoder.encode("cheikh123"),
                "Ndiaye", "Cheikh", "cheikh@epfafrica.com", Role.ETUDIANT);

        utilisateurRepository.save(admin);
        utilisateurRepository.save(awa);
        utilisateurRepository.save(cheikh);

        // Trajets proposes par Awa
        Trajet t1 = new Trajet();
        t1.setVilleDepart("Dakar");
        t1.setVilleArrivee("Thies");
        t1.setDateHeureDepart(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0));
        t1.setPlacesTotal(3);
        t1.setPlacesDisponibles(3);
        t1.setPrixParPlace(1500);
        t1.setStatut(StatutTrajet.OUVERT);
        t1.setConducteur(awa);

        Trajet t2 = new Trajet();
        t2.setVilleDepart("Dakar");
        t2.setVilleArrivee("Saint-Louis");
        t2.setDateHeureDepart(LocalDateTime.now().plusDays(2).withHour(14).withMinute(30).withSecond(0).withNano(0));
        t2.setPlacesTotal(4);
        t2.setPlacesDisponibles(4);
        t2.setPrixParPlace(3000);
        t2.setStatut(StatutTrajet.OUVERT);
        t2.setConducteur(cheikh);

        trajetRepository.save(t1);
        trajetRepository.save(t2);

        System.out.println("=== Donnees initialisees ===");
        System.out.println("ADMIN    -> admin / admin123");
        System.out.println("ETUDIANT -> awa / awa123");
        System.out.println("ETUDIANT -> cheikh / cheikh123");
    }
}
