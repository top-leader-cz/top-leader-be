package com.topleader.topleader.coach.note;

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

    @Secured({"COACH", "ADMIN"})
    @PostMapping("/{userId}")
    public void addNote(@AuthenticationPrincipal UserDetails auth, @PathVariable String userId, @RequestBody CoachUserNoteDto note) {
        var existingNote = repository.findByCoachIdAndUserId(auth.getUsername(), userId);
        var noteEntity = existingNote.orElse(new CoachUserNote()
                .setCoachId(auth.getUsername())
                .setUserId(userId));
        noteEntity.setNote(note.note);
        repository.save(noteEntity);
    }

    @Secured({"COACH", "ADMIN"})
    @GetMapping("/{userId}")
    public CoachUserNoteDto getNote(@AuthenticationPrincipal UserDetails auth, @PathVariable String userId, CoachUserNoteDto note) {
        return repository.findByCoachIdAndUserId(auth.getUsername(), userId)
                .map(CoachUserNoteDto::toDto)
                .orElse(CoachUserNoteDto.empty(auth.getUsername(), userId));
    }

    public record CoachUserNoteDto(String coachId, String userId, String note) {
        public static CoachUserNoteDto toDto(CoachUserNote note) {
            return new CoachUserNoteDto(note.getCoachId(), note.getUserId(), note.getNote());
        }

        public static CoachUserNoteDto empty(String coachId, String userId) {
            return new CoachUserNoteDto(coachId, userId, null);
        }
    }
}
