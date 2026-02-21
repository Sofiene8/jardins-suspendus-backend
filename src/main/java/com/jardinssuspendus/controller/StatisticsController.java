package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.StatisticsResponse;
import com.jardinssuspendus.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Retourne toutes les statistiques en une seule requête :
     *  - Clients par mois (année en cours)
     *  - Réservations par mois
     *  - Répartition adultes / enfants
     *  - Chiffre d'affaires N vs N-1
     *  - Taux d'occupation des chambres
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics() {
        StatisticsResponse stats = statisticsService.getStatistics();
        return ResponseEntity.ok(
                ApiResponse.success("Statistiques récupérées avec succès", stats)
        );
    }
}
