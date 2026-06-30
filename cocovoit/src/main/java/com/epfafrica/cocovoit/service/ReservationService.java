package com.epfafrica.cocovoit.service;

import com.epfafrica.cocovoit.dto.ReservationRequest;
import com.epfafrica.cocovoit.dto.ReservationResponse;
import com.epfafrica.cocovoit.entity.*;
import com.epfafrica.cocovoit.exception.AccesInterditException;
import com.epfafrica.cocovoit.exception.ConflitMetierException;
import com.epfafrica.cocovoit.exception.RessourceIntrouvableException;
import com.epfafrica.cocovoit.repository.ReservationRepository;
import com.epfafrica.cocovoit.repository.TrajetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final TrajetService trajetService;

    public ReservationService(ReservationRepository reservationRepository,
                              TrajetRepository trajetRepository,
                              TrajetService trajetService) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.trajetService = trajetService;
    }

    @Transactional
    public ReservationResponse reserver(Long trajetId, ReservationRequest req, Utilisateur passager) {
        Trajet trajet = trajetService.trouver(trajetId);

        // Regle : on ne reserve pas son propre trajet
        if (trajet.getConducteur().getId().equals(passager.getId())) {
            throw new ConflitMetierException("Vous ne pouvez pas reserver votre propre trajet");
        }

        // Regle : trajet annule
        if (trajet.getStatut() == StatutTrajet.ANNULE) {
            throw new ConflitMetierException("Ce trajet est annule");
        }

        // Regle : trajet complet
        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            throw new ConflitMetierException("Ce trajet est complet");
        }

        // Regle : trajet deja passe
        if (trajet.getDateHeureDepart().isBefore(LocalDateTime.now())) {
            throw new ConflitMetierException("Ce trajet est deja passe");
        }

        // Regle : pas de double reservation confirmee
        boolean dejaReserve = reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(
                trajetId, passager.getId(), StatutReservation.CONFIRMEE);
        if (dejaReserve) {
            throw new ConflitMetierException("Vous avez deja une reservation confirmee sur ce trajet");
        }

        // Regle : assez de places
        if (req.nbPlaces() > trajet.getPlacesDisponibles()) {
            throw new ConflitMetierException(
                    "Places insuffisantes : " + trajet.getPlacesDisponibles() + " disponible(s)");
        }

        // Creation
        Reservation r = new Reservation();
        r.setTrajet(trajet);
        r.setPassager(passager);
        r.setNbPlaces(req.nbPlaces());
        r.setStatut(StatutReservation.CONFIRMEE);
        r.setDateReservation(LocalDateTime.now());

        // Decrement des places
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - req.nbPlaces());
        if (trajet.getPlacesDisponibles() == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
        }
        trajetRepository.save(trajet);

        return ReservationResponse.from(reservationRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> mesReservations(Utilisateur passager) {
        return reservationRepository.findByPassagerId(passager.getId())
                .stream().map(ReservationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsDuTrajet(Long trajetId, Utilisateur courant) {
        Trajet trajet = trajetService.trouver(trajetId);
        boolean estConducteur = trajet.getConducteur().getId().equals(courant.getId());
        boolean estAdmin = courant.getRole() == Role.ADMIN;
        if (!estConducteur && !estAdmin) {
            throw new AccesInterditException("Seul le conducteur peut voir les reservations de ce trajet");
        }
        return reservationRepository.findByTrajetId(trajetId)
                .stream().map(ReservationResponse::from).toList();
    }

    @Transactional
    public void annuler(Long reservationId, Utilisateur courant) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Reservation introuvable : id=" + reservationId));

        boolean estProprietaire = r.getPassager().getId().equals(courant.getId());
        boolean estAdmin = courant.getRole() == Role.ADMIN;
        if (!estProprietaire && !estAdmin) {
            throw new AccesInterditException("Seul le passager ou un ADMIN peut annuler cette reservation");
        }

        if (r.getStatut() == StatutReservation.ANNULEE) {
            throw new ConflitMetierException("Cette reservation est deja annulee");
        }

        r.setStatut(StatutReservation.ANNULEE);

        // restitution des places
        Trajet trajet = r.getTrajet();
        if (trajet.getStatut() != StatutTrajet.ANNULE) {
            trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + r.getNbPlaces());
            if (trajet.getStatut() == StatutTrajet.COMPLET) {
                trajet.setStatut(StatutTrajet.OUVERT);
            }
            trajetRepository.save(trajet);
        }
        reservationRepository.save(r);
    }
}
