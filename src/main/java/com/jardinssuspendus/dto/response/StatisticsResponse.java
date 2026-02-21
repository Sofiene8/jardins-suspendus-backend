package com.jardinssuspendus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    // ── Clients ──────────────────────────────────────────────────────────────
    private Map<String, Long> clientsPerMonth;
    private Long totalClients;

    // ── Réservations ─────────────────────────────────────────────────────────
    private Map<String, Long> reservationsPerMonth;
    private Long totalReservations;

    // ── Composition invités ───────────────────────────────────────────────────
    private Long   totalAdults;
    private Long   totalChildren;
    private Double adultPercentage;
    private Double childrenPercentage;

    // ── Chiffre d'affaires ────────────────────────────────────────────────────
    private BigDecimal              currentYearRevenue;
    private BigDecimal              previousYearRevenue;
    private BigDecimal              revenueDifference;
    private Double                  revenueGrowthPercentage;
    private Map<String, BigDecimal> revenuePerMonth;   // ← nouveau : CA mois par mois

    // ── Chambres ──────────────────────────────────────────────────────────────
    private Long   totalRooms;
    private Long   availableRooms;
    private Double occupancyRate;

    // ── Top chambres ──────────────────────────────────────────────────────────
    private Map<String, Long>   topRoomsByReservations;
    private Map<String, Double> topRoomsByRating;
}