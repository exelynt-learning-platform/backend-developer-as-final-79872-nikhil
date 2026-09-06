package com.example.booking.controller;

import com.example.booking.dto.resource.ResourceRequest;
import com.example.booking.dto.resource.ResourceResponse;
import com.example.booking.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public List<ResourceResponse> getAllResources() {

        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    public ResourceResponse getResourceById(
            @PathVariable Long id) {

        return resourceService.getResourceById(id);
    }

    @PostMapping
    public ResourceResponse createResource(
            @Valid @RequestBody ResourceRequest request) {

        return resourceService.createResource(request);
    }

    @PutMapping("/{id}")
    public ResourceResponse updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {

        return resourceService.updateResource(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public void deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);
    }
}