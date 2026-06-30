package com.epfafrica.cocovoit.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


public record TrajetRequest(
    @NotBlank(message = "La ville de départ est obligatoire") String villeDepart,
    @NotBlank(message = "La ville d'arrivee est obligatoire")
    String villeArrivee,

    @NotNull(message = "La date de depart est obligatoire")
    @Future(message = "La date de depart doit etre dans le futur")
    LocalDateTime dateHeureDepart,

    @Min(value = 1, message = "Le nombre de places doit etre au moins 1")
    int placesTotal,

    @Min(value = 0, message = "Le prix par place doit etre positif ou nul")
    double prixParPlace

) {
    
}
