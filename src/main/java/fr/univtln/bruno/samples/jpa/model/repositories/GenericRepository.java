package fr.univtln.bruno.samples.jpa.model.repositories;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll(int pageNumber, int pageSize);
    T save(T entity);
    void delete(T entity);
}