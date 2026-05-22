package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.*;
import com.hephaitos.maintenance.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AgentRepository agentRepository;
    private final WorkshopRepository workshopRepository;
    private final OrderRepository orderRepository;
    private final TeamRepository teamRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final CancellationFeeRuleRepository feeRuleRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long totalCompanies = companyRepository.count();
        long totalAgents = agentRepository.count();
        long totalWorkshops = workshopRepository.count();
        long totalOrders = orderRepository.count();

        List<Order> orders = orderRepository.findAll();
        Map<String, Long> ordersByStatus = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Order o : orders) {
            String status = o.getStatut().name();
            ordersByStatus.put(status, ordersByStatus.getOrDefault(status, 0L) + 1);

            // Chiffre d'affaires basé sur les commandes complétées/facturées payées
            if (o.getStatut() == OrderStatus.TERMINEE || o.getStatut() == OrderStatus.FACTUREE) {
                totalRevenue = totalRevenue.add(o.getMontantTotal());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("usersCount", totalUsers);
        stats.put("companiesCount", totalCompanies);
        stats.put("agentsCount", totalAgents);
        stats.put("workshopsCount", totalWorkshops);
        stats.put("ordersCount", totalOrders);
        stats.put("ordersByStatus", ordersByStatus);
        stats.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/companies/{id}/validate")
    @Transactional
    public ResponseEntity<?> validateCompany(@PathVariable Long id, @RequestParam boolean validate) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée"));
        company.setValide(validate);
        companyRepository.save(company);
        return ResponseEntity.ok(java.util.Map.of("message", "Statut entreprise mis à jour avec succès : " + validate));
    }

    @PostMapping("/agents/{id}/validate")
    @Transactional
    public ResponseEntity<?> validateAgent(@PathVariable Long id, @RequestParam boolean validate) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé"));
        agent.setValide(validate);
        agentRepository.save(agent);
        return ResponseEntity.ok(java.util.Map.of("message", "Statut agent mis à jour avec succès : " + validate));
    }

    @PostMapping("/service-types")
    public ResponseEntity<?> createServiceType(@RequestBody ServiceType serviceType) {
        if (serviceTypeRepository.findByLibelle(serviceType.getLibelle()).isPresent()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Ce type de service existe déjà."));
        }
        return ResponseEntity.ok(serviceTypeRepository.save(serviceType));
    }

    @PutMapping("/cancellation-fees")
    @Transactional
    public ResponseEntity<?> updateCancellationFees(@RequestBody List<CancellationFeeRule> rules) {
        feeRuleRepository.deleteAll();
        List<CancellationFeeRule> saved = feeRuleRepository.saveAll(rules);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/teams")
    @Transactional
    public ResponseEntity<?> createTeam(@RequestBody TeamRequest request) {
        Workshop workshop = workshopRepository.findById(request.getWorkshopId())
                .orElseThrow(() -> new IllegalArgumentException("Atelier non trouvé"));

        // Contrainte métier : atelier doit être actif et entreprise validée
        if (!workshop.isActif()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "L'atelier sélectionné n'est pas actif."));
        }
        if (workshop.getCompany() != null && !workshop.getCompany().isValide()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "L'entreprise associée à cet atelier n'est pas validée."));
        }

        Team team = Team.builder()
                .nom(request.getNom())
                .capacite(5) // Par défaut : 4 agents + 1 chef
                .workshop(workshop)
                .actif(true)
                .build();

        team = teamRepository.save(team);

        // Si des agents sont passés, on les lie à l'équipe
        if (request.getAgentIds() != null && !request.getAgentIds().isEmpty()) {
            List<Agent> agents = agentRepository.findAllById(request.getAgentIds());
            for (Agent agent : agents) {
                agent.setTeam(team);
                agentRepository.save(agent);
            }
            team.setAgents(agents);
        }

        return ResponseEntity.ok(team);
    }

    @PostMapping("/agents")
    public ResponseEntity<?> addAgent(@RequestBody AgentRequest request) {
        Team team = null;
        if (request.getTeamId() != null) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée"));
        }

        Agent agent = Agent.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .carteProId(request.getCarteProId())
                .valide(false) // Validé = false par défaut
                .specialite(request.getSpecialite())
                .telephone(request.getTelephone())
                .team(team)
                .build();

        return ResponseEntity.ok(agentRepository.save(agent));
    }

    @Data
    public static class TeamRequest {
        private String nom;
        private Long workshopId;
        private List<Long> agentIds;
    }

    @Data
    public static class AgentRequest {
        private String nom;
        private String prenom;
        private String carteProId;
        private String specialite;
        private String telephone;
        private Long teamId;
    }
}
