package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.Role;
import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.repository.UserRepository;
import com.hephaitos.maintenance.security.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Par défaut, rôle USER
                .actif(true)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect."));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("nom", user.getNom());
        extraClaims.put("prenom", user.getPrenom());
        extraClaims.put("id", user.getId());

        String token = jwtUtils.generateToken(extraClaims, user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        return new AuthResponse(token, refreshToken, user.getRole().name(), user.getEmail());
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtils.extractUsername(refreshToken);
        if (email != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            if (jwtUtils.isTokenValid(refreshToken, user)) {
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("role", user.getRole().name());
                extraClaims.put("nom", user.getNom());
                extraClaims.put("prenom", user.getPrenom());
                extraClaims.put("id", user.getId());

                String newToken = jwtUtils.generateToken(extraClaims, user);
                String newRefreshToken = jwtUtils.generateRefreshToken(user);
                return new AuthResponse(newToken, newRefreshToken, user.getRole().name(), user.getEmail());
            }
        }
        throw new IllegalArgumentException("Refresh token invalide ou expiré");
    }

    @Data
    public static class RegisterRequest {
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private String role;
        private String email;
    }
}
