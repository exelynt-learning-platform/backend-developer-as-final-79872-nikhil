package com.example.booking.controller;

import com.example.booking.dto.reservation.ReservationRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;


    // ==============================
    // CREATE RESERVATION
    // USER + ADMIN
    // ==============================
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


    // ==============================
    // GET MY RESERVATIONS
    // USER + ADMIN
    // ==============================
    @GetMapping("/my")
    public List<ReservationResponse> getMyReservations(
            Authentication authentication) {

        String username = authentication.getName();

        return reservationService.getMyReservations(
                username
        );
    }


    // ==============================
    // GET ALL RESERVATIONS
    // ADMIN
    // ==============================
    @GetMapping
    public List<ReservationResponse> getAllReservations() {

        return reservationService.getAllReservations();
    }


    // ==============================
    // GET RESERVATION BY ID
    // USER + ADMIN
    // ==============================
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(
            @PathVariable Long id) {

        return reservationService.getReservationById(id);
    }


    // ==============================
    // CANCEL RESERVATION
    // USER + ADMIN
    // ==============================
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
}