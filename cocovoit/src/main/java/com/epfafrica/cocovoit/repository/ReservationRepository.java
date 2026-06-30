package com.epfafrica.cocovoit.repository;

import com.epfafrica.cocovoit.entity.Reservation;
import com.epfafrica.cocovoit.entity.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassagerId(Long passagerId);

    List<Reservation> findByTrajetId(Long trajetId);

    boolean existsByTrajetIdAndPassagerIdAndStatut(Long trajetId, Long passagerId, StatutReservation statut);
    
}
