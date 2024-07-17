package ru.practicum.compilation.model;

import lombok.*;
import ru.practicum.event.model.Event;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilation")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    private Long id;
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "event_compilation",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events;
    private Boolean pinned;
    private String title;
}
