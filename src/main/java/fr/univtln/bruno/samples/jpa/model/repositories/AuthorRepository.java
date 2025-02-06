package fr.univtln.bruno.samples.jpa.model.repositories;

import java.util.Optional;

import fr.univtln.bruno.samples.jpa.model.documents.Author;
import fr.univtln.bruno.samples.jpa.model.documents.AuthorDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class AuthorRepository extends JpaRepository<Author, Long> {
    public AuthorRepository(EntityManager entityManager){
        super(Author.class, entityManager);
    }
	
    public Optional<TypedQuery<AuthorDTO>> getAuthorsByDocNb(int docnb, int FirstResult, int MaxResults){
        try {
        TypedQuery<AuthorDTO> query =  em.createQuery("SELECT new fr.univtln.bruno.samples.jpa.model.documents.AuthorDTO(a.id, a.name) FROM Author a WHERE SIZE(a.documents) = :docnb", AuthorDTO.class)
        .setParameter("docnb", docnb)
        .setFirstResult(FirstResult)
        .setMaxResults(MaxResults);
        return Optional.of(query);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
}
