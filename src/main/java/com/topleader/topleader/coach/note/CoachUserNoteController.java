package com.topleader.topleader.coach.note;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/latest/coach-user-note")
@RequiredArgsConstructor
public class CoachUserNoteController {


    private final CoachUserNoteRepository repository;
    private Long id;

    @Secured({"COACH", "ADMIN"})
    @PostMapping("/{userId}")
    public void addNote(@AuthenticationPrincipal UserDetails auth,  @PathVariable String userId, @RequestBody CoachUserNoteDto note) {
        repository.save(new CoachUserNote().setId(new CoachUserNote.CoachUserNoteId(auth.getUsername(), userId)).setNote(note.note));
    }

    @Secured({"COACH", "ADMIN"})
    @GetMapping("/{userId}")
    public CoachUserNoteDto getNote(@AuthenticationPrincipal UserDetails auth,  @PathVariable String userId, CoachUserNoteDto note) {
        return repository.findById(new CoachUserNote.CoachUserNoteId(auth.getUsername(), userId))
                .map(CoachUserNoteDto::toDto)
                .orElseThrow(() -> new EntityNotFoundException("CoachUserNoteDto not found for "+  userId));
    }

    public record CoachUserNoteDto(String coachId, String userId, String note) {
        public static CoachUserNoteDto toDto(CoachUserNote note) {
            return new CoachUserNoteDto(note.getId().getCoachId(), note.getId().getUserId(), note.getNote());
        }
    }
}
