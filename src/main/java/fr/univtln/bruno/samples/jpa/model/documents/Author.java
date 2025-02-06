
package fr.univtln.bruno.samples.jpa.model.documents;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
/**
 * Represents an Author entity in the system.
 * This class manages author information including their name, address, and associated documents.
 * - @Entity This class is mapped to the "authors" table in the database
 * - @Builder Implements the Builder pattern for object creation
 *
 * @author Emmanuel Bruno
 * @version 0.0.1
 * @since 0.0.1
 */
@Entity
@Table(name="authors")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)

@NamedQueries({
  @NamedQuery(
    name = "Author.getDocNumber",
    query = "SELECT COUNT(d) FROM Document d JOIN d.authors a WHERE a.id = :authorId"
  )
})
public class Author {
  /**
   * The unique identifier for the Author entity.
   * This ID is automatically generated using a sequence generator named "author_seq".
   * The sequence is configured to increment by 1 (allocationSize = 1).
   */
  @Id
  @SequenceGenerator(name = "author_seq", sequenceName = "author_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
  private Long id;

  /**
   * The name of the author.
   * This field cannot be null as specified by the @Column annotation.
   */
  @Column(nullable = false)
  @Setter
  @ToString.Include
  private String name;

  /**
   * The address of the author.
   * This field is embedded within the Author entity.
   */
  @Embedded
  @Setter
  private Address address;

  /**
   * The set of documents associated with the author.
   * This is a many-to-many relationship managed by the "document_author" join table.
   */
  @ManyToMany(mappedBy = "authors")
  @Setter
  private Set<Document> documents;
}

