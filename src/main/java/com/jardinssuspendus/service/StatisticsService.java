package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.response.StatisticsResponse;
import com.jardinssuspendus.entity.enums.Role;
import com.jardinssuspendus.repository.PaymentRepository;
import com.jardinssuspendus.repository.ReservationRepository;
import com.jardinssuspendus.repository.RoomRepository;
import com.jardinssuspendus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired private UserRepository        userRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private PaymentRepository     paymentRepository;
    @Autowired private RoomRepository        roomRepository;

    public StatisticsResponse getStatistics() {
        StatisticsResponse stats = new StatisticsResponse();

        int currentYear  = LocalDateTime.now().getYear();
        int previousYear = currentYear - 1;

        LocalDateTime startCurrent  = yearStart(currentYear);
        LocalDateTime endCurrent    = yearEnd(currentYear);
        LocalDateTime startPrevious = yearStart(previousYear);
        LocalDateTime endPrevious   = yearEnd(previousYear);
        LocalDateTime now           = LocalDateTime.now();

        // ── Clients ─────────────────────────────────────────────────────────
        stats.setClientsPerMonth(buildClientsPerMonth(currentYear));
        stats.setTotalClients((long) userRepository.findByRole(Role.CLIENT).size());

        // ── Réservations ────────────────────────────────────────────────────
        stats.setReservationsPerMonth(buildReservationsPerMonth(currentYear));
        stats.setTotalReservations(reservationRepository.count());

        // ── Catégories invités (année en cours) ─────────────────────────────
        Long adults   = orZero(reservationRepository.countAdultsBetween(startCurrent, now));
        Long children = orZero(reservationRepository.countChildrenBetween(startCurrent, now));
        long total    = adults + children;

        stats.setTotalAdults(adults);
        stats.setTotalChildren(children);
        stats.setAdultPercentage(   total > 0 ? round((adults   * 100.0) / total) : 0.0);
        stats.setChildrenPercentage(total > 0 ? round((children * 100.0) / total) : 0.0);

        // ── Chiffre d'affaires ───────────────────────────────────────────────
        // Depuis les paiements PAYE (source de vérité financière)
        BigDecimal currentRevenue  = orZero(paymentRepository.calculateRevenueBetween(startCurrent,  endCurrent));
        BigDecimal previousRevenue = orZero(paymentRepository.calculateRevenueBetween(startPrevious, endPrevious));

        stats.setCurrentYearRevenue(currentRevenue);
        stats.setPreviousYearRevenue(previousRevenue);
        stats.setRevenueDifference(currentRevenue.subtract(previousRevenue));

        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            double growth = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            stats.setRevenueGrowthPercentage(round(growth));
        } else {
            stats.setRevenueGrowthPercentage(
                currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
        }

        // ── CA mensuel pour le graphique ────────────────────────────────────
        stats.setRevenuePerMonth(buildRevenuePerMonth(currentYear));

        // ── Chambres ────────────────────────────────────────────────────────
        long totalRooms     = roomRepository.count();
        long availableRooms = roomRepository.findAllAvailable().size();
        stats.setTotalRooms(totalRooms);
        stats.setAvailableRooms(availableRooms);
        stats.setOccupancyRate(
            totalRooms > 0 ? round(((totalRooms - availableRooms) * 100.0) / totalRooms) : 0.0);

        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Long> buildClientsPerMonth(int year) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(year, m);
            result.put(m.name(), orZero(userRepository.countByRoleAndCreatedAtBetween(
                    Role.CLIENT,
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(23, 59, 59))));
        }
        return result;
    }

    private Map<String, Long> buildReservationsPerMonth(int year) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(year, m);
            result.put(m.name(), orZero(reservationRepository.countValidatedReservationsBetween(
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(23, 59, 59))));
        }
        return result;
    }

    private Map<String, BigDecimal> buildRevenuePerMonth(int year) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(year, m);
            BigDecimal rev = paymentRepository.calculateRevenueBetween(
                    ym.atDay(1).atStartOfDay(),
                    ym.atEndOfMonth().atTime(23, 59, 59));
            result.put(m.name(), orZero(rev));
        }
        return result;
    }

    private LocalDateTime yearStart(int year) { return LocalDateTime.of(year, 1,  1,  0, 0, 0); }
    private LocalDateTime yearEnd  (int year) { return LocalDateTime.of(year, 12, 31, 23, 59, 59); }
    private Long       orZero(Long v)       { return v != null ? v : 0L; }
    private BigDecimal orZero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private double     round(double v)      { return Math.round(v * 100.0) / 100.0; }
}