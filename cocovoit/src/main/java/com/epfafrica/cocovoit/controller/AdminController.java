package com.epfafrica.cocovoit.controller;

import com.epfafrica.cocovoit.dto.UtilisateurResponse;
import com.epfafrica.cocovoit.dto.TrajetResponse;
import com.epfafrica.cocovoit.dto.ReservationResponse;
import com.epfafrica.cocovoit.security.SecurityUtils;
import com.epfafrica.cocovoit.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration")
public class AdminController{

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService=adminService;
    }

    @Operation(summary = "Lister tous le comptes")
    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurResponse>> utilisateurs() {
        return ResponseEntity.ok(adminService.listerUtilisateurs());
    }

    @Operation(summary = "Lister tous les trajets")
    @GetMapping("/trajets")
    public ResponseEntity<List<TrajetResponse>> trajets(){
        return ResponseEntity.ok(adminService.listerTousLesTrajets());
    }

    @Operation(summary = "Supprimer un compte (ADMIN)")
    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        adminService.supprimerUtilisateur(id, SecurityUtils.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister toutes les réservations")
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations(
        @RequestParam(required = false) Long trajetId,
        @RequestParam(required = false) Long passagerId){

        return ResponseEntity.ok(adminService.listerTouteslesReservations(trajetId, passagerId));
    }
    

}
