package com.hephaitos.maintenance.config;

import com.hephaitos.maintenance.entity.*;
import com.hephaitos.maintenance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final WorkshopRepository workshopRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final TeamRepository teamRepository;
    private final AgentRepository agentRepository;
    private final CancellationFeeRuleRepository feeRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Vérification et initialisation des données par défaut...");

        // 1. Initialisation des types de services
        if (serviceTypeRepository.count() == 0) {
            serviceTypeRepository.save(new ServiceType(null, "Plomberie", "Dépannage fuite, robinetterie, tuyauterie", "WrenchIcon"));
            serviceTypeRepository.save(new ServiceType(null, "Menuiserie", "Pose de portes, fenêtres, réparation meubles", "HomeIcon"));
            serviceTypeRepository.save(new ServiceType(null, "Automobile", "Mécanique générale, vidange, pneumatiques", "SparklesIcon"));
            log.info("Types de services initialisés.");
        }

        // 2. Initialisation des règles de frais d'annulation
        if (feeRuleRepository.count() == 0) {
            feeRuleRepository.save(new CancellationFeeRule(null, 48, BigDecimal.valueOf(10.00)));
            feeRuleRepository.save(new CancellationFeeRule(null, 24, BigDecimal.valueOf(30.00)));
            feeRuleRepository.save(new CancellationFeeRule(null, 0, BigDecimal.valueOf(50.00)));
            log.info("Règles de frais d'annulation initialisées.");
        }

        // 3. Initialisation d'une entreprise par défaut
        Company company = null;
        if (companyRepository.count() == 0) {
            company = Company.builder()
                    .nom("Keyce Maintenance Group")
                    .siret("12345678901234")
                    .valide(true) // Validée par défaut pour les tests
                    .adresse("Rue de la Joie, Douala")
                    .telephone("+237690000001")
                    .email("contact@keycemaintenance.cm")
                    .description("Spécialiste de la maintenance rapide d'urgence")
                    .build();
            company = companyRepository.save(company);
            log.info("Entreprise par défaut créée.");
        } else {
            company = companyRepository.findAll().get(0);
        }

        // 4. Initialisation des utilisateurs de test
        if (userRepository.count() == 0) {
            // Admin
            userRepository.save(User.builder()
                    .nom("Admin")
                    .prenom("Hephaitos")
                    .email("admin@hephaitos.com")
                    .telephone("+237690000002")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .actif(true)
                    .build());

            // Manager
            userRepository.save(User.builder()
                    .nom("Manager")
                    .prenom("Keyce")
                    .email("manager@hephaitos.com")
                    .telephone("+237690000003")
                    .password(passwordEncoder.encode("manager123"))
                    .role(Role.COMPANY_MANAGER)
                    .company(company)
                    .actif(true)
                    .build());

            // User client
            userRepository.save(User.builder()
                    .nom("Client")
                    .prenom("Jean")
                    .email("client@hephaitos.com")
                    .telephone("+237690000004")
                    .password(passwordEncoder.encode("client123"))
                    .role(Role.USER)
                    .actif(true)
                    .build());

            log.info("Utilisateurs de test créés (admin@hephaitos.com, manager@hephaitos.com, client@hephaitos.com). Mot de passe: [identifiant]123");
        }

        // 5. Initialisation d'ateliers
        if (workshopRepository.count() == 0 && company != null) {
            Workshop w1 = Workshop.builder()
                    .nom("Atelier Plomberie Douala Centre")
                    .adresse("Akwa, Douala")
                    .ville("Douala")
                    .latitude(4.0511)
                    .longitude(9.7085)
                    .telephone("+237690000005")
                    .email("akwa@keycemaintenance.cm")
                    .serviceType("Plomberie")
                    .deplacementPossible(true)
                    .horaires("Lun-Ven: 8h-18h, Sam: 9h-14h")
                    .tarifJson("{\"intervention\":15000,\"deplacement\":5000,\"urgence\":10000}")
                    .company(company)
                    .actif(true)
                    .build();

            Workshop w2 = Workshop.builder()
                    .nom("Menuiserie Fine de Yaoundé")
                    .adresse("Bastos, Yaoundé")
                    .ville("Yaoundé")
                    .latitude(3.8480)
                    .longitude(11.5021)
                    .telephone("+237690000006")
                    .email("yde@keycemaintenance.cm")
                    .serviceType("Menuiserie")
                    .deplacementPossible(true)
                    .horaires("Lun-Sam: 8h-19h")
                    .tarifJson("{\"intervention\":20000,\"deplacement\":7000,\"urgence\":12000}")
                    .company(company)
                    .actif(true)
                    .build();

            Workshop w3 = Workshop.builder()
                    .nom("Garage Auto Douala Bonapriso")
                    .adresse("Bonapriso, Douala")
                    .ville("Douala")
                    .latitude(4.0298)
                    .longitude(9.6997)
                    .telephone("+237690000007")
                    .email("bonapriso@keycemaintenance.cm")
                    .serviceType("Automobile")
                    .deplacementPossible(false) // Uniquement sur place
                    .horaires("7j/7, 24h/24")
                    .tarifJson("{\"diagnostic\":10000,\"vidange\":25000,\"reparation\":40000}")
                    .company(company)
                    .actif(true)
                    .build();

            w1 = workshopRepository.save(w1);
            w2 = workshopRepository.save(w2);
            w3 = workshopRepository.save(w3);
            log.info("Ateliers d'exemples créés.");

            // 6. Création d'équipes et d'agents
            Team teamPlomberie = Team.builder()
                    .nom("Équipe Plombier Rapide")
                    .capacite(5)
                    .workshop(w1)
                    .actif(true)
                    .build();
            teamPlomberie = teamRepository.save(teamPlomberie);

            List<Agent> agents = new ArrayList<>();
            agents.add(new Agent(null, "Eto'o", "Samuel", "PRO-PL-001", true, teamPlomberie, "Soudure & Canalisations", "+237650000001"));
            agents.add(new Agent(null, "Song", "Rigobert", "PRO-PL-002", true, teamPlomberie, "Robinetterie", "+237650000002"));
            agents.add(new Agent(null, "Milla", "Roger", "PRO-PL-003", true, teamPlomberie, "Chauffe-eau", "+237650000003"));
            agents.add(new Agent(null, "Mbappé", "Kylian", "PRO-PL-004", true, teamPlomberie, "Tuyauterie générale", "+237650000004"));
            agents.add(new Agent(null, "Nkoulou", "Nicolas", "PRO-PL-005", true, teamPlomberie, "Débouchage", "+237650000005")); // Chef

            agentRepository.saveAll(agents);
            log.info("Équipe de plomberie et 5 agents créés.");
        }
    }
}
