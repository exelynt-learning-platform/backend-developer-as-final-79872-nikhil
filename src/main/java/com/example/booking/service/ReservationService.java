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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

        // 1. Find logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // 2. Find resource
        Resource resource = resourceRepository.findById(
                        request.getResourceId())
                .orElseThrow(() ->
                        new RuntimeException("Resource not found"));


        // 3. Validate start time
        if (request.getStartTime().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Start time cannot be in the past"
            );
        }


        // 4. Validate end time
        if (!request.getEndTime().isAfter(request.getStartTime())) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }


        // 5. Check overlapping reservation
        boolean overlapping =
                reservationRepository.existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );


        if (overlapping) {

            throw new ResourceAlreadyBookedException(
                    "Resource is already booked for the selected time"
            );
        }


        // 6. Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .price(resource.getPrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING)
                .build();


        // 7. Save reservation
        Reservation savedReservation =
                reservationRepository.save(reservation);


        // 8. Return response
        return mapToResponse(savedReservation);
    }


    // =========================================================
    // GET MY RESERVATIONS
    // =========================================================
    public List<ReservationResponse> getMyReservations(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        return reservationRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET RESERVATION BY ID
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
    // CANCEL MY RESERVATION
    // =========================================================
    public ReservationResponse cancelReservation(
            Long id,
            String username) {

        // 1. Find reservation
        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"
                                ));


        // 2. Check ownership
        if (!reservation.getUser()
                .getUsername()
                .equals(username)) {

            throw new RuntimeException(
                    "You can only cancel your own reservation"
            );
        }


        // 3. Check if already cancelled
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new RuntimeException(
                    "Reservation is already cancelled"
            );
        }


        // 4. Change status
        reservation.setStatus(
                ReservationStatus.CANCELLED
        );


        // 5. Save updated reservation
        Reservation updatedReservation =
                reservationRepository.save(reservation);


        // 6. Return response
        return mapToResponse(updatedReservation);
    }


    // =========================================================
    // ADMIN - GET ALL RESERVATIONS
    // =========================================================
    public List<ReservationResponse> getAllReservations() {

        return reservationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
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