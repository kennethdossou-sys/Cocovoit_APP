package com.epfafrica.cocovoit.service;

import com.epfafrica.cocovoit.dto.UtilisateurResponse;
import com.epfafrica.cocovoit.entity.Utilisateur;
import com.epfafrica.cocovoit.exception.ConflitMetierException;

import com.epfafrica.cocovoit.exception.RessourceIntrouvableException;
import com.epfafrica.cocovoit.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;

    public AdminService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
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

    
}
