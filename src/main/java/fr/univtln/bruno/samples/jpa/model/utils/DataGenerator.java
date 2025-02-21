package fr.univtln.bruno.samples.jpa.model.utils;

import com.github.javafaker.Faker;
import fr.univtln.bruno.samples.jpa.model.Loan;
import fr.univtln.bruno.samples.jpa.model.documents.Address;
import fr.univtln.bruno.samples.jpa.model.documents.Author;
import fr.univtln.bruno.samples.jpa.model.documents.Book;
import fr.univtln.bruno.samples.jpa.model.users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Utility class for generating sample data for the library management system.
 * This class generates authors, books, and loans using the Faker library.
 * It manages transactions and ensures data consistency.
 */
@Slf4j
public class DataGenerator implements AutoCloseable {
  private final Faker faker = new Faker();
  private final Random random = new Random();
  private final int authorCount;
  private final int userCount;
  private final int bookCount;
  private final int loanCount;
  private final EntityManager entityManager;

  /**
   * Constructs a DataGenerator with the specified parameters.
   *
   * @param entityManagerFactory the factory to create EntityManager instances
   * @param authorCount the number of authors to generate
   * @param userCount the number of users to generate
   * @param bookCount the number of books to generate
   * @param loanCount the number of loans to generate
   */
  @Builder
  private DataGenerator(EntityManagerFactory entityManagerFactory,
                        int authorCount,
                        int userCount,
                        int bookCount,
                        int loanCount) {
    if (entityManagerFactory == null)
      throw new IllegalArgumentException("EntityManagerFactory cannot be null");
    if (authorCount < 0 || bookCount < 0 || loanCount < 0 || userCount < 0)
      throw new IllegalArgumentException("Count values must be positive");

    this.authorCount = authorCount;
    this.userCount = userCount;
    this.bookCount = bookCount;
    this.loanCount = loanCount;
    this.entityManager = entityManagerFactory.createEntityManager();
  }

  /**
   * Creates a new Author with random data.
   *
   * @return a new Author instance
   */
  public Author createAuthor() {
    com.github.javafaker.Address fakeAddress = faker.address();
    return Author.builder()
      .name(faker.name().fullName())
      .address(
        Address.builder()
          .street(fakeAddress.streetAddress())
          .city(fakeAddress.city())
          .country(fakeAddress.country())
          .build())
      .build();
  }

  /**
   * Retrieves a list of random authors from the database.
   *
   * @param n the number of authors to retrieve
   * @return a list of random authors
   */
  public List<Author> getRandomAuthors(int n) {
    TypedQuery<Author> query = entityManager.createQuery("SELECT a FROM Author a ORDER BY FUNCTION('RANDOM')",
      Author.class);
    query.setMaxResults(n);
    return query.getResultList();
  }

  /**
   * Creates a new Book with random data.
   *
   * @return a new Book instance
   */
  public Book createBook() {
    return Book.builder()
      .isbn(faker.code().isbn13())
      .pages(faker.number().numberBetween(50, 1000))
      .publicationDate(LocalDate.now().minusYears(random.nextInt(20)))
      .title(faker.book().title())
      .authors(getRandomAuthors(random.nextInt(3) + 1))
      .build();
  }

  /**
   * Retrieves a random free book that is not currently on loan as of the given date.
   *
   * @param date the date to check for book availability
   * @return an Optional containing a random free book, or empty if no free book is found
   */
  public Optional<Book> getRandomFreeBook(LocalDate date) {
    TypedQuery<Book> query = entityManager.createQuery(
      "SELECT b FROM Book b WHERE b.id NOT IN (SELECT l.document.id FROM Loan l WHERE l.returnDate IS NULL OR l.dueDate > :date) ORDER BY FUNCTION('RANDOM')",
      Book.class);
    query.setParameter("date", date);
    query.setMaxResults(1);
    return Optional.of(query.getSingleResult());
  }

  /**
   * Creates a new Loan for the specified user with a random free book.
   *
   * @param user the user to create the loan for
   * @return a new Loan instance, or null if no free book is available
   */
  public Loan createLoan(User user) {
    LocalDate loanDate = LocalDate.now().minusDays(random.nextInt(30));

    return getRandomFreeBook(loanDate)
      .map(book -> {
        Loan loan = new Loan();
        loan.setDocument(book);
        loan.setDueDate(loanDate.plusDays(14));
        loan.setUser(user);
        if (random.nextBoolean()) {
          loan.setReturnDate(loanDate.plusDays(random.nextInt(20)));
        }
        return loan;
      }).orElse(null);
  }

  /**
   /**
   * Creates a new User with random data.
   * Ensures the email is unique by checking the database.
   *
   * @return a new User instance
   */
  public User createUser() {
    User user;
    String email;
    do {
      email = faker.internet().emailAddress();
      TypedQuery<Long> query = entityManager.createQuery(
        "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
      query.setParameter("email", email);
      long count = query.getSingleResult();
      if (count == 0) {
        user = new User();
        user.setName(faker.name().fullName());
        user.setEmail(email);
        break;
      }
    } while (true);
    return user;
  }

  /**
   * Generates sample data for authors, books, and loans.
   * This method manages the transaction and ensures data consistency.
   */
  public void generateData() {
    entityManager.getTransaction().begin();
    try {
      // Generate authors
      IntStream.range(0, authorCount)
        .mapToObj(i -> createAuthor())
        .forEach(entityManager::persist);

      // Generate users
      IntStream.range(0, userCount)
        .mapToObj(i -> createUser())
        .forEach(entityManager::persist);

      // Generate books
      IntStream.range(0, bookCount)
        .mapToObj(i -> createBook())
        .forEach(entityManager::persist);

      // Generate loans
      IntStream.range(0, loanCount)
        .forEach(i -> entityManager.persist(createLoan(getRandomUser())));
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      log.error("Error generating data", e);
    }
  }

  /**
   * Retrieves a random user from the database.
   *
   * @return a random User instance
   */
  private User getRandomUser() {
    TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u ORDER BY FUNCTION('RANDOM')", User.class);
    query.setMaxResults(1);
    return query.getSingleResult();
  }

  /**
   * Closes the EntityManager to release resources.
   */
  @Override
  public void close() {
    if (entityManager != null && entityManager.isOpen()) {
      entityManager.close();
    }
  }
}
