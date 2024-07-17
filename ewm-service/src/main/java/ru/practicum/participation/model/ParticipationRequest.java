package ru.practicum.participation.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "participate_request")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    private LocalDateTime created;
    @Column(name = "event_id")
    private Long event;
    @Column(name = "requester_id")
    private Long requester;
    @Enumerated(EnumType.STRING)
    private Status status;
}
