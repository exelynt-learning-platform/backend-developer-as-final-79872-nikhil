package com.example.booking.controller;

import com.example.booking.dto.reservation.ReservationRequest;
import com.example.booking.dto.reservation.ReservationResponse;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(
        name = "Reservations",
        description = "Operations for creating and managing reservations"
)
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;


    // =========================================================
    // CREATE RESERVATION
    // USER + ADMIN
    // =========================================================

    @Operation(
            summary = "Create a reservation",
            description = "Creates a reservation for the authenticated user. "
                    + "The user identity is taken from the JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Resource is already booked for the requested time"
            )
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.createReservation(
                        request,
                        username
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET MY RESERVATIONS
    // USER + ADMIN
    // =========================================================

    @Operation(
            summary = "Get my reservations",
            description = "Returns reservations belonging only to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping("/my")
    public Page<ReservationResponse> getMyReservations(
            Authentication authentication,

            @Parameter(
                    description = "Pagination and optional sorting parameters",
                    example = "page=0&size=10&sort=price,asc"
            )
            Pageable pageable) {

        String username = authentication.getName();

        return reservationService.getMyReservations(
                username,
                pageable
        );
    }


    // =========================================================
    // GET ALL RESERVATIONS
    // ADMIN ONLY
    // =========================================================

    @Operation(
            summary = "Get all reservations",
            description = "Returns all reservations in the system. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can access all reservations"
            )
    })
    @GetMapping
    public List<ReservationResponse> getAllReservations() {

        return reservationService.getAllReservations();
    }


    // =========================================================
    // FILTER + PAGINATION + SORTING
    // ADMIN ONLY
    // =========================================================

    @Operation(
            summary = "Filter reservations",
            description = "ADMIN can filter reservations by status and price range, "
                    + "with pagination and optional sorting."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter, pagination, or sorting parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can filter all reservations"
            )
    })
    @GetMapping("/filter")
    public Page<ReservationResponse> getReservationsWithFilters(

            @Parameter(
                    description = "Reservation status",
                    example = "PENDING"
            )
            @RequestParam(required = false)
            ReservationStatus status,

            @Parameter(
                    description = "Minimum reservation price",
                    example = "100.00"
            )
            @RequestParam(required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum reservation price",
                    example = "1000.00"
            )
            @RequestParam(required = false)
            BigDecimal maxPrice,

            @Parameter(
                    description = "Page number, starting from 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of records per page, maximum 100",
                    example = "10"
            )
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(
                    description = "Entity field to sort by",
                    example = "price"
            )
            @RequestParam(required = false)
            String sortBy,

            @Parameter(
                    description = "Sort direction: asc or desc",
                    example = "asc"
            )
            @RequestParam(defaultValue = "asc")
            String direction) {

        // ---------------------------------------------------------
        // Validate page
        // ---------------------------------------------------------

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be 0 or greater"
            );
        }


        // ---------------------------------------------------------
        // Validate size
        // ---------------------------------------------------------

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


        // ---------------------------------------------------------
        // Validate minimum price
        // ---------------------------------------------------------

        if (minPrice != null &&
                minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be negative"
            );
        }


        // ---------------------------------------------------------
        // Validate maximum price
        // ---------------------------------------------------------

        if (maxPrice != null &&
                maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Maximum price cannot be negative"
            );
        }


        // ---------------------------------------------------------
        // Validate price range
        // ---------------------------------------------------------

        if (minPrice != null &&
                maxPrice != null &&
                minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }


        // ---------------------------------------------------------
        // Sorting
        // ---------------------------------------------------------

        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {

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

            sort = Sort.by(
                    sortDirection,
                    sortBy
            );
        }


        // ---------------------------------------------------------
        // Pageable
        // ---------------------------------------------------------

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort
                );


        // ---------------------------------------------------------
        // Service
        // ---------------------------------------------------------

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

    @Operation(
            summary = "Get reservation by ID",
            description = "USER can access only their own reservation. "
                    + "ADMIN can access any reservation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Reservation not found or access denied"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to access this reservation"
            )
    })
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(

            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
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

    @Operation(
            summary = "Update a reservation",
            description = "Updates an existing reservation. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data or reservation not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can update reservations"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Resource is already booked for the requested time"
            )
    })
    @PutMapping("/{id}")
    public ReservationResponse updateReservation(

            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
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

    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels a reservation. USER can cancel only their own reservation; "
                    + "ADMIN can cancel any reservation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Reservation not found or cannot be cancelled"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User is not allowed to cancel this reservation"
            )
    })
    @PutMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(

            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
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

    @Operation(
            summary = "Delete a reservation",
            description = "Deletes a reservation permanently. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Reservation not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can delete reservations"
            )
    })
    @DeleteMapping("/{id}")
    public void deleteReservation(

            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
            @PathVariable Long id) {

        reservationService.deleteReservation(id);
    }
}