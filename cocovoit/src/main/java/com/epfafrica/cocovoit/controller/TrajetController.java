package com.epfafrica.cocovoit.controller;

import com.epfafrica.cocovoit.dto.ReservationResponse;
import com.epfafrica.cocovoit.dto.TrajetRequest;
import com.epfafrica.cocovoit.dto.TrajetResponse;
import com.epfafrica.cocovoit.security.SecurityUtils;
import com.epfafrica.cocovoit.service.ReservationService;
import com.epfafrica.cocovoit.service.TrajetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@Tag(name = "Trajets")
public class TrajetController {

    private final TrajetService trajetService;
    private final ReservationService reservationService;

    public TrajetController(TrajetService trajetService, ReservationService reservationService) {
        this.trajetService = trajetService;
        this.reservationService = reservationService;
    }

    @Operation(summary = "Liste des trajets avec filtres optionnels (public)")
    @GetMapping
    public ResponseEntity<List<TrajetResponse>> lister(
            @RequestParam(required = false) String depart,
            @RequestParam(required = false) String arrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(trajetService.rechercher(depart, arrivee, date));
    }

    @Operation(summary = "Detail d'un trajet (public)")
    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.getById(id));
    }

    @Operation(summary = "Creer un trajet (le createur devient conducteur)")
    @PostMapping
    public ResponseEntity<TrajetResponse> creer(@Valid @RequestBody TrajetRequest req) {
        TrajetResponse t = trajetService.creer(req, SecurityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @Operation(summary = "Modifier son trajet (conducteur uniquement)")
    @PutMapping("/{id}")
    public ResponseEntity<TrajetResponse> modifier(@PathVariable Long id,
                                                   @Valid @RequestBody TrajetRequest req) {
        return ResponseEntity.ok(trajetService.modifier(id, req, SecurityUtils.getCurrentUser()));
    }

    @Operation(summary = "Annuler un trajet (conducteur ou ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        trajetService.annuler(id, SecurityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Voir les reservations d'un trajet (conducteur)")
    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations(@PathVariable Long id) {
        return ResponseEntity.ok(
                reservationService.reservationsDuTrajet(id, SecurityUtils.getCurrentUser()));
    }
}
