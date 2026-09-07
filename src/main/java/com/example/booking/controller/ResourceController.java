package com.example.booking.controller;

import com.example.booking.dto.resource.ResourceRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(
        name = "Resources",
        description = "Operations for managing bookable resources"
)
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final ResourceService resourceService;


    // =========================================================
    // GET ALL RESOURCES
    // USER + ADMIN
    // =========================================================

    @Operation(
            summary = "Get all resources",
            description = "Returns all available bookable resources"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resources retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public List<ResourceResponse> getAllResources() {

        return resourceService.getAllResources();
    }


    // =========================================================
    // GET RESOURCE BY ID
    // USER + ADMIN
    // =========================================================

    @Operation(
            summary = "Get resource by ID",
            description = "Returns a single resource using its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Resource not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping("/{id}")
    public ResourceResponse getResourceById(

            @Parameter(
                    description = "ID of the resource",
                    example = "1"
            )
            @PathVariable Long id) {

        return resourceService.getResourceById(id);
    }


    // =========================================================
    // CREATE RESOURCE
    // ADMIN ONLY
    // =========================================================

    @Operation(
            summary = "Create a resource",
            description = "Creates a new bookable resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can create resources"
            )
    })
    @PostMapping
    public ResourceResponse createResource(
            @Valid @RequestBody ResourceRequest request) {

        return resourceService.createResource(request);
    }


    // =========================================================
    // UPDATE RESOURCE
    // ADMIN ONLY
    // =========================================================

    @Operation(
            summary = "Update a resource",
            description = "Updates an existing resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource data or resource not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can update resources"
            )
    })
    @PutMapping("/{id}")
    public ResourceResponse updateResource(

            @Parameter(
                    description = "ID of the resource",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody ResourceRequest request) {

        return resourceService.updateResource(
                id,
                request
        );
    }


    // =========================================================
    // DELETE RESOURCE
    // ADMIN ONLY
    // =========================================================

    @Operation(
            summary = "Delete a resource",
            description = "Deletes an existing resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Resource not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only ADMIN can delete resources"
            )
    })
    @DeleteMapping("/{id}")
    public void deleteResource(

            @Parameter(
                    description = "ID of the resource",
                    example = "1"
            )
            @PathVariable Long id) {

        resourceService.deleteResource(id);
    }
}