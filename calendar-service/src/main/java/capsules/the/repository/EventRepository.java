package capsules.the.repository;

import capsules.the.domain.Event;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

public class EventRepository {

    @PersistenceContext(unitName = "servicePU")
    private EntityManager entityManager;

    @Transactional
    public void save(Event event) {
        entityManager.persist(event);
    }

    public Event find(int id) {
        return entityManager.find(Event.class, id);
    }

    public List<Event> findAll() {
        TypedQuery<Event> query = entityManager.createQuery("select e from Event e", Event.class);
        return query.getResultList();
    }

    @Transactional
    public void update(Event event) {
        entityManager.merge(event);
    }

    @Transactional
    public void remove(int id) {
        entityManager.remove(this.find(id));
    }
}
