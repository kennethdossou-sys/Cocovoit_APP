package com.epfafrica.cocovoit.dto;

import com.epfafrica.cocovoit.entity.Trajet;

import java.time.LocalDateTime;

public record TrajetResponse(
    Long id,
    String villeDepart,
    String villeArrivee,
    LocalDateTime dateHeureDepart,
    int placesTotal,
    int placesDisponibles,
    double prixparPlace,
    String Statut,
    Long conducteurId,
    String conducteurUsername,
    String conducteurName,
    String conducteurFirstname
) {
    public static TrajetResponse from(Trajet t) {
        return new TrajetResponse(
                t.getId(),
                t.getVilleDepart(),
                t.getVilleArrivee(),
                t.getDateHeureDepart(),
                t.getPlacesTotal(),
                t.getPlacesDisponibles(),
                t.getPrixParPlace(),
                t.getStatut().name(),
                t.getConducteur().getId(),
                t.getConducteur().getUsername(),
                t.getConducteur().getName(),
                t.getConducteur().getFirstname()
        );
    }
    
    
}
