package com.example.booking.controller;

import com.example.booking.dto.reservation.ReservationRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;


    // =========================================================
    // CREATE RESERVATION
    // USER + ADMIN
    // =========================================================

    @PostMapping
    public ReservationResponse createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        return reservationService.createReservation(
                request,
                username
        );
    }


    // =========================================================
    // GET MY RESERVATIONS
    // USER + ADMIN
    // =========================================================

    @GetMapping("/my")
    public Page<ReservationResponse> getMyReservations(
            Authentication authentication,
            Pageable pageable) {

        String username = authentication.getName();

        return reservationService.getMyReservations(
                username,
                pageable
        );
    }


    // =========================================================
    // GET ALL RESERVATIONS
    // ADMIN
    // =========================================================

    @GetMapping
    public List<ReservationResponse> getAllReservations() {

        return reservationService.getAllReservations();
    }


    // =========================================================
    // FILTER + PAGINATION + SORTING
    // ADMIN
    // =========================================================

    @GetMapping("/filter")
    public Page<ReservationResponse> getReservationsWithFilters(

            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be 0 or greater"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Size must be greater than 0"
            );
        }

        if (minPrice != null &&
                minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be negative"
            );
        }

        if (maxPrice != null &&
                maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Maximum price cannot be negative"
            );
        }

        if (minPrice != null &&
                maxPrice != null &&
                minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }


        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {

            Sort.Direction sortDirection =
                    direction.equalsIgnoreCase("desc")
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            sort = Sort.by(
                    sortDirection,
                    sortBy
            );
        }


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        return reservationService.getReservationsWithFilters(
                status,
                minPrice,
                maxPrice,
                pageable
        );
    }


    // =========================================================
    // GET RESERVATION BY ID
    // USER + ADMIN
    // =========================================================

    @GetMapping("/{id}")
    public ReservationResponse getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        return reservationService.getReservationById(
                id,
                username,
                isAdmin
        );
    }


    // =========================================================
    // UPDATE RESERVATION
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{id}")
    public ReservationResponse updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {

        return reservationService.updateReservation(
                id,
                request
        );
    }


    // =========================================================
    // CANCEL RESERVATION
    // USER + ADMIN
    // =========================================================

    @PutMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        return reservationService.cancelReservation(
                id,
                username,
                isAdmin
        );
    }


    // =========================================================
    // DELETE RESERVATION
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);
    }
}