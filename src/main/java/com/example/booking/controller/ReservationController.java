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

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        validatePagination(page, size);

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                direction
        );

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
    public Page<ReservationResponse> getAllReservations(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        validatePagination(page, size);

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                direction
        );

        return reservationService.getAllReservations(
                pageable
        );
    }


    // =========================================================
    // FILTER RESERVATIONS
    // USER + ADMIN
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

        validatePagination(page, size);

        // Validate price range
        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        Pageable pageable = createPageable(
                page,
                size,
                sortBy,
                direction
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
            @PathVariable Long id) {

        return reservationService.getReservationById(id);
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

        return reservationService.cancelReservation(
                id,
                username
        );
    }


    // =========================================================
    // PAGINATION VALIDATION
    // =========================================================
    private void validatePagination(
            int page,
            int size) {

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

        if (size > 100) {
            throw new IllegalArgumentException(
                    "Size cannot be greater than 100"
            );
        }
    }


    // =========================================================
    // CREATE PAGEABLE
    // =========================================================
    private Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String direction) {

        // No sorting requested
        if (sortBy == null || sortBy.isBlank()) {

            return PageRequest.of(
                    page,
                    size
            );
        }

        Sort.Direction sortDirection;

        if (direction.equalsIgnoreCase("desc")) {

            sortDirection = Sort.Direction.DESC;

        } else if (direction.equalsIgnoreCase("asc")) {

            sortDirection = Sort.Direction.ASC;

        } else {

            throw new IllegalArgumentException(
                    "Direction must be 'asc' or 'desc'"
            );
        }

        Sort sort = Sort.by(
                sortDirection,
                sortBy
        );

        return PageRequest.of(
                page,
                size,
                sort
        );
    }
}