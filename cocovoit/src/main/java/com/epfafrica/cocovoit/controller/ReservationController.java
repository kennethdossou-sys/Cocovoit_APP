package com.epfafrica.cocovoit.controller;

import com.epfafrica.cocovoit.dto.ReservationRequest;
import com.epfafrica.cocovoit.dto.ReservationResponse;
import com.epfafrica.cocovoit.security.SecurityUtils;
import com.epfafrica.cocovoit.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Reserver n places sur un trajet")
    @PostMapping("/trajets/{id}/reservations")
    public ResponseEntity<ReservationResponse> reserver(@PathVariable Long id,
                                                        @Valid @RequestBody ReservationRequest req) {
        ReservationResponse r = reservationService.reserver(id, req, SecurityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @Operation(summary = "Mes reservations")
    @GetMapping("/mes-reservations")
    public ResponseEntity<List<ReservationResponse>> mesReservations() {
        return ResponseEntity.ok(reservationService.mesReservations(SecurityUtils.getCurrentUser()));
    }

    @Operation(summary = "Annuler une reservation (passager ou ADMIN)")
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        reservationService.annuler(id, SecurityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
