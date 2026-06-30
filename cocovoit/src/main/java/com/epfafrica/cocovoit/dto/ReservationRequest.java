package com.epfafrica.cocovoit.dto;

import jakarta.validation.constraints.Min;

public record ReservationRequest(
        @Min(value = 1, message = "Le nombre de places doit etre au moins 1")
        int nbPlaces
) {
}
