package com.jardinssuspendus.repository;

import com.jardinssuspendus.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailable(Boolean available);

    @Query("SELECT r FROM Room r WHERE r.available = true")
    List<Room> findAllAvailable();

    @Query("SELECT DISTINCT r FROM Room r " +
           "LEFT JOIN r.reservations res " +
           "WHERE r.available = true " +
           "AND (res.id IS NULL " +
           "OR NOT (res.startDate <= :endDate AND res.endDate >= :startDate " +
           "AND res.status IN ('EN_COURS', 'VALIDEE')))")
    List<Room> findAvailableRoomsBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Room r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Room> searchByTitle(@Param("keyword") String keyword);
}