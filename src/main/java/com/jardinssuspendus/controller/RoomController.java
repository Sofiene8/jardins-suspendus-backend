package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.request.RoomRequest;
import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.RoomResponse;
import com.jardinssuspendus.service.FileStorageService;
import com.jardinssuspendus.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private FileStorageService fileStorageService;

    // ─── Lecture (public) ─────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.success("Chambres récupérées", roomService.getAllRooms()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<RoomResponse> rooms = (startDate != null && endDate != null)
                ? roomService.getAvailableRoomsBetweenDates(startDate, endDate)
                : roomService.getAvailableRooms();

        return ResponseEntity.ok(ApiResponse.success("Chambres disponibles récupérées", rooms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chambre récupérée", roomService.getRoomById(id)));
    }

    /** Servir les images stockées localement */
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse room = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chambre créée avec succès", room));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id, @Valid @RequestBody RoomRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Chambre mise à jour", roomService.updateRoom(id, request)));
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Disponibilité modifiée", roomService.toggleAvailability(id)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Chambre supprimée"));
    }

    /** Upload d'une image vers une chambre */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        String filename = fileStorageService.storeFile(file);
        roomService.addImageToRoom(id, filename);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploadée avec succès", filename));
    }

    /** Suppression d'une image d'une chambre */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @RequestParam String imageName
    ) {
        roomService.removeImageFromRoom(id, imageName);
        fileStorageService.deleteFile(imageName);
        return ResponseEntity.ok(ApiResponse.success("Image supprimée"));
    }
}