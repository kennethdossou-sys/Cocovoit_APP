package com.epfafrica.cocovoit.repository;

//import com.epfafrica.cocovoit.entity.StatutTrajet;
import com.epfafrica.cocovoit.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
public interface TrajetRepository extends JpaRepository<Trajet, Long>{

    List<Trajet> findByVilleDepartIgnoreCaseAndVilleArriveeIgnoreCase(String villeDepart, String villeArrivee);

        @Query("""
            SELECT t FROM Trajet t
            WHERE (:depart IS NULL OR LOWER(t.villeDepart) = LOWER(:depart))
              AND (:arrivee IS NULL OR LOWER(t.villeArrivee) = LOWER(:arrivee))
              AND (:dateMin IS NULL OR t.dateHeureDepart >= :dateMin)
              AND (:dateMax IS NULL OR t.dateHeureDepart < :dateMax)
            ORDER BY t.dateHeureDepart ASC
            """)
    List<Trajet> rechercher(@Param("depart") String depart,
                            @Param("arrivee") String arrivee,
                            @Param("dateMin") LocalDateTime dateMin,
                            @Param("dateMax") LocalDateTime dateMax);
}

