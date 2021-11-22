package de.koelle.christian.spring.r2dbc.ratings.business;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import de.koelle.christian.spring.r2dbc.ratings.IntTestConfiguration;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublication;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublicationRepository;
import de.koelle.christian.spring.r2dbc.ratings.shared.test.DbInitSupport;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link de.koelle.christian.spring.r2dbc.ratings.business.BusinessService}.
 */
@SpringBootTest(classes = IntTestConfiguration.class)
class BusinessServiceCrudDbTest {

	private static final LocalDateTime DATE_TIME_STABLE = LocalDateTime.of(2020, Month.JUNE, 13, 14, 15);

	@Autowired
	BusinessService service;
	@Autowired
	RatingPublicationRepository repository;
	@Autowired
	DatabaseClient database;

	@BeforeEach
	void setUp() {
		Hooks.onOperatorDebug();
		DbInitSupport.doInitDb(database);
	}

	@Test
	void exceptionTriggersTransactionRollback() {

		final RatingPublication publication = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "42", DATE_TIME_STABLE);
		final Predicate<RatingPublication> shouldTriggerExceptionPostSavingPredicate = i -> "42".equals(publication.businessVersion());

		service.createPublication(publication, shouldTriggerExceptionPostSavingPredicate)
			.as(StepVerifier::create)
			.expectError() // Error because of the exception triggered within the service
			.verify();

		// No data inserted due to rollback
		repository.findByMono(publication.domain(), publication.year(), publication.businessVersion())
			.as(StepVerifier::create)
			.verifyComplete();
	}

	@Test
	void insertDataTransactionally() {

		final RatingPublication publication = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", LocalDateTime
			.of(2020, Month.JUNE, 13, 14, 15));
		service.createPublication(publication)
			.as(StepVerifier::create)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();

		// Data inserted due to commit
		repository.findByMono(PublicationDomain.MEDIA, 2020, "v01")
			.as(StepVerifier::create)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();
	}

	@Test
	void publicationCrudAndExceptions() {

		final RatingPublication publication = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", LocalDateTime
			.of(2020, Month.JUNE, 13, 14, 15));


		ArrayList<RatingPublication> collector = new ArrayList<>();

		service.createPublication(publication)
			.as(StepVerifier::create)
			.recordWith(() -> collector)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();

		service.createPublication(publication)
			.as(StepVerifier::create)
			.expectErrorMatches(new StepExceptionPredicate(
				IllegalArgumentException.class, Set.of(
				"Publication with same business identity already exists",
				"RatingPublication",
				"v01"
			)))
			.verify();

		final RatingPublication existingOneInitial = collector.get(0);

		service.updatePublication(existingOneInitial.id(), "V42")
			.as(StepVerifier::create)
			.recordWith(() -> collector)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();

		final RatingPublication existingOnePostUpdate = collector.get(1);
		Assertions.assertEquals(existingOneInitial.id(), existingOnePostUpdate.id());
		Assertions.assertEquals("V42", existingOnePostUpdate.businessVersion());

		service.updatePublication(Integer.MAX_VALUE, "V42") // Extend
			.as(StepVerifier::create)
			.expectErrorMatches(new StepExceptionPredicate(
				IllegalArgumentException.class, Set.of(
				"Publication with the id=",
				String.valueOf(Integer.MAX_VALUE),
				"does not exist."
			)))
			.verify();
	}

	private record StepExceptionPredicate(
		Class<? extends Throwable> clazz,
		Set<String> expectedMsgFragments
	) implements Predicate<Throwable> {

		@Override
		public boolean test(final Throwable throwable) {
			return clazz.isAssignableFrom(throwable.getClass())
				&& expectedMsgFragments.stream()
				.filter(i -> !throwable.getMessage().contains(i))
				.findFirst()
				.isEmpty();
		}
	}
}
