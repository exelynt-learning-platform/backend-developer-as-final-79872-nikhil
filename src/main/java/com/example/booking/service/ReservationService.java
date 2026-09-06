package com.example.booking.service;

import com.example.booking.dto.reservation.ReservationRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.entity.Resource;
import com.example.booking.entity.User;
import com.example.booking.exception.ResourceAlreadyBookedException;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;


    // =========================================================
    // CREATE RESERVATION
    // =========================================================
    public ReservationResponse createReservation(
            ReservationRequest request,
            String username) {

        // Find logged-in user from JWT
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // Find resource
        Resource resource = resourceRepository.findById(
                        request.getResourceId())
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));


        // Validate start time
        if (request.getStartTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Start time cannot be in the past"
            );
        }


        // Validate end time
        if (!request.getEndTime()
                .isAfter(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }


        // Check overlapping reservation
        boolean overlapping =
                reservationRepository
                        .existsOverlappingReservation(
                                resource.getId(),
                                request.getStartTime(),
                                request.getEndTime()
                        );


        if (overlapping) {

            throw new ResourceAlreadyBookedException(
                    "Resource is already booked for the selected time"
            );
        }


        // Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .price(resource.getPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING)
                .build();


        // Save reservation
        Reservation savedReservation =
                reservationRepository.save(reservation);


        return mapToResponse(savedReservation);
    }


    // =========================================================
    // GET MY RESERVATIONS
    // USER + ADMIN
    // PAGINATION + SORTING
    // =========================================================
    public Page<ReservationResponse> getMyReservations(
            String username,
            Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        return reservationRepository
                .findByUserId(
                        user.getId(),
                        pageable
                )
                .map(this::mapToResponse);
    }


    // =========================================================
    // GET RESERVATION BY ID
    // USER + ADMIN
    // =========================================================
    public ReservationResponse getReservationById(
            Long id) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"
                                ));


        return mapToResponse(reservation);
    }


    // =========================================================
    // CANCEL RESERVATION
    // USER + ADMIN
    // =========================================================
    public ReservationResponse cancelReservation(
            Long id,
            String username) {

        // Find reservation
        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"
                                ));


        // Check ownership
        if (!reservation.getUser()
                .getUsername()
                .equals(username)) {

            throw new RuntimeException(
                    "You can only cancel your own reservation"
            );
        }


        // Check if already cancelled
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new RuntimeException(
                    "Reservation is already cancelled"
            );
        }


        // Change status
        reservation.setStatus(
                ReservationStatus.CANCELLED
        );


        // Save updated reservation
        Reservation updatedReservation =
                reservationRepository.save(reservation);


        return mapToResponse(updatedReservation);
    }


    // =========================================================
    // ADMIN - GET ALL RESERVATIONS
    // PAGINATION + SORTING
    // =========================================================
    public Page<ReservationResponse> getAllReservations(
            Pageable pageable) {

        return reservationRepository
                .findAll(pageable)
                .map(this::mapToResponse);
    }


    // =========================================================
    // FILTER + PAGINATION + SORTING
    // =========================================================
    public Page<ReservationResponse> getReservationsWithFilters(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return reservationRepository
                .findWithFilters(
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                )
                .map(this::mapToResponse);
    }


    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================
    private ReservationResponse mapToResponse(
            Reservation reservation) {

        return ReservationResponse.builder()

                .id(reservation.getId())

                .resourceId(
                        reservation.getResource().getId()
                )

                .resourceName(
                        reservation.getResource().getName()
                )

                .username(
                        reservation.getUser().getUsername()
                )

                .price(
                        reservation.getPrice()
                )

                .startTime(
                        reservation.getStartTime()
                )

                .endTime(
                        reservation.getEndTime()
                )

                .status(
                        reservation.getStatus()
                )

                .build();
    }
}