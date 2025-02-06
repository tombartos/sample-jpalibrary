package fr.univtln.bruno.samples.jpa.model.repositories;

import fr.univtln.bruno.samples.jpa.model.documents.Document;
import jakarta.persistence.EntityManager;

public class DocumentRepository extends JpaRepository<Document, Long>{
    public DocumentRepository (EntityManager entityManager){
        super(Document.class, entityManager);
    }
    
}
