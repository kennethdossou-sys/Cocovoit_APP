package com.epfafrica.cocovoit.service;

import com.epfafrica.cocovoit.dto.UtilisateurResponse;
import com.epfafrica.cocovoit.dto.TrajetResponse;
import com.epfafrica.cocovoit.dto.ReservationResponse;
import com.epfafrica.cocovoit.entity.Utilisateur;
import com.epfafrica.cocovoit.entity.Trajet;
import com.epfafrica.cocovoit.entity.Reservation;
import com.epfafrica.cocovoit.exception.ConflitMetierException;

import com.epfafrica.cocovoit.exception.RessourceIntrouvableException;
import com.epfafrica.cocovoit.repository.UtilisateurRepository;
import com.epfafrica.cocovoit.repository.TrajetRepository;
import com.epfafrica.cocovoit.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;

    public AdminService(UtilisateurRepository utilisateurRepository,TrajetRepository trajetRepository,ReservationRepository reservationRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream().map(UtilisateurResponse::from).toList();
    }
    @Transactional
    public void supprimerUtilisateur(Long id, Utilisateur courant){
        Utilisateur cible = utilisateurRepository.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable : id =" +id));

        if (cible.getId().equals(courant.getId())) {
            throw new ConflitMetierException("Un administrateur ne peut pas supprimer son propre compte");
        }
        utilisateurRepository.delete(cible);
    }

    @Transactional(readOnly = true)
    public List<TrajetResponse> listerTousLesTrajets(){
        return trajetRepository.findAll()
                .stream().map(TrajetResponse::from).toList();

    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listerTouteslesReservations(Long trajetId,Long passagerId){
        List<Reservation> resas;
        if(trajetId != null){
            resas = reservationRepository.findByTrajetId(trajetId);
        }else if(passagerId != null){
            resas = reservationRepository.findByPassagerId(passagerId);
        }else {
            resas=reservationRepository.findAll();
        }
        return resas.stream().map(ReservationResponse::from).toList();

    }
    
}
