package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.Team;
import com.hephaitos.maintenance.entity.Workshop;
import com.hephaitos.maintenance.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping
    public ResponseEntity<Page<Workshop>> getWorkshops(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Boolean deplacementPossible,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                workshopService.getWorkshops(serviceType, ville, deplacementPossible, PageRequest.of(page, size))
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<WorkshopService.NearbyWorkshopResponse>> getNearbyWorkshops(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "15.0") double radius
    ) {
        return ResponseEntity.ok(workshopService.getNearbyWorkshops(lat, lng, radius));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkshopById(@PathVariable Long id) {
        try {
            Workshop w = workshopService.getWorkshopById(id);
            List<Team> teams = workshopService.getTeamsForWorkshop(id);
            
            // On peut retourner un wrapper de détails
            return ResponseEntity.ok(java.util.Map.of(
                    "workshop", w,
                    "teams", teams
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
