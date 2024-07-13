package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.HitEndpoint;
import ru.practicum.model.StatsView;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<HitEndpoint, Long> {
    @Query("SELECT new ru.practicum.model.StatsView(h.app, h.uri, COUNT(h.ip)) FROM HitEndpoint AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<StatsView> findAllByDateBetweenAndUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.model.StatsView(h.app, h.uri, COUNT(DISTINCT h.ip)) FROM HitEndpoint AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<StatsView> findAllByDateBetweenAndUriAndUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.model.StatsView(h.app, h.uri, COUNT(h.ip)) FROM HitEndpoint AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<StatsView> findAllByDateBetweenStartAndEnd(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.StatsView(h.app, h.uri, COUNT(DISTINCT h.ip)) FROM HitEndpoint AS h " +
            "WHERE (h.timestamp BETWEEN :start AND :end) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<StatsView> findAllByDateBetweenAndUniqueIp(LocalDateTime start, LocalDateTime end);
}
