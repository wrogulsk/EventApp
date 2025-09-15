package pl.coderslab.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.coderslab.invitations.Invitation;
import pl.coderslab.locations.Location;
import pl.coderslab.notifications.Notification;
import pl.coderslab.registrations.Registration;
import pl.coderslab.registrations.RegistrationStatus;
import pl.coderslab.tags.Tag;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 1, max = 70)
    private String title;

    @Size(min = 1, max = 70)
    private String organizer;

    @Column(name = "start_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endAt;

    @NotNull
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "location_id")
    @JsonIgnore
    private Location location;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Registration> registrations = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
    @JsonIgnore
    private Set<Invitation> invitations = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
    @JsonIgnore
    private Set<Notification> notifications = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "event_tags",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties("events")
    private Set<Tag> tags = new HashSet<>();

    public void addRegistration(Registration registration) {
        registrations.add(registration);
        registration.setEvent(this);
    }

    public void addInvitation(Invitation invitation) {
        invitations.add(invitation);
        invitation.setEvent(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getEvents().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getEvents().remove(this);
    }

    public int getRegisteredCount() {
        if (registrations == null) {
            return 0;
        }
        return (int) registrations.stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.CONFIRMED)
                .count();
    }

    public boolean isUserRegistered(Long userId) {
        if (registrations == null || userId == null) {
            return false;
        }
        return registrations.stream()
                .anyMatch(reg -> reg.getUser().getId().equals(userId)
                        && reg.getStatus() == RegistrationStatus.CONFIRMED);
    }

    public boolean isFull() {
        return getRegisteredCount() >= capacity;
    }

    public int getCapacity() {
        return capacity != null ? capacity : 0;
    }

}
