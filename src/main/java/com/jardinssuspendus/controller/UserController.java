package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.UserResponse;
import com.jardinssuspendus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /** Profil de l'utilisateur connecté */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        return ResponseEntity.ok(ApiResponse.success("Profil récupéré", userService.getCurrentUserResponse()));
    }

    /** Mise à jour du profil */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone
    ) {
        UserResponse current = userService.getCurrentUserResponse();
        UserResponse updated = userService.updateProfile(current.getId(), name, phone);
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", updated));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Utilisateurs récupérés", userService.getAllUsers()));
    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllClients() {
        return ResponseEntity.ok(ApiResponse.success("Clients récupérés", userService.getAllClients()));
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllAdmins() {
        return ResponseEntity.ok(ApiResponse.success("Admins récupérés", userService.getAllAdmins()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Utilisateur récupéré", userService.getUserById(id)));
    }

    /** Promouvoir un client en admin */
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> promoteToAdmin(@PathVariable Long id) {
        UserResponse user = userService.promoteToAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur promu administrateur", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé"));
    }
}