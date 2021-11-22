package de.koelle.christian.spring.r2dbc.ratings.business;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublication;
import de.koelle.christian.spring.r2dbc.ratings.dao.RatingPublicationRepository;
import de.koelle.christian.spring.r2dbc.ratings.shared.test.DbInitSupport;
import de.koelle.christian.spring.r2dbc.ratings.IntTestConfiguration;
import de.koelle.christian.spring.r2dbc.ratings.shared.types.PublicationDomain;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link de.koelle.christian.spring.r2dbc.ratings.business.BusinessService}.
 *
 * @author Oliver Drotbohm
 * @soundtrack Shame - Tedeschi Trucks Band (Signs)
 */
@SpringBootTest(classes = IntTestConfiguration.class)
class BusinessServiceGeneralDbTest {

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

		final RatingPublication ratingPublication = new RatingPublication(null, PublicationDomain.MEDIA, 2020, "42", LocalDateTime
			.of(2020, Month.JUNE, 13, 14, 15));
		final Predicate<RatingPublication> shouldTriggerExceptionPostSavingPredicate = i -> "42".equals(ratingPublication.businessVersion());

		service.createPublication(ratingPublication, shouldTriggerExceptionPostSavingPredicate)
			.as(StepVerifier::create)
			.expectError() // Error because of the exception triggered within the service
			.verify();

		// No data inserted due to rollback
		repository.findByMono(ratingPublication.domain(), ratingPublication.year(), ratingPublication.businessVersion())
			.as(StepVerifier::create)
			.verifyComplete();
	}

	@Test
	void insertsDataTransactionally() {

		service.createPublication(new RatingPublication(null, PublicationDomain.MEDIA, 2020, "v01", LocalDateTime.of(2020, Month.JUNE, 13, 14, 15)))
			.as(StepVerifier::create)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();

		// Data inserted due to commit
		repository.findByMono(PublicationDomain.MEDIA, 2020, "v01")
			.as(StepVerifier::create)
			.expectNextMatches(RatingPublication::hasId)
			.verifyComplete();
	}
}
