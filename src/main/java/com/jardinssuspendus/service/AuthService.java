package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.request.LoginRequest;
import com.jardinssuspendus.dto.request.RegisterRequest;
import com.jardinssuspendus.dto.response.AuthResponse;
import com.jardinssuspendus.entity.User;
import com.jardinssuspendus.entity.enums.Role;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.exception.EmailAlreadyExistsException;
import com.jardinssuspendus.exception.UnauthorizedException;
import com.jardinssuspendus.repository.UserRepository;
import com.jardinssuspendus.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OTPService otpService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail(), true);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENT);
        user.setEnabled(false);

        userRepository.save(user);
        otpService.generateAndSendOTP(request.getEmail());
    }

    @Transactional
    public AuthResponse verifyOTPAndActivate(String email, String code) {
        otpService.verifyOTP(email, code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouvé"));

        user.setEnabled(true);
        userRepository.save(user);

        String token = tokenProvider.generateTokenFromEmail(email);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (!user.getEnabled()) {
            throw new UnauthorizedException("Compte non activé. Veuillez vérifier votre email.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @Transactional
    public void resendOTP(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouvé"));

        if (user.getEnabled()) {
            throw new BadRequestException("Ce compte est déjà activé");
        }

        otpService.generateAndSendOTP(email);
    }
}