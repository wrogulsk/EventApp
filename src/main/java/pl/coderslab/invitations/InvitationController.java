package pl.coderslab.invitations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invitation")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Invitation> sendInvitation(
            @RequestParam Long eventId,
            @RequestParam String email,
            @RequestParam Long invitedByUserId) {
        Invitation invitation = invitationService.sendInvitation(eventId, email, invitedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @PostMapping("/send-multiple")
    public ResponseEntity<List<Invitation>> sendMultipleInvitations(
            @RequestParam Long eventId,
            @RequestBody List<String> emails,
            @RequestParam Long invitedByUserId) {
        List<Invitation> invitations = invitationService.sendMultipleInvitations(eventId, emails, invitedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitations);
    }

    @PutMapping("/{id}/respond")
    public ResponseEntity<Invitation> respondToInvitation(
            @PathVariable Long id,
            @RequestParam InvitationStatus response) {
        Invitation invitation = invitationService.respondToInvitation(id, response);
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Invitation>> getInvitationsForEvent(@PathVariable Long eventId) {
        List<Invitation> invitations = invitationService.getInvitationsForEvent(eventId);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Invitation>> getInvitationsForUser(@PathVariable Long userId) {
        List<Invitation> invitations = invitationService.getInvitationsForUser(userId);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/email")
    public ResponseEntity<List<Invitation>> getInvitationsByEmail(@RequestParam String email) {
        List<Invitation> invitations = invitationService.getInvitationsByEmail(email);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/event/{eventId}/pending")
    public ResponseEntity<List<Invitation>> getPendingInvitations(@PathVariable Long eventId) {
        List<Invitation> invitations = invitationService.getPendingInvitations(eventId);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<Invitation>> getPendingInvitationsForUser(@PathVariable Long userId) {
        List<Invitation> invitations = invitationService.getPendingInvitationsForUser(userId);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long id,
            @RequestParam Long cancelledByUserId) {
        invitationService.cancelInvitation(id, cancelledByUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resend")
    public ResponseEntity<Invitation> resendInvitation(@PathVariable Long id) {
        Invitation invitation = invitationService.resendInvitation(id);
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/event/{eventId}/statistics")
    public ResponseEntity<Map<InvitationStatus, Long>> getInvitationStatistics(@PathVariable Long eventId) {
        Map<InvitationStatus, Long> statistics = invitationService.getInvitationStatistics(eventId);
        return ResponseEntity.ok(statistics);
    }
}
