package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.request.RoomRequest;
import com.jardinssuspendus.dto.response.RoomResponse;
import com.jardinssuspendus.entity.Room;
import com.jardinssuspendus.exception.ResourceNotFoundException;
import com.jardinssuspendus.repository.FeedbackRepository;
import com.jardinssuspendus.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@Getter
@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findAllAvailable()
                .stream()
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> getAvailableRoomsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findAvailableRoomsBetweenDates(startDate, endDate)
                .stream()
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse getRoomById(Long id) {
        return toRoomResponse(findRoomById(id));
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Room room = new Room();
        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setPrice(request.getPrice());
        room.setCapacity(request.getCapacity());
        room.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        return toRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = findRoomById(id);
        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setPrice(request.getPrice());
        room.setCapacity(request.getCapacity());
        if (request.getAvailable() != null) room.setAvailable(request.getAvailable());
        return toRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.delete(findRoomById(id));
    }

    @Transactional
    public void addImageToRoom(Long roomId, String imagePath) {
        Room room = findRoomById(roomId);
        // Ajout direct sur la liste — pas besoin de méthode helper
        room.getImages().add(imagePath);
        roomRepository.save(room);
    }

    @Transactional
    public void removeImageFromRoom(Long roomId, String imagePath) {
        Room room = findRoomById(roomId);
        // Suppression directe sur la liste — pas besoin de méthode helper
        room.getImages().remove(imagePath);
        roomRepository.save(room);
    }

    @Transactional
    public RoomResponse toggleAvailability(Long id) {
        Room room = findRoomById(id);
        room.setAvailable(!room.getAvailable());
        return toRoomResponse(roomRepository.save(room));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre", "id", id));
    }

    private RoomResponse toRoomResponse(Room room) {
        Double avgRating    = feedbackRepository.getAverageRatingByRoomId(room.getId());
        Long   totalReviews = feedbackRepository.countByRoomId(room.getId());
        return RoomResponse.fromEntity(room, avgRating, totalReviews);
    }
}