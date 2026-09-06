package com.example.booking.repository;

import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // =========================================================
    // FIND RESERVATIONS OF A USER
    // =========================================================
    Page<Reservation> findByUserId(
            Long userId,
            Pageable pageable
    );


    // =========================================================
    // CHECK OVERLAPPING RESERVATION
    // =========================================================
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.resource.id = :resourceId
            AND r.status <> 'CANCELLED'
            AND r.startTime < :endTime
            AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


    // =========================================================
    // FILTER + PAGINATION + SORTING
    // =========================================================
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE (:status IS NULL OR r.status = :status)
            AND (:minPrice IS NULL OR r.price >= :minPrice)
            AND (:maxPrice IS NULL OR r.price <= :maxPrice)
            """)
    Page<Reservation> findWithFilters(
            @Param("status") ReservationStatus status,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );
}