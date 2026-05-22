package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.Team;
import com.hephaitos.maintenance.entity.Workshop;
import com.hephaitos.maintenance.repository.TeamRepository;
import com.hephaitos.maintenance.repository.WorkshopRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final TeamRepository teamRepository;

    public Page<Workshop> getWorkshops(String serviceType, String ville, Boolean deplacementPossible, Pageable pageable) {
        // Règle métier : n'affiche que les ateliers d'entreprises validées, mais ici on applique findFilteredWorkshops
        // On va s'assurer côté logique que si l'entreprise est associée, elle doit être validée
        return workshopRepository.findFilteredWorkshops(serviceType, ville, deplacementPossible, pageable)
                .map(w -> {
                    // Si l'atelier appartient à une entreprise non validée, on peut choisir de le cacher ou de filtrer.
                    // Filtrons au niveau de la requête SQL ou ici. Faisons-le de manière robuste.
                    if (w.getCompany() != null && !w.getCompany().isValide()) {
                        return null; // Sera ignoré ou traité plus tard
                    }
                    return w;
                });
    }

    public List<NearbyWorkshopResponse> getNearbyWorkshops(double lat, double lng, double radiusKm) {
        List<Workshop> allWorkshops = workshopRepository.findAll();
        List<NearbyWorkshopResponse> nearby = new ArrayList<>();

        for (Workshop w : allWorkshops) {
            // Règle métier : L'entreprise associée doit être validée
            if (w.getCompany() != null && !w.getCompany().isValide()) {
                continue;
            }
            if (!w.isActif()) {
                continue;
            }

            if (w.getLatitude() != null && w.getLongitude() != null) {
                double distance = calculateDistance(lat, lng, w.getLatitude(), w.getLongitude());
                if (distance <= radiusKm) {
                    nearby.add(new NearbyWorkshopResponse(w, distance));
                }
            }
        }

        // Trier par distance
        nearby.sort((o1, o2) -> Double.compare(o1.getDistance(), o2.getDistance()));
        return nearby;
    }

    public Workshop getWorkshopById(Long id) {
        Workshop w = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atelier non trouvé"));

        if (w.getCompany() != null && !w.getCompany().isValide()) {
            throw new IllegalArgumentException("Cet atelier appartient à une entreprise en attente de validation.");
        }

        return w;
    }

    public List<Team> getTeamsForWorkshop(Long workshopId) {
        Workshop workshop = getWorkshopById(workshopId);
        return teamRepository.findByWorkshopAndActif(workshop, true);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Data
    @AllArgsConstructor
    public static class NearbyWorkshopResponse {
        private Workshop workshop;
        private double distance;
    }
}
