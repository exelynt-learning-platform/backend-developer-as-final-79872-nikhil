package com.example.booking.dto.reservation;

import com.example.booking.entity.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;

    private Long resourceId;

    private String resourceName;

    private String username;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ReservationStatus status;
}