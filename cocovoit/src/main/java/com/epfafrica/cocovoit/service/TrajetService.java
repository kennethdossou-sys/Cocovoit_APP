package com.epfafrica.cocovoit.service;

import com.epfafrica.cocovoit.dto.TrajetRequest;
import com.epfafrica.cocovoit.dto.TrajetResponse;
import com.epfafrica.cocovoit.entity.*;
import com.epfafrica.cocovoit.exception.AccesInterditException;
import com.epfafrica.cocovoit.exception.RessourceIntrouvableException;
import com.epfafrica.cocovoit.repository.TrajetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrajetService {

    private final TrajetRepository trajetRepository;

    public TrajetService(TrajetRepository trajetRepository) {
        this.trajetRepository = trajetRepository;
    }

    @Transactional(readOnly = true)
    public List<TrajetResponse> rechercher(String depart, String arrivee, java.time.LocalDate date) {
        LocalDateTime dateMin = null;
        LocalDateTime dateMax = null;
        if (date != null) {
            dateMin = date.atStartOfDay();
            dateMax = date.atTime(LocalTime.MAX);
        }
        String d = (depart == null || depart.isBlank()) ? null : depart;
        String a = (arrivee == null || arrivee.isBlank()) ? null : arrivee;
        return trajetRepository.rechercher(d, a, dateMin, dateMax)
                .stream().map(TrajetResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TrajetResponse getById(Long id) {
        return TrajetResponse.from(trouver(id));
    }

    public Trajet trouver(Long id) {
        return trajetRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Trajet introuvable : id=" + id));
    }

    @Transactional
    public TrajetResponse creer(TrajetRequest req, Utilisateur conducteur) {
        Trajet t = new Trajet();
        t.setVilleDepart(req.villeDepart());
        t.setVilleArrivee(req.villeArrivee());
        t.setDateHeureDepart(req.dateHeureDepart());
        t.setPlacesTotal(req.placesTotal());
        t.setPlacesDisponibles(req.placesTotal());
        t.setPrixParPlace(req.prixParPlace());
        t.setStatut(StatutTrajet.OUVERT);
        t.setConducteur(conducteur);
        return TrajetResponse.from(trajetRepository.save(t));
    }

    @Transactional
    public TrajetResponse modifier(Long id, TrajetRequest req, Utilisateur courant) {
        Trajet t = trouver(id);
        verifierProprieteOuAdmin(t, courant);

        // ajuster les places disponibles si le total change
        int placesReservees = t.getPlacesTotal() - t.getPlacesDisponibles();
        t.setVilleDepart(req.villeDepart());
        t.setVilleArrivee(req.villeArrivee());
        t.setDateHeureDepart(req.dateHeureDepart());
        t.setPlacesTotal(req.placesTotal());
        t.setPlacesDisponibles(Math.max(0, req.placesTotal() - placesReservees));
        t.setPrixParPlace(req.prixParPlace());
        // recalcul statut
        if (t.getStatut() != StatutTrajet.ANNULE) {
            t.setStatut(t.getPlacesDisponibles() == 0 ? StatutTrajet.COMPLET : StatutTrajet.OUVERT);
        }
        return TrajetResponse.from(trajetRepository.save(t));
    }

    @Transactional
    public void annuler(Long id, Utilisateur courant) {
        Trajet t = trouver(id);
        verifierProprieteOuAdmin(t, courant);

        t.setStatut(StatutTrajet.ANNULE);
        // annulation en cascade des reservations confirmees
        for (Reservation r : t.getReservations()) {
            if (r.getStatut() == StatutReservation.CONFIRMEE) {
                r.setStatut(StatutReservation.ANNULEE);
            }
        }
        trajetRepository.save(t);
    }

    private void verifierProprieteOuAdmin(Trajet t, Utilisateur courant) {
        boolean estProprietaire = t.getConducteur().getId().equals(courant.getId());
        boolean estAdmin = courant.getRole() == Role.ADMIN;
        if (!estProprietaire && !estAdmin) {
            throw new AccesInterditException("Seul le conducteur ou un ADMIN peut effectuer cette action");
        }
    }
}
