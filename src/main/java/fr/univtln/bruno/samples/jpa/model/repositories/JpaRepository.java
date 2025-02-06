package fr.univtln.bruno.samples.jpa.model.repositories;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.*;

public abstract class JpaRepository<T, ID> implements GenericRepository<T, ID> {
    //A fournir dans le constructeur ou autre (cf. CDI ou Spring)
    protected EntityManager em;
    
    //A fournir ou à déduire
    private final Class<T> entityClass;
    
    protected JpaRepository(Class<T> entityClass, EntityManager entityManager) {
        this.em = entityManager;
        this.entityClass = entityClass;
    }
    
    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }
    
    @Override
    public List<T> findAll(int pageNumber, int pageSize) {
        //Utiliser des named queries ou la criteria API
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id";
        return em.createQuery(jpql, entityClass)
            .setFirstResult(pageNumber*pageSize)
            .setMaxResults(pageSize)
            .getResultList();
    }

    @Override
    public T save(T entity) {
        em.getTransaction().begin();
        if (em.contains(entity)) {
            entity = em.merge(entity);
        } else {
            em.persist(entity);
        }
        em.getTransaction().commit();
        return entity;
    }
    
    @Override
    public void delete(T entity) {
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }
}