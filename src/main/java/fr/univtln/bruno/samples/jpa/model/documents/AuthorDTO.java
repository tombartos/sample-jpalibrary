package fr.univtln.bruno.samples.jpa.model.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
public class AuthorDTO {
    private long id;
    private String name;
}
