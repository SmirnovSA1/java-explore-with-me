package ru.practicum.event.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.event.dto.AdminSearchEventOption;
import ru.practicum.event.dto.SearchEventOption;
import ru.practicum.event.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.event.model.EventSort.EVENT_DATE;
import static ru.practicum.event.model.EventState.PUBLISHED;

@Repository
@AllArgsConstructor
public class SearchEventRepository {
    private final EntityManager entityManager;

    public List<Event> getEventsByCriteriaByAdmin(AdminSearchEventOption option) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (option.getUsers() != null && !option.getUsers().isEmpty()) {
            predicates.add(root.get("initiator").in(option.getUsers()));
        }

        if (option.getStates() != null && !option.getStates().isEmpty()) {
            predicates.add(root.get("eventState").in(option.getStates()));
        }

        if (option.getCategories() != null && !option.getCategories().isEmpty()) {
            predicates.add(root.get("category").in(option.getCategories()));
        }

        if (option.getRangeStart() != null && option.getRangeEnd() != null) {
            predicates.add(builder.between(root.get("eventDate"), option.getRangeStart(), option.getRangeEnd()));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult(option.getFrom())
                .setMaxResults(option.getSize())
                .getResultList();
    }

    public List<Event> getEventsByCriteriaByAll(SearchEventOption option) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("eventState"), PUBLISHED));

        if (option.getText() != null && !option.getText().isEmpty()) {
            String searchText = "%" + option.getText().toUpperCase() + "%";
            List<Predicate> orPredicates = new ArrayList<>();

            orPredicates.add(builder.like(builder.upper(root.get("annotation")), searchText));
            orPredicates.add(builder.like(builder.upper(root.get("description")), searchText));
            predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (option.getCategories() != null && !option.getCategories().isEmpty()) {
            predicates.add(root.get("category").in(option.getCategories()));
        }

        if (option.getPaid() != null) {
            predicates.add(builder.equal(root.get("paid"), option.getPaid()));
        }

        if (option.getRangeStart() != null && option.getRangeEnd() != null) {
            predicates.add(builder.between(root.get("eventDate"), option.getRangeStart(), option.getRangeEnd()));
        }

        if (option.getOnlyAvailable().equals(true)) {
            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(builder.equal(root.get("participantLimit"), 0));
            orPredicates.add(builder.gt(root.get("participantLimit"), root.get("confirmedRequests")));

            predicates.add(builder.or(orPredicates.toArray(new Predicate[0])));
        }

        if (EVENT_DATE.equals(option.getSort())) {
            query.orderBy(builder.desc(root.get("eventDate")));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query)
                .setFirstResult(option.getFrom())
                .setMaxResults(option.getSize())
                .getResultList();
    }
}
