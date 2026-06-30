package com.epfafrica.cocovoit.dto;

import com.epfafrica.cocovoit.entity.Reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long trajetId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateHeureDepart,
        Long passagerId,
        String passagerUsername,
        int nbPlaces,
        String statut,
        LocalDateTime dateReservation
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getTrajet().getId(),
                r.getTrajet().getVilleDepart(),
                r.getTrajet().getVilleArrivee(),
                r.getTrajet().getDateHeureDepart(),
                r.getPassager().getId(),
                r.getPassager().getUsername(),
                r.getNbPlaces(),
                r.getStatut().name(),
                r.getDateReservation()
        );
    }
}
