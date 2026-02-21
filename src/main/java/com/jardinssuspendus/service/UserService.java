package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.response.UserResponse;
import com.jardinssuspendus.entity.User;
import com.jardinssuspendus.entity.enums.Role;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.exception.ResourceNotFoundException;
import com.jardinssuspendus.repository.UserRepository;
import com.jardinssuspendus.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userDetailsService.loadUserEntityByEmail(email);
    }

    public UserResponse getCurrentUserResponse() {
        return UserResponse.fromEntity(getCurrentUser());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> getAllClients() {
        return userRepository.findByRole(Role.CLIENT)
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN)
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User currentUser = getCurrentUser();

        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse promoteToAdmin(Long userId) {
        User currentUser = getCurrentUser();

        if (!currentUser.isAdmin()) {
            throw new BadRequestException("Seul un admin peut promouvoir un utilisateur");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (user.isAdmin()) {
            throw new BadRequestException("Cet utilisateur est déjà administrateur");
        }

        user.setRole(Role.ADMIN);
        userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateProfile(Long id, String name, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        User currentUser = getCurrentUser();
        if (!currentUser.isAdmin() && !currentUser.getId().equals(id)) {
            throw new BadRequestException("Vous ne pouvez modifier que votre propre profil");
        }

        if (name != null && !name.isBlank()) user.setName(name);
        if (phone != null && !phone.isBlank()) user.setPhone(phone);

        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }
}