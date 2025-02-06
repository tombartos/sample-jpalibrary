package fr.univtln.bruno.samples.jpa.model.repositories;

import fr.univtln.bruno.samples.jpa.model.documents.Book;
import jakarta.persistence.EntityManager;

public class BookRepository extends JpaRepository<Book, Long> {
    public BookRepository(EntityManager entityManager) {
        super(Book.class, entityManager);
    }
}